/*
 * AuthenticationContext.java
 * Copyright © 2018, Richard Arriaga.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * * Neither the name of the copyright holder nor the names of the contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */

package org.availlang.raa.api;
import org.availlang.raa.ApplicationRuntime;
import org.availlang.raa.ApplicationRuntime.ExitCode;
import org.availlang.raa.ApplicationState;
import org.availlang.raa.exceptions.ApplicationException;
import org.availlang.raa.exceptions.PropertiesException;
import org.availlang.raa.exceptions.StateException;
import org.availlang.raa.utilities.PropertiesManager;

import javax.annotation.Nullable;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.function.BiConsumer;

/**
 * An {@code AuthenticationContext} is a context that contains account and
 * authentication information for using secure Backblaze API. It manages all
 * {@link APIRequest} traffic.
 *
 * <p>
 * Each secure request is made asynchronously using new connections for each
 * request, making it is necessary to send the authorization token, that
 * identifies the server's active session, with each request in order to access
 * the account in a secure way. This requires that the authentication state be
 * checked prior to each request. Because requests are processed concurrently,
 * it is necessary to synchronize this process when sending each request to the
 * Backblaze server to ensure changes have not been made to the authentication
 * state since the request was first created.
 * </p>
 *
 * <p>
 * The application should be further extended to ensure the requests being made
 *
 * </p>
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
public class AuthenticationContext
{
	/**
	 * The application's sole Authorization.
	 */
	public static final AuthenticationContext soleInstance =
		new AuthenticationContext();

	/**
	 * The application key for the account.
	 */
	private @Nullable String applicationKey;

	/**
	 * Answer the application key for the account Backblaze account.
	 *
	 * @return A String.
	 */
	public static @Nullable String applicationKey ()
	{
		return soleInstance.applicationKey;
	}

	/**
	 * The identifier for the account.
	 */
	private @Nullable String accountId;

	/**
	 * Answer the identifier for the account.
	 *
	 * @return A String.
	 */
	public static @Nullable String accountId ()
	{
		return soleInstance.accountId;
	}

	/**
	 * Has the account information been initialized?
	 *
	 * @return {@code true} if the account information has been added to the
	 *         application; {@code false} otherwise.
	 */
	private static boolean isAccountInitialized ()
	{
		return soleInstance.accountId != null
			&& soleInstance.applicationKey != null;
	}

	/**
	 * Update the account information being used.
	 *
	 * @param accountId
	 *        The identifier for the account.
	 * @param applicationKey
	 *        The application key for the account.
	 */
	public void updateAccountInfo (
		final String accountId,
		final String applicationKey)
	{
		synchronized (ApplicationRuntime.stateLock)
		{
			this.applicationKey = applicationKey;
			this.accountId = accountId;

			// Any authentication state from a previous authentication is now
			// obsolete
			clearAuthenticationCredentials();

			// Drop the waiting requests as they would have been for the
			// previous account info.
			waitingRequestQueue.clear();
			ApplicationRuntime.setState(ApplicationState.ACCOUNT_INITIALIZED);
		}
	}

	/**
	 * Clear the credentials used in secure API calls.
	 */
	private void clearAuthenticationCredentials ()
	{
		this.authorizationToken = null;
		this.downloadUrl = null;
		this.apiUrl = null;
	}

	/**
	 * A {@link BiConsumer} that accepts two Strings; the {@link #accountId} and
	 * the {@link #applicationKey} that can be run to authenticate the account.
	 */
	private @Nullable BiConsumer<String, String> authenticator;

	/**
	 * Clean up the {@link AuthenticationContext}. Mostly used for testing.
	 */
	public static void cleanUp ()
	{
		soleInstance.clearAuthenticationCredentials();
		soleInstance.authenticator = null;
	}

	/**
	 * Set the {@link BiConsumer} that accepts two Strings; the {@link
	 * #accountId} and the {@link #applicationKey} that can be run to
	 * authenticate the account.
	 *
	 * @param authenticator
	 *        A {@code BiConsumer} to set.
	 */
	public static void setAuthenticator (
		final BiConsumer<String, String> authenticator)
	{
		synchronized (ApplicationRuntime.stateLock)
		{
			soleInstance.authenticator = authenticator;
		}
	}

	/**
	 * A {@link BlockingQueue} for holding {@link APIRequest}s waiting to
	 * be sent to the B2 API server while the application authenticates with
	 * the server.
	 */
	private final BlockingQueue<APIRequest<?>> waitingRequestQueue =
		new LinkedBlockingQueue<>();

	/**
	 * The number of {@link APIRequest}s in the {@link #waitingRequestQueue}.
	 *
	 * @return An {@code integer}.
	 */
	public static int queueCount ()
	{
		return soleInstance.waitingRequestQueue.size();
	}

	/**
	 * Authenticate this application with the Backblaze server.
	 *
	 * <p>
	 * <strong>NOTE:</strong> This method causes a synchronization, forcing
	 * {@link APIRequest}s to wait until it completes.
	 * </p>
	 */
	public static void authenticate ()
	{
		synchronized (ApplicationRuntime.stateLock)
		{
			ApplicationRuntime.setState(ApplicationState.AUTHENTICATING);
			soleInstance.privateAuthenticate();
		}
	}

	/**
	 * This method contains the core implementation for authenticating the
	 * application.
	 *
	 * <p>
	 * <strong>NOTE:</strong> This method should only every be in
	 * </p>
	 */
	private void privateAuthenticate ()
	{
		clearAuthenticationCredentials();
		if (authenticator == null)
		{
			System.err.println(
				"No authenticator set for AuthenticationContext");
			ExitCode.AUTHENTICATION_FAILURE.shutdown();
		}
		if (!isAccountInitialized())
		{
			try
			{
				PropertiesManager.retrieveAccountInfo();
			}
			catch (final PropertiesException e)
			{
				e.printStackTrace();
				ExitCode.AUTHENTICATION_FAILURE.shutdown();
			}
		}
		authenticator.accept(accountId, applicationKey);
		ApplicationRuntime.setState(ApplicationState.AUTHENTICATED);
		// Make all the requests waiting for authentication to complete
		waitingRequestQueue.forEach(this::sendRequest);
		waitingRequestQueue.clear();
	}

	/**
	 * Process the provided {@link APIRequest}.
	 *
	 * @param request
	 *        A {@code SecureRequest}.
	 */
	private void privateProcessRequest (
		final APIRequest<?> request)
	{
		// Make an opportune check of the volatile application state
		switch (ApplicationRuntime.state())
		{
			case APPLICATION_UNINITIALIZED:
			{    // Requests shouldn't be fielded now, this is a bug.
				final ApplicationException ex =
					new ApplicationException(
						ExitCode.UNEXPECTED_EXCEPTION,
						"Should not be making requests before app is "
							+ "initialized!");
				ex.printStackTrace();
				ex.exitCode.shutdown();
				break;
			}
			case ACCOUNT_UNINITIALIZED:
				// Any requests coming in have to be from a prior initialized
				// account and as such should be thrown away? Potentailly call
				// the failbackContinuation?
				break;
			case ACCOUNT_INITIALIZED:
			case AUTHENTICATED:
			{
				synchronized (ApplicationRuntime.stateLock)
				{
					switch (ApplicationRuntime.state())
					{
						case AUTHENTICATED:
							sendRequest(request);
							break;
						case ACCOUNT_INITIALIZED:
						{
							if (request.isAuthenticatedRequest())
							{
								waitingRequestQueue.add(request);
							}
							else
							{
								sendRequest(request);
							}
							break;
						}
						case ACCOUNT_UNINITIALIZED:
							// Any requests coming in have to be from a prior initialized
							// account and as such should be thrown away? Potentailly call
							// the failbackContinuation?
							break;
						case AUTHENTICATING:
							waitingRequestQueue.add(request);
							break;
						case APPLICATION_UNINITIALIZED:
						{    // Requests shouldn't be fielded now, this is a bug.
							final ApplicationException ex =
								new ApplicationException(
									ExitCode.UNEXPECTED_EXCEPTION,
									"Should not be making requests before app is "
										+ "initialized!");
							ex.printStackTrace();
							ex.exitCode.shutdown();
							break;
						}
					}
				}
				break;
			}
			case AUTHENTICATING:
				waitingRequestQueue.add(request);
				break;
		}
	}

	/**
	 * Process the provided {@link APIRequest}.
	 *
	 * @param request
	 *        A {@code SecureRequest}.
	 */
	public static void processRequest (final APIRequest<?> request)
	{
		soleInstance.privateProcessRequest(request);
	}

	/**
	 * Send the {@link APIRequest} to the B2 API server.
	 *
	 * @param request
	 *        The {@code SecureRequest}
	 */
	private void sendRequest (final APIRequest<?> request)
	{
		if (request.validSendStates().contains(ApplicationRuntime.state()))
		{
			ApplicationRuntime.scheduleTask(() ->
			{
				if (request.usesAuthorizationToken())
				{
					final String token = authorizationToken;
					assert token != null;
					request.setAuthorizationToken(token);
				}
				final String url =
					request.isDownloadRequest() ? downloadUrl : apiUrl;
				assert url != null;
				request.setBaseClientLocationIdentifier(url);
				ApplicationRuntime.client().processRequest(request);
			});
		}
		else
		{
			request.failureContinuation().accept(
				new StateException(String.format(
					"Attempted to send a %s while in the state %s",
					request.getClass().getSimpleName(),
					ApplicationRuntime.state().name())));
		}
	}

	/**
	 * The authorization token to use with all secure API calls.
	 */
	private @Nullable String authorizationToken;

	/**
	 * The String url that is to be used for future downloads.
	 */
	private @Nullable String downloadUrl;

	/**
	 * The String to make authenticated API calls to.
	 */
	private @Nullable String apiUrl;

	/**
	 * The {@link ScheduledFuture} that holds the timer until the application
	 * is re-authenticated.
	 */
	private @Nullable ScheduledFuture<?> reauthenticateScheduledFuture;

	/**
	 * Reset the {@link ScheduledFuture} ({@link
	 * #reauthenticateScheduledFuture}) that holds the timer until the
	 * application is re-authenticated.
	 *
	 * @param timeUntilExpires
	 *        The time in milliseconds until the application is
	 *        re-authenticated.
	 */
	private void resetReauthenticateScheduledFuture (
		final long timeUntilExpires)
	{
		if (reauthenticateScheduledFuture != null)
		{
			reauthenticateScheduledFuture.cancel(false);
		}
		reauthenticateScheduledFuture =
			ApplicationRuntime.startTimer(
				timeUntilExpires, AuthenticationContext::authenticate);
	}

	/**
	 * Update the data that authorizes the application to use with all secure
	 * API calls.
	 *
	 * @param authorizationToken
	 *        The authorization token to use with all secure API calls. This
	 *        authorization token is valid for the next {@code timeUntilExpires}
	 *        milliseconds.
	 * @param downloadUrl
	 *        The String url that is to be used for future downloads.
	 * @param apiUrl
	 *        The String to make authenticated API calls to.
	 * @param timeUntilExpires
	 *        The time in milliseconds until the {@link #authorizationToken}
	 *        will expire.
	 */
	public static void setAuthorizationData (
		final String authorizationToken,
		final String downloadUrl,
		final String apiUrl,
		final long timeUntilExpires)
	{
		soleInstance.authorizationToken = authorizationToken;
		soleInstance.downloadUrl = downloadUrl;
		soleInstance.apiUrl = apiUrl;
		soleInstance.resetReauthenticateScheduledFuture(timeUntilExpires);
	}

	/**
	 * Construct a {@link AuthenticationContext}.
	 */
	private AuthenticationContext () { /* Do Nothing */ }
}
