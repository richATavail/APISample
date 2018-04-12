/*
 * APICatalogue.java
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

import org.availlang.raa.api.b2api.B2AuthorizeAccountRequest;
import org.availlang.raa.api.b2api.B2BucketListFileNamesRequest;
import org.availlang.raa.api.b2api.B2DownloadFileByIdRequest;
import org.availlang.raa.api.b2api.B2ListBucketsRequest;
import org.availlang.raa.client.http.HTTPProtocolMethod;
import static org.availlang.raa.client.http.HTTPProtocolMethod.*;

/**
 * A {@code APICatalogue} is an enum that contains information about each {@link
 * APIRequest}.
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
public enum APICatalogue
{
	/**
	 * The {@link B2AuthorizeAccountRequest}.
	 */
	B2_AUTHORIZE_ACCOUNT
	{
		@Override
		public HTTPProtocolMethod supportedHTTPMethod () { return GET; }
	},

	/**
	 * The {@link B2ListBucketsRequest}.
	 */
	B2_LIST_BUCKETS
	{
		@Override
		public HTTPProtocolMethod supportedHTTPMethod () { return POST; }
	},

	/**
	 * The {@link B2BucketListFileNamesRequest}.
	 */
	B2_LIST_FILE_NAMES
	{
		@Override
		public HTTPProtocolMethod supportedHTTPMethod () { return POST; }
	},

	/**
	 * The {@link B2DownloadFileByIdRequest}.
	 */
	B2_DOWNLOAD_FILE_BY_ID
	{
		@Override
		public HTTPProtocolMethod supportedHTTPMethod () { return POST; }
	};

	/**
	 * Answer which {@link HTTPProtocolMethod} is used to call the respective
	 * {@link APIRequest}.
	 *
	 * @return An {@code HTTPProtocolMethod}.
	 */
	public abstract HTTPProtocolMethod supportedHTTPMethod ();
}
