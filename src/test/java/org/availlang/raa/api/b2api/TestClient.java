/*
 * TestClient.java
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
import com.avail.utility.json.JSONReader;
import com.avail.utility.json.JSONWriter;
import org.availlang.raa.ApplicationRuntime.ExitCode;
import org.availlang.raa.B2Bucket;
import org.availlang.raa.B2File;
import org.availlang.raa.api.APIRequest;
import org.availlang.raa.api.APIResponse;
import org.availlang.raa.api.AuthenticationContext;
import org.availlang.raa.client.Client;
import org.availlang.raa.exceptions.ApplicationException;
import org.junit.jupiter.api.Assertions;

import java.io.StringReader;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A {@code TestClient} is an implementation of {@link Client} for unit testing.
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
public class TestClient
implements Client
{
	/**
	 * The simulated {@link B2Bucket}s.
	 */
	public Map<String, B2Bucket> buckets = Collections.emptyMap();

	/**
	 * The simulated {@linkplain B2File files} contained in the {@link
	 * B2Bucket}s in {@link #buckets}.
	 */
	public Map<String, List<B2File>> bucketMap = Collections.emptyMap();

	/**
	 * The expected {@link AuthenticationContext#authorizationToken} to use.
	 */
	public String expectedAuthToken = "";

	/**
	 * The expected {@link AuthenticationContext#apiUrl} to use.
	 */
	public String expectedApiUrl = "";

	/**
	 * The expected {@link AuthenticationContext#downloadUrl} to use.
	 */
	public String expectedDownloadUrl = "";

	/**
	 * Should the client check to ensure the correct target location is set in
	 * the request?
	 */
	public boolean checkBaseClientLocationIdentifier = false;

	/**
	 * Set the {@link #expectedAuthToken}.
	 *
	 * @param accountId
	 *        The expected {@link AuthenticationContext#accountId}.
	 * @param appKey
	 *        The expected {@link AuthenticationContext#applicationKey}.
	 */
	public void setexpectedAuthToken (
		final String accountId, final String appKey)
	{
		this.expectedAuthToken = String.format(
			"Basic %s",
			Base64.getEncoder().encodeToString(
				(accountId + ":" + appKey).getBytes()));
	}

	/**
	 * The {@link Set} of {@link B2Bucket} names expected to be received via a
	 * test action.
	 */
	public Set<String> expectedBucketNames = Collections.emptySet();

	/**
	 * The {@link Set} of {@link B2File} names expected to be received via a
	 * test action.
	 */
	public Set<String> expectedChosenFileNames = Collections.emptySet();

	/**
	 * Answer an {@link ApplicationException}.
	 *
	 * @param exitCode
	 *        The {@link ExitCode} to claim was recieved.
	 * @param message
	 *        The failure message.
	 * @return An {@code ApplicationException}.
	 */
	private ApplicationException ex (
		final ExitCode exitCode,
		final String message)
	{
		return new ApplicationException(exitCode, message);
	}

	@Override
	public <Response extends APIResponse> void processRequest (
		final APIRequest<Response> request)
	{
		switch (request.catalogue())
		{
			case B2_AUTHORIZE_ACCOUNT:
			{
				final B2AuthorizeAccountRequest r =
					(B2AuthorizeAccountRequest) request;
				if (r.authorizationToken().equals(expectedAuthToken))
				{
					final JSONWriter writer = new JSONWriter();
					writer.startObject();
					writer.write("authorizationToken");
					writer.write(r.authorizationToken());
					writer.write("downloadUrl");
					writer.write(expectedDownloadUrl);
					writer.write("apiUrl");
					writer.write(expectedApiUrl);
					writer.endObject();
					r.contentConsumer().accept(
						(JSONObject) new JSONReader(
							new StringReader(writer.toString())).read());
					if (checkBaseClientLocationIdentifier)
					{
						Assertions.assertEquals(
							B2APIGroup.soleInstance
								.baseClientLocationIdentifier(),
							request.baseClientLocationIdentifier());
					}
				}
				else
				{
					r.failureContinuation().accept(
						ex(
							ExitCode.AUTHENTICATION_FAILURE,
							"Failed login!"));
				}
				break;
			}
			case B2_LIST_BUCKETS:
			{
				final B2ListBucketsRequest r =
					(B2ListBucketsRequest) request;
				if (r.authorizationToken().equals(expectedAuthToken))
				{
					final JSONWriter writer = new JSONWriter();
					writer.startObject();
					writer.write("buckets");
					writer.startArray();
					buckets.values().forEach(b ->
					{
						writer.startObject();
						writer.write("bucketId");
						writer.write(b.bucketName);
						writer.write("bucketName");
						writer.write(b.bucketId);
						writer.endObject();
					});
					writer.endArray();
					writer.endObject();
					r.contentConsumer().accept(
						(JSONObject) new JSONReader(
							new StringReader(writer.toString())).read());
					if (checkBaseClientLocationIdentifier)
					{
						Assertions.assertEquals(
							expectedApiUrl,
							request.baseClientLocationIdentifier());
					}
				}
				else
				{
					r.failureContinuation().accept(
						ex(
							ExitCode.AUTHENTICATION_FAILURE,
							"Bad auth token!"));
				}
				break;
			}
			case B2_LIST_FILE_NAMES:
			{
				final B2BucketListFileNamesRequest r =
					(B2BucketListFileNamesRequest) request;
				if (r.authorizationToken().equals(expectedAuthToken))
				{
					final JSONWriter writer = new JSONWriter();
					writer.startObject();
					writer.write("files");
					writer.startArray();
					bucketMap.get(r.chosenBucket.bucketName).forEach(f ->
					{
						writer.startObject();
						writer.write("fileName");
						writer.write(f.fileName);
						writer.write("fileId");
						writer.write(f.fileId);
						writer.endObject();
					});
					writer.endArray();
					writer.write("nextFileName");
					writer.writeNull();
					writer.endObject();
					r.contentConsumer().accept(
						(JSONObject) new JSONReader(
							new StringReader(writer.toString())).read());
					if (checkBaseClientLocationIdentifier)
					{
						Assertions.assertEquals(
							expectedApiUrl,
							request.baseClientLocationIdentifier());
					}
				}
				else
				{
					r.failureContinuation().accept(
						ex(
							ExitCode.AUTHENTICATION_FAILURE,
							"Bad auth token!"));
				}
				break;
			}
			case B2_DOWNLOAD_FILE_BY_ID:
			{
				// Don't need to actually write file to disk.
				final B2DownloadFileByIdRequest r =
					(B2DownloadFileByIdRequest) request;
				if (r.authorizationToken().equals(expectedAuthToken))
				{
					r.contentConsumer().accept(
						(JSONObject) new JSONReader(
							new StringReader("{}")).read());
					if (checkBaseClientLocationIdentifier)
					{
						Assertions.assertEquals(
							expectedDownloadUrl,
							request.baseClientLocationIdentifier());
					}
				}
				else
				{
					r.failureContinuation().accept(
						ex(
							ExitCode.AUTHENTICATION_FAILURE,
							"Bad auth token!"));
				}
				break;
			}
		}
	}

	/**
	 * Construct a {@link TestClient}.
	 *
	 * @param expectedApiUrl
	 *        The expected {@link AuthenticationContext#apiUrl} to use.
	 * @param expectedDownloadUrl
	 *        The expected {@link AuthenticationContext#downloadUrl} to use.
	 * @param bucketList
	 *        The simulated {@link B2Bucket}s.
	 * @param bucketMap
	 *        The simulated {@linkplain B2File files} contained in the {@link
	 *        B2Bucket}s in {@link #buckets}.
	 */
	TestClient (
		final String expectedApiUrl,
		final String expectedDownloadUrl,
		final List<B2Bucket> bucketList,
		final Map<String, List<B2File>> bucketMap)
	{
		this.buckets = new HashMap<>();
		bucketList.forEach(b -> buckets.put(b.bucketName, b));
		// Check test initialization
		assert buckets.keySet().containsAll(bucketMap.keySet())
			: "Bad test set up, not all buckets mentioned in map are in bucket "
			+ "list";
		this.bucketMap = bucketMap;
		this.expectedApiUrl = expectedApiUrl;
		this.expectedDownloadUrl = expectedDownloadUrl;
	}

	/**
	 * Construct an empty {@link TestClient}.
	 */
	TestClient () { }
}
