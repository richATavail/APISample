/*
 * B2ListBucketsRequest.java
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
 * POSSIBILITY OF SUCH DAMAGE..
 */

package org.availlang.raa.api.b2api;
import com.avail.utility.json.JSONObject;
import com.avail.utility.json.JSONWriter;
import org.availlang.raa.ApplicationRuntime.ExitCode;
import org.availlang.raa.ApplicationState;
import org.availlang.raa.B2File;
import org.availlang.raa.api.APICatalogue;
import org.availlang.raa.api.APIGroup;
import org.availlang.raa.api.APIRequest;
import org.availlang.raa.exceptions.ApplicationException;
import org.availlang.raa.exceptions.CompatibilityException;

import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A {@code B2DownloadFileByIdRequest} is an {@link APIRequest} for downloading
 * a {@link B2File}.
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 * @see <a href="https://www.backblaze.com/b2/docs/b2_download_file_by_id.html">
 *     List B2 Buckets</a>
 */
public class B2DownloadFileByIdRequest
extends APIRequest<B2DownloadFileResponse>
{
	/**
	 * The {@link B2File} to download.
	 */
	private final B2File file;

	@Override
	public B2File file ()
	{
		return file;
	}

	/**
	 * The location the file should be written to.
	 */
	private final String outputPath;

	@Override
	public String outputPath ()
	{
		return outputPath;
	}

	@Override
	public Set<ApplicationState> validSendStates ()
	{
		return Collections.singleton(ApplicationState.AUTHENTICATED);
	}

	@Override
	public String apiOperationName ()
	{
		return "b2_download_file_by_id";
	}



	@Override
	public B2DownloadFileResponse createResponse (final JSONObject content)
	{
		return new B2DownloadFileResponse(content);
	}

	@Override
	public APICatalogue catalogue ()
	{
		return APICatalogue.B2_DOWNLOAD_FILE_BY_ID;
	}

	@Override
	public String apiVersion ()
	{
		return "v1";
	}

	@Override
	public APIGroup apiGroup ()
	{
		return B2APIGroup.soleInstance;
	}

	@Override
	public boolean isDownloadRequest ()
	{
		return true;
	}

	@Override
	protected void checkCompatibility ()
	{
		if (!B2ListBucketsResponse.compatibleApiVersions()
			.contains(apiVersion()))
		{
			// A mismatch in API means the application cannot be trusted to
			// function properly.
			new CompatibilityException(
					this,
					B2DownloadFileResponse.class,
					B2DownloadFileResponse.compatibleApiVersions())
				.printStackTrace();
			ExitCode.API_MISMATCH.shutdown();
		}
	}

	@Override
	public void writeTo (final JSONWriter writer)
	{
		writer.startObject();
		writer.write("fileId");
		writer.write(file.fileId);
		writer.endObject();

	}

	@Override
	public boolean isAuthenticatedRequest ()
	{
		return true;
	}

	/**
	 * Construct a {@link B2BucketListFileNamesRequest}.
	 *
	 * @param file
	 *        The {@link B2File} to download.
	 * @param outputPath
	 *        The location the file should be written to.
	 * @param responseConsumer
	 *        A {@link Consumer} that accepts a {@code Response} to the request.
	 * @param failureContinuation
	 *        The {@link Consumer} that accepts an {@link ApplicationException}
	 *        to call in the event of a managed failure.
	 */
	public B2DownloadFileByIdRequest (
		final B2File file,
		final String outputPath,
		final Consumer<B2DownloadFileResponse> responseConsumer,
		final Consumer<ApplicationException> failureContinuation)
	{
		super(responseConsumer, failureContinuation);
		this.file = file;
		this.outputPath = outputPath;
	}
}
