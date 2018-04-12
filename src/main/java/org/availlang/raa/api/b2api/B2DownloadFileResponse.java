/*
 * B2ListBucketsResponse.java
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
import org.availlang.raa.api.APIResponse;

import java.util.Collections;
import java.util.Set;

/**
 * A {@code B2ListBucketsResponse} is is an {@link APIResponse} that is the
 * result of a {@link B2ListBucketsRequest}.
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 * @see <a href="https://www.backblaze.com/b2/docs/b2_list_buckets.html">
 *     List B2 Buckets</a>
 */
public class B2DownloadFileResponse
extends APIResponse
{
	/**
	 * Answer the {@link Set} of Strings that represent the {@link
	 * B2ListBucketsRequest}s this {@link B2DownloadFileResponse} is compatible
	 * with.
	 *
	 * @return A {@code Set} of Strings.
	 */
	static Set<String> compatibleApiVersions ()
	{
		return Collections.singleton("v1");
	}

	/**
	 * Construct a new {@link B2DownloadFileResponse}.
	 *
	 * @param content
	 *        The {@link JSONObject} that represents the raw content of the
	 *        {@link APIResponse}.
	 */
	B2DownloadFileResponse (
		final JSONObject content)
	{
		super(content);
	}
}
