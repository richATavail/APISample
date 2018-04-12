/*
 * B2AuthorizeAccountRequest.java
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

package org.availlang.raa.api.b2api;
import com.avail.utility.json.JSONObject;
import com.avail.utility.json.JSONWriter;
import org.availlang.raa.ApplicationRuntime;
import org.availlang.raa.ApplicationRuntime.ExitCode;
import org.availlang.raa.ApplicationState;
import org.availlang.raa.api.APICatalogue;
import org.availlang.raa.api.APIGroup;
import org.availlang.raa.api.APIRequest;
import org.availlang.raa.api.AuthenticationContext;
import org.availlang.raa.exceptions.ApplicationException;
import org.availlang.raa.exceptions.CompatibilityException;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A {@code B2AuthorizeAccountRequest} is an {@link APIRequest} used to
 * login to the B2 API.
 *
 * @see <a href="https://www.backblaze.com/b2/docs/b2_authorize_account.html">
 *     authenticate account</a>
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
public class B2AuthorizeAccountRequest
extends APIRequest<B2AuthorizeAccountResponse>
{
	@Override
	public boolean isAuthenticatedRequest () { return false; }

	@Override
	public B2AuthorizeAccountResponse createResponse (final JSONObject content)
	{
		return new B2AuthorizeAccountResponse(content);
	}

	private static Set<ApplicationState> validSendStates =
		new HashSet<>(Arrays.asList(
			ApplicationState.AUTHENTICATED,
			ApplicationState.ACCOUNT_INITIALIZED));

	@Override
	public Set<ApplicationState> validSendStates ()
	{
		return validSendStates;
	}

	@Override
	public APICatalogue catalogue ()
	{
		return APICatalogue.B2_AUTHORIZE_ACCOUNT;
	}

	@Override
	public String apiVersion ()
	{
		return "v1";
	}

	@Override
	public String apiOperationName ()
	{
		return "b2_authorize_account";
	}

	@Override
	public APIGroup apiGroup ()
	{
		return B2APIGroup.soleInstance;
	}

	@Override
	public void writeTo (final JSONWriter writer) { /* Do nothing */ }

	@Override
	public String baseClientLocationIdentifier ()
	{
		return apiGroup().baseClientLocationIdentifier();
	}

	@Override
	protected void checkCompatibility ()
	{
		if (!B2AuthorizeAccountResponse.compatibleApiVersions()
			.contains(apiVersion()))
		{
			// A mismatch in API means the application cannot be trusted to
			// function properly.
			new CompatibilityException(
					this,
					B2AuthorizeAccountResponse.class,
					B2AuthorizeAccountResponse.compatibleApiVersions())
				.printStackTrace();
			ExitCode.API_MISMATCH.shutdown();
		}
	}

	/**
	 * Authorize access to the B2 account.
	 *
	 * @param acctId
	 *        The {@link AuthenticationContext#accountId()}.
	 * @param appKey
	 *        The {@link AuthenticationContext#applicationKey()}.
	 * @see <a href="https://www.backblaze.com/b2/docs/b2_authorize_account.html">
	 *     authenticate account</a>
	 */
	public static void authenticate (
		final String acctId,
		final String appKey)
	{
		final Consumer<ApplicationException> failureContinuation = ex ->
		{
			ex.printStackTrace();
			ex.exitCode.shutdown();
		};
		final B2AuthorizeAccountRequest request =
			new B2AuthorizeAccountRequest(
				acctId,
				appKey,
				response ->
				{
					//noinspection Convert2MethodRef
					AuthenticationContext.setAuthorizationData(
						response.authorizationToken(),
						response.downloadUrl(),
						response.apiUrl(),
						response.timeUntilExpires());
				},
				failureContinuation);
		ApplicationRuntime.client().processRequest(request);
	}

	/**
	 * Construct a new {@link B2AuthorizeAccountRequest}.
	 *
	 * <p>
	 * This is a private constructor as there should only be one {@link
	 * B2AuthorizeAccountResponse} with a valid {@linkplain
	 * B2AuthorizeAccountResponse#authorizationToken()} available at any time.
	 * </p>
	 *
	 * @param accountId
	 *        The B2 account number.
	 * @param applicationKey
	 *        The B2 application key.
	 * @param responseConsumer
	 *        The {@link Consumer} that accepts a {@code Response} to the
	 *        request.
	 * @param failureContinuation
	 *        The {@link Consumer} that accepts an {@link ApplicationException}
	 *        to call in the event of a managed failure.
	 * @see <a href="https://www.backblaze.com/b2/docs/b2_authorize_account.html">
	 *     authenticate account</a>
	 */
	private B2AuthorizeAccountRequest (
		final String accountId,
		final String applicationKey,
		final Consumer<B2AuthorizeAccountResponse> responseConsumer,
		final Consumer<ApplicationException> failureContinuation)
	{
		super(responseConsumer, failureContinuation);
		setAuthorizationToken(
			String.format(
				"Basic %s",
				Base64.getEncoder().encodeToString(
					(accountId + ":" + applicationKey).getBytes())));
	}
}
