/*
 * InitializationTest.java
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
import org.availlang.raa.ApplicationRuntime;
import org.availlang.raa.ApplicationState;
import org.availlang.raa.B2Bucket;
import org.availlang.raa.B2File;
import org.availlang.raa.api.AuthenticationContext;
import org.availlang.raa.utilities.PropertiesManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A {@code ApplicationTest} is a set of JUnit tests for testing the startup
 * components of the application as well as the API.
 *
 * <p>
 * <strong>NOTE:</strong> Running this test will delete the properties file if
 * one exists.
 * </p>
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
class ApplicationTest
{
	@BeforeAll
	static void clearSystem () throws IOException
	{
		Files.deleteIfExists(Paths.get("config/app.properties"));
	}

	@Test
	@DisplayName("Set initial account information")
	void initializeApplication()
	{
		assertEquals(
			ApplicationState.APPLICATION_UNINITIALIZED,
			ApplicationRuntime.state());
		ApplicationRuntime.initialize(
			new TestClient(),
			B2AuthorizeAccountRequest::authenticate);
		assertEquals(
			ApplicationState.ACCOUNT_UNINITIALIZED, ApplicationRuntime.state());
		assertFalse(PropertiesManager.propertiesFileExists());
		assertTrue(AuthenticationContext.accountId().isEmpty());
		assertTrue(AuthenticationContext.applicationKey().isEmpty());
		PropertiesManager.updateAccountInfo("abc", "def");
		assertTrue(PropertiesManager.propertiesFileExists());
		PropertiesManager.retrieveAccountInfo();
		assertEquals(
			ApplicationState.ACCOUNT_INITIALIZED, ApplicationRuntime.state());
		assertEquals("abc", AuthenticationContext.accountId());
		assertEquals("def", AuthenticationContext.applicationKey());
		assertNotEquals("bogus", AuthenticationContext.accountId());
		assertNotEquals("bogus", AuthenticationContext.applicationKey());
		PropertiesManager.updateAccountInfo("id", "key");
		PropertiesManager.retrieveAccountInfo();
		assertEquals("id", AuthenticationContext.accountId());
		assertEquals("key", AuthenticationContext.applicationKey());
	}

	@Test
	@DisplayName("Test API requests")
	void apiExchange()
	{
		final String accountId = "A1234";
		final String applicationKey = "K123456";
		final List<B2Bucket> bucketList = new ArrayList<>();
		final B2Bucket b1 = new B2Bucket("b1", "bucket-1");
		final B2Bucket b2 = new B2Bucket("b2", "bucket-2");
		bucketList.add(b1);
		bucketList.add(b2);
		final B2File f1b1 = new B2File("b1 file 1", "f1b1");
		final B2File f1b2 = new B2File("b1 file 2", "f1b2");
		final B2File f1b3 = new B2File("b1 file 3", "f1b3");
		final B2File f2b1 = new B2File("b2 file 1", "f2b1");
		final B2File f2b2 = new B2File("b2 file 2", "f2b2");
		final Map<String, List<B2File>> bucketMap = new HashMap<>();
		bucketMap.put(b1.bucketName, Arrays.asList(f1b1, f1b2, f1b3));
		bucketMap.put(b2.bucketName,Arrays.asList(f2b1, f2b2));
		final String apiUrl = "api-url";
		final String downloadUrl = "download-url";
		final TestClient client = new TestClient(
			apiUrl, downloadUrl, bucketList, bucketMap);
		client.setexpectedAuthToken(accountId, applicationKey);
		ApplicationRuntime.initialize(
			client, B2AuthorizeAccountRequest::authenticate);

		assertEquals(
			ApplicationState.ACCOUNT_UNINITIALIZED, ApplicationRuntime.state());
		assertTrue(AuthenticationContext.accountId().isEmpty());
		assertTrue(AuthenticationContext.applicationKey().isEmpty());
		PropertiesManager.updateAccountInfo(accountId, applicationKey);
		assertTrue(PropertiesManager.propertiesFileExists());
		PropertiesManager.retrieveAccountInfo();
		assertEquals(
			ApplicationState.ACCOUNT_INITIALIZED, ApplicationRuntime.state());
		assertNotEquals("bogus", AuthenticationContext.accountId());
		assertNotEquals("bogus", AuthenticationContext.applicationKey());
		assertEquals(accountId, AuthenticationContext.accountId());
		assertEquals(applicationKey, AuthenticationContext.applicationKey());
		AuthenticationContext.authenticate();
		assertEquals(ApplicationState.AUTHENTICATED, ApplicationRuntime.state());

		// Force change to the state to make sure the requests enter the
		// waiting queue
		ApplicationRuntime.setState(ApplicationState.AUTHENTICATING);
		client.checkBaseClientLocationIdentifier = true;
		final B2ListBucketsRequest bucketsRequest = new B2ListBucketsRequest(
			response ->
			{
				final Set<String> bucketNames = response.buckets().stream()
					.map(b -> b.bucketId).collect(Collectors.toSet());
				assertEquals(bucketMap.keySet(), bucketNames);
			},
			ex -> {throw ex;});
		final B2BucketListFileNamesRequest fileList =
			new B2BucketListFileNamesRequest(b1,
				response ->
				{
					final Set<String> fileNames = response.files().stream()
						.map(f -> f.fileId).collect(Collectors.toSet());
					final Set<String> expected = bucketMap.get(b1.bucketName)
						.stream().map(f -> f.fileId).collect(Collectors.toSet());
					assertEquals(expected, fileNames);
				},
				ex -> {throw ex;});
		final B2DownloadFileByIdRequest downloadRequest =
			new B2DownloadFileByIdRequest(
				f1b2,
				"path-anywhere,",
				response ->
				{
					// Do nothing: if it got here, it passed.
				},
				ex -> {throw ex;});

		// These next three requests should end up in a waiting queue due to
		// the AUTHENTICATING state being active.
		AuthenticationContext.processRequest(bucketsRequest);
		AuthenticationContext.processRequest(fileList);
		AuthenticationContext.processRequest(downloadRequest);
		assertEquals(3, AuthenticationContext.queueCount());

		// This will trigger the app to re-authenticate and pass the requests
		// on to the Client.
		AuthenticationContext.authenticate();
		assertEquals(ApplicationState.AUTHENTICATED, ApplicationRuntime.state());
		assertEquals(0, AuthenticationContext.queueCount());
	}

	@AfterEach
	void cleanup () throws IOException
	{
		Files.deleteIfExists(Paths.get("config/app.properties"));
		ApplicationRuntime.uninitializeApplication();
		AuthenticationContext.cleanUp();
	}
}
