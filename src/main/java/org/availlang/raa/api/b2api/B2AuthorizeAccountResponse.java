/*
 * B2AuthorizeAccountResponse.java
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
import com.avail.utility.json.JSONException;
import com.avail.utility.json.JSONObject;
import org.availlang.raa.ApplicationRuntime.ExitCode;
import org.availlang.raa.api.APIResponse;

import java.util.Collections;
import java.util.Set;

/**
 * A {@code B2AuthorizeAccountResponse} is an {@link APIResponse} that is the
 * result of a {@link B2AuthorizeAccountRequest}.
 *
 * <p>
 * <em>NOTE:</em> Unlike the other response classes, I created accessor methods
 * for all the state returned in the JSON payload by the server. The intent
 * is to allow for extension of the application by implementing other API. This
 * request is important to all the other APIs. Since I built this to be the
 * a "framework", I figured each field probably has a purpose relative to the
 * other APIs, so I added accessors for completeness. I did not do that for all
 * the other APIs in this application.
 * </p>
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
class B2AuthorizeAccountResponse
extends APIResponse
{
	/**
	 * The amount of time in milliseconds the {@link #authorizationToken()}
	 * is expected to be good for. The API documentation indicates the key is
	 * valid for 24 hours. To provide a buffer between actual expiration and
	 * final use, the valid window is set to 23 hours and 50 minutes.
	 *
	 * @return A {@code long}.
	 */
	public long timeUntilExpires ()
	{
		return 85800000;
	}

	/**
	 * Answer the {@link Set} of Strings that represent the {@link
	 * B2AuthorizeAccountRequest}s this {@link B2AuthorizeAccountResponse}
	 * is compatible with.
	 *
	 * @return A {@code Set} of Strings.
	 */
	public static Set<String> compatibleApiVersions ()
	{
		return Collections.singleton("v1");
	}

	/**
	 * Answer the recommended size for each part of a large file. It is
	 * recommend using this part size for optimal upload performance.
	 *
	 * @return A {@code long}.
	 */
	@SuppressWarnings("unused")
	long recommendedPartSize ()
	{
		return content().getNumber("recommendedPartSize").getLong();
	}

	/**
	 * Answer the smallest possible size of a part of a large file (except the
	 * last one). This is smaller than the {@link #recommendedPartSize()}. Using
	 * it may result in taking longer overall to upload a large file.
	 *
	 * @return A {@code long}.
	 */
	@SuppressWarnings("unused")
	long absoluteMinimumPartSize ()
	{
		return content().getNumber("absoluteMinimumPartSize").getLong();
	}

	/**
	 * Answer the identifier for the account.
	 *
	 * @return A String.
	 */
	@SuppressWarnings("unused")
	String accountId ()
	{
		return content().getString("accountId");
	}

	/**
	 * Answer the authorization token to use with all calls, other than
	 * b2_authorize_account, that need an Authorization header. This
	 * authorization token is valid for at most 24 hours.
	 *
	 * @return A String.
	 */
	public String authorizationToken ()
	{
		return content().getString("authorizationToken");
	}

	/**
	 * Answer the String url that is to be used for future downloads.
	 *
	 * @return A {@code String}.
	 */
	public String downloadUrl ()
	{
		return content().getString("downloadUrl");
	}

	/**
	 * Answer the String to make authenticated API calls to.
	 *
	 * @return A {@code String}.
	 */
	public String apiUrl ()
	{
		return content().getString("apiUrl");
	}

	/**
	 * Construct a {@link B2AuthorizeAccountResponse}.
	 *
	 * @param content
	 *        The {@link JSONObject} that represents the raw content of the
	 *        {@link APIResponse}.
	 */
	B2AuthorizeAccountResponse (
		final JSONObject content)
	{
		super(content);
		try
		{
			// Ensure has authorization token and download url
			authorizationToken();
			downloadUrl();
			apiUrl();
		}
		catch (final JSONException e)
		{
			System.err.println("Unexpected API mismatch");
			e.printStackTrace();
			ExitCode.API_MISMATCH.shutdown();
		}
	}
}
