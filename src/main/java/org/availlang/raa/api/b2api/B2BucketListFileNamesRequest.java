/*
 * B2BucketListFileNamesRequest.java
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
import org.availlang.raa.B2Bucket;
import org.availlang.raa.B2File;
import org.availlang.raa.api.APICatalogue;
import org.availlang.raa.api.APIGroup;
import org.availlang.raa.api.APIRequest;
import org.availlang.raa.exceptions.ApplicationException;
import org.availlang.raa.exceptions.CompatibilityException;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Set;
import java.util.function.Consumer;

/**
 * A {@code B2BucketListFileNamesRequest} is an {@link APIRequest} for
 * retrieving the list of files for a particular {@link B2Bucket}.
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 * @see <a href="https://www.backblaze.com/b2/docs/b2_list_file_names.html">
 *     List B2 Bucket Files</a>
 */
public class B2BucketListFileNamesRequest
extends APIRequest<B2BucketListFileNamesResponse>
{
	/**
	 * The {@link B2Bucket} to retrieve {@link B2File}s for.
	 */
	public final B2Bucket chosenBucket;

	/**
	 * The maximum number of {@link B2File}s to retrieve with this
	 * request.
	 *
	 * <p>
	 * <em>Note:</em> The maximum is 100 files at a time. This probably should
	 * be a customizable setting as it maybe more useful to be able to set this
	 * for a more versatile client than a console tool.
	 * </p>
	 *
	 * @return An {@code integer}.
	 */
	public static int MAX_BATCH_FILE_COUNT = 10;

	@Override
	public String apiOperationName ()
	{
		return "b2_list_file_names";
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
	protected void checkCompatibility ()
	{
		if (!B2BucketListFileNamesResponse.compatibleApiVersions()
			.contains(apiVersion()))
		{
			// A mismatch in API means the application cannot be trusted to
			// function properly.
			new CompatibilityException(
					this,
					B2BucketListFileNamesResponse.class,
					B2BucketListFileNamesResponse.compatibleApiVersions())
				.printStackTrace();
			ExitCode.API_MISMATCH.shutdown();
		}
	}

	@Override
	public B2BucketListFileNamesResponse createResponse (
		final JSONObject content)
	{
		return new B2BucketListFileNamesResponse(content);
	}

	@Override
	public APICatalogue catalogue ()
	{
		return APICatalogue.B2_LIST_FILE_NAMES;
	}

	@Override
	public Set<ApplicationState> validSendStates ()
	{
		return Collections.singleton(ApplicationState.AUTHENTICATED);
	}

	/**
	 * The name of the next file to continue downloading from or {@code null}
	 * if none exists.
	 */
	private @Nullable String nextStartFileName = null;

	@Override
	public void writeTo (final JSONWriter writer)
	{
		writer.startObject();
		writer.write("bucketId");
		writer.write(chosenBucket.bucketId);
		writer.write("maxFileCount");
		writer.write(MAX_BATCH_FILE_COUNT);
		if (nextStartFileName != null)
		{
			writer.write("startFileName");
			writer.write(nextStartFileName);
		}
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
	 * @param chosenBucket
	 *        The {@link B2Bucket} to retrieve {@link B2File}s for.
	 * @param responseConsumer
	 *        A {@link Consumer} that accepts a {@code Response} to the request.
	 * @param failureContinuation
	 *        The {@link Consumer} that accepts an {@link ApplicationException}
	 *        to call in the event of a managed failure.
	 */
	public B2BucketListFileNamesRequest (
		final B2Bucket chosenBucket,
		final Consumer<B2BucketListFileNamesResponse> responseConsumer,
		final Consumer<ApplicationException> failureContinuation)
	{
		super(responseConsumer, failureContinuation);
		this.chosenBucket = chosenBucket;
	}

	/**
	 * Construct a {@link B2BucketListFileNamesRequest}.
	 *
	 * @param chosenBucket
	 *        The {@link B2Bucket} to retrieve {@link B2File}s for.
	 * @param nextStartFileName
	 *        The name of the next file to continue downloading from or {@code
	 *        null} if none exists.
	 * @param responseConsumer
	 *        A {@link Consumer} that accepts a {@code Response} to the request.
	 * @param failureContinuation
	 *        The {@link Consumer} that accepts an {@link ApplicationException}
	 *        to call in the event of a managed failure.
	 */
	public B2BucketListFileNamesRequest (
		final B2Bucket chosenBucket,
		final String nextStartFileName,
		final Consumer<B2BucketListFileNamesResponse> responseConsumer,
		final Consumer<ApplicationException> failureContinuation)
	{
		super(responseConsumer, failureContinuation);
		this.chosenBucket = chosenBucket;
		this.nextStartFileName = nextStartFileName;
	}
}
