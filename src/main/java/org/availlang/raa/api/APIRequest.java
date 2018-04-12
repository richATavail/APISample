/*
 * APIRequest.java
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
import com.avail.utility.json.JSONFriendly;
import com.avail.utility.json.JSONObject;
import org.availlang.raa.ApplicationRuntime.ExitCode;
import org.availlang.raa.ApplicationState;
import org.availlang.raa.B2File;
import org.availlang.raa.exceptions.ApplicationException;

import java.util.Set;
import java.util.function.Consumer;

/**
 * A {@code APIRequest} is a Backblaze API that is a member of a specific {@link
 * APIGroup}.
 *
 * @param <Response> The type {@link APIResponse} that is paired with this
 *                   {@code APIRequest}.
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
public abstract class APIRequest<Response extends APIResponse>
implements JSONFriendly
{
	/**
	 * The {@link Consumer} that accepts a {@code Response} to the request.
	 */
	private final Consumer<Response> responseConsumer;

	/**
	 * The {@link Consumer} that accepts an {@link ApplicationException} to call
	 * in the event of a managed failure.
	 */
	private final Consumer<ApplicationException> failureContinuation;

	/**
	 * Answer the {@link Consumer} that accepts an {@link ApplicationException}
	 * to call in the event of a managed failure.
	 *
	 * @return A {@code Consumer}.
	 */
	public Consumer<ApplicationException> failureContinuation ()
	{
		return failureContinuation;
	}

	/**
	 * The {@link Set} of {@link ApplicationState}s that allows for this {@link
	 * APIRequest} to be sent.
	 *
	 * @return A {@code Set}.
	 */
	public abstract Set<ApplicationState> validSendStates ();

	/**
	 * Create a {@link Response} from the provided content.
	 *
	 * @param content
	 *        The {@link JSONObject} to populate the response with.
	 * @return A {@code Response}.
	 */
	protected abstract Response createResponse (final JSONObject content);

	/**
	 * Answer the {@link APICatalogue} member that represents this {@link
	 * APIRequest}.
	 *
	 * @return An {@code APICatalogue}
	 */
	public abstract APICatalogue catalogue ();

	/**
	 * The name of the API operation.
	 *
	 * @return A String.
	 */
	public abstract String apiOperationName ();

	/**
	 * The current Backblaze API version of this {@link APIRequest}.
	 *
	 * @return A String.
	 */
	@SuppressWarnings("SameReturnValue")
	public abstract String apiVersion ();

	/**
	 * The {@link APIGroup} this {@link APIRequest} belongs to.
	 *
	 * @return The {@code APIGroup}.
	 */
	@SuppressWarnings("SameReturnValue")
	public abstract APIGroup apiGroup ();

	/**
	 * Confirm this {@link APIRequest} is compatible with its {@link
	 * APIResponse}.
	 */
	@SuppressWarnings("unused")
	protected abstract void checkCompatibility ();

	/**
	 * The base target location for the request.
	 */
	private String baseClientLocationIdentifier = "";

	/**
	 * Set the base target location for the request.
	 *
	 * @param baseClientLocationIdentifier
	 *        The String location.
	 */
	public void setBaseClientLocationIdentifier (
		final String baseClientLocationIdentifier)
	{
		this.baseClientLocationIdentifier = baseClientLocationIdentifier;
	}

	/**
	 * Answer the base target location for the request.
	 *
	 * @return A String.
	 */
	public String baseClientLocationIdentifier ()
	{
		return baseClientLocationIdentifier;
	}

	/**
	 * Answer whether or not this {@link APIRequest} uses an {@link
	 * AuthenticationContext#authorizationToken} when making its request?
	 *
	 * @return {@code true} indicates it does; {@code false} otherwise.
	 */
	public boolean usesAuthorizationToken ()
	{
		// Defaulted to true as all the API is likely to require a token.
		return true;
	}

	/**
	 * The {@link AuthenticationContext#authorizationToken} for this
	 * {@link APIRequest} given {@link #usesAuthorizationToken()} evaluates to
	 * {@link true}.
	 */
	private String authorizationToken = "";

	/**
	 * Set the {@link #authorizationToken}.
	 *
	 * @param authorizationToken
	 *        The String to set.
	 */
	public void setAuthorizationToken (final String authorizationToken)
	{
		this.authorizationToken = authorizationToken;
	}

	/**
	 * Answer the {@link AuthenticationContext#authorizationToken} for this
	 * {@link APIRequest} given {@link #usesAuthorizationToken()} evaluates to
	 * {@link true}.
	 *
	 * @return A String.
	 */
	public String authorizationToken ()
	{
		return authorizationToken;
	}

	/**
	 * Does this {@link APIRequest} download files?
	 *
	 * @return {@code true} indicates yes; {@code false} otherwise.
	 */
	public boolean isDownloadRequest ()
	{
		// Default to false as almost all the API will not result in downloading
		// data files
		return false;
	}

	/**
	 * Does this {@link APIRequest} only work when the application is {@link
	 * ApplicationState#AUTHENTICATED}?
	 *
	 * @return {@code true} indicates yes; {@code false} otherwise.
	 */
	public boolean isAuthenticatedRequest ()
	{
		// Default to true as almost all the API will require an authenticated
		// state
		return true;
	}

	/**
	 * Answer the {@link B2File} to download.
	 */
	public B2File file()
	{
		// Any call of this implementation is an error and should be reported
		// as a bug.
		if (!isDownloadRequest())
		{
			new UnsupportedOperationException(
				"Bug please report: This is not a download request and should "
					+ "not have been called!")
				.printStackTrace();
		}
		else
		{
			new UnsupportedOperationException(
				"Bug please report: This is a download request but this method "
					+ "has not yet been implemented!")
				.printStackTrace();
		}
		ExitCode.UNEXPECTED_EXCEPTION.shutdown();
		return null; // The application will never get here
	}

	/**
	 * Answer the location the file should be written to.
	 */
	public String outputPath ()
	{
		// Any call of this implementation is an error and should be reported
		// as a bug.
		if (!isDownloadRequest())
		{
			new UnsupportedOperationException(
				"Bug please report: This is not a download request and should "
					+ "not have been called!")
				.printStackTrace();
		}
		else
		{
			new UnsupportedOperationException(
				"Bug please report: This is a download request but this method "
					+ "has not yet been implemented!")
				.printStackTrace();
		}
		ExitCode.UNEXPECTED_EXCEPTION.shutdown();
		return null; // The application will never get here
	}

	/**
	 * A {@link Consumer} that accepts a {@link JSONObject} that is used to
	 * build the {@link Response}.
	 *
	 * @return A {@code Consumer}.
	 */
	public final Consumer<JSONObject> contentConsumer ()
	{
		return jsonObject ->
			responseConsumer.accept(createResponse(jsonObject));
	}

	@Override
	public String toString ()
	{
		return String.format("APIRequest{version: %s}", apiVersion());
	}

	/**
	 * Construct a {@link APIRequest}.
	 *
	 * @param responseConsumer
	 *        The {@link Consumer} that accepts a {@code Response} to the
	 *        request.
	 * @param failureContinuation
	 *        The {@link Consumer} that accepts an {@link ApplicationException}
	 *        to call in the event of a managed failure.
	 */
	protected APIRequest (
		final Consumer<Response> responseConsumer,
		final Consumer<ApplicationException> failureContinuation)
	{
		this.responseConsumer = responseConsumer;
		this.failureContinuation = failureContinuation;
		checkCompatibility();
	}
}
