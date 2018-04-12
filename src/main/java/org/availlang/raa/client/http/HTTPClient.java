/*
 * HTTPClient.java
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

package org.availlang.raa.client.http;
import com.avail.utility.json.JSONException;
import com.avail.utility.json.JSONObject;
import com.avail.utility.json.JSONReader;
import com.avail.utility.json.JSONWriter;
import org.availlang.raa.ApplicationRuntime.ExitCode;
import org.availlang.raa.api.APIRequest;
import org.availlang.raa.api.APIResponse;
import org.availlang.raa.client.Client;
import org.availlang.raa.exceptions.ApplicationException;
import org.availlang.raa.exceptions.ConnectionException;
import org.availlang.raa.exceptions.DownloadException;
import org.availlang.raa.exceptions.ResponseException;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

/**
 * A {@code HTTPClient} is a {@link Client} that uses the HTTP protocol to
 * communicate with the Backblaze API server.
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
public class HTTPClient
implements Client
{
	/**
	 * Answer the {@link URL} used for making this {@link APIRequest}.
	 *
	 * @param request
	 *        The {@code APIRequest} to process.
	 * @return A String.
	 */
	private String url (final APIRequest<?> request)
	{
		//noinspection StringBufferReplaceableByString
		return new StringBuilder(request.baseClientLocationIdentifier())
			.append('/')
			.append(request.apiGroup().apiGroupLabel())
			.append('/')
			.append(request.apiVersion())
			.append('/')
			.append(request.apiOperationName())
			.toString();
	}

	/**
	 * Create a {@link HttpURLConnection} for the provided {@link APIRequest}.
	 *
	 * @param request
	 *        A {@code APIRequest}.
	 * @param failureContinuation
	 *        The {@link Consumer} that accepts a {@link Throwable} to call in
	 *        the event of a failure.
	 * @return An {@code HttpURLConnection}.
	 */
	private HttpURLConnection createConnection (
		final APIRequest<?> request,
		final Consumer<ApplicationException> failureContinuation)
	{
		final HTTPProtocolMethod method =
			request.catalogue().supportedHTTPMethod();
		switch (method)
		{
			case GET:
				return createHTTPGetConnection(request, failureContinuation);
			case POST:
				return createHTTPPostConnection(request, failureContinuation);
			default:
				new UnsupportedOperationException(
						"HTTP " + method.name() + " is not supported")
					.printStackTrace();
				ExitCode.UNEXPECTED_EXCEPTION.shutdown();
				return null;
		}
	}

	/**
	 * Create a {@link HttpURLConnection} for the provided {@link
	 * HTTPProtocolMethod#GET} {@link APIRequest}.
	 *
	 * @param request
	 *        A {@code APIRequest}.
	 * @param failureContinuation
	 *        The {@link Consumer} that accepts a {@link Throwable} to call in
	 *        the event of a failure.
	 * @return An {@code HttpURLConnection}.
	 */
	private HttpURLConnection createHTTPGetConnection (
		final APIRequest<?> request,
		final Consumer<ApplicationException> failureContinuation)
	{
		HttpURLConnection connection = null;
		try
		{
			final URL url = new URL(url(request));
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");
			if (request.usesAuthorizationToken())
			{
				connection.setRequestProperty(
					"Authorization",
					request.authorizationToken());
			}
			return connection;
		}
		catch (final UnknownHostException e)
		{
			System.err.println("Could not connect to server");
			e.printStackTrace();
			if (connection != null)
			{
				connection.disconnect();
			}
			ExitCode.COULD_NOT_CONNECT.shutdown();
		}
		catch (final IOException | IllegalArgumentException
			| ResponseException | JSONException e)
		{
			failureContinuation.accept(
				new ConnectionException("Unexpected Error", e));
		}
		return null; // Will never get here, but needed to make Java happy.
	}

	/**
	 * Create a {@link HttpURLConnection} for the provided {@link
	 * HTTPProtocolMethod#POST} {@link APIRequest}.
	 *
	 * @param request
	 *        A {@code APIRequest}.
	 * @param failureContinuation
	 *        The {@link Consumer} that accepts a {@link Throwable} to call in
	 *        the event of a failure.
	 * @return An {@code HttpURLConnection}.
	 */
	private HttpURLConnection createHTTPPostConnection (
		final APIRequest<?> request,
		final Consumer<ApplicationException> failureContinuation)
	{
		HttpURLConnection connection = null;
		try
		{
			final JSONWriter writer = new JSONWriter();
			request.writeTo(writer);
			byte postData[] = writer
				.toString()
				.getBytes(StandardCharsets.UTF_8);
			final URL url = new URL(url(request));
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("POST");
			if (request.usesAuthorizationToken())
			{
				connection.setRequestProperty(
					"Authorization",
					request.authorizationToken());
			}
			connection.setRequestProperty(
				"Content-Type", "application/x-www-form-urlencoded");
			connection.setRequestProperty("Charset", "UTF-8");
			connection.setRequestProperty(
				"Content-Length", Integer.toString(postData.length));
			connection.setDoOutput(true);
			DataOutputStream outputStream =
				new DataOutputStream(connection.getOutputStream());
			outputStream.write(postData);
			return connection;
		}catch (final UnknownHostException e)
		{
			System.err.println("Could not connect to server");
			e.printStackTrace();
			if (connection != null)
			{
				connection.disconnect();
			}
			ExitCode.COULD_NOT_CONNECT.shutdown();
		}
		catch (final IOException e)
		{
			failureContinuation.accept(
				new ConnectionException("Unexpected Error", e));
		}
		return null; // Will never get here, but needed to make Java happy.
	}

	@Override
	public <Response extends APIResponse> void processRequest (
		final APIRequest<Response> request)
	{
		if (request.isDownloadRequest())
		{
			processDownloadRequest(request);
		}
		else
		{
			processRegularRequest(request);
		}
	}


	private <Response extends APIResponse> void processRegularRequest (
		final APIRequest<Response> request)
	{
		final Consumer<JSONObject> contentConsumer = request.contentConsumer();
		final Consumer<ApplicationException> failureContinuation =
			request.failureContinuation();
		HttpURLConnection connection = null;
		try
		{
			connection = createConnection(request, failureContinuation);
			final int code = connection.getResponseCode();
			final InputStream in = code != 200
				? new BufferedInputStream(connection.getErrorStream())
				: new BufferedInputStream(connection.getInputStream());
			final StringBuilder sb = new StringBuilder();
			try (final InputStreamReader reader = new InputStreamReader(in))
			{
				int c = reader.read();
				while (c != -1)
				{
					sb.append((char) c);
					c = reader.read();
				}
			}
			if (code != 200)
			{
				System.err.println("Attempted to reach: "
					+ connection.getURL().toString());
				System.err.println(sb);
				failureContinuation.accept(new ResponseException(
					code,
					connection.getResponseMessage(),
					sb.toString()));
			}
			else
			{
				contentConsumer.accept((JSONObject) new JSONReader(
					new StringReader(sb.toString())).read());
			}
		}
		catch (final IOException e)
		{
			failureContinuation.accept(
				new ConnectionException("Unexpected Error", e));
		}
		catch (final Throwable e)
		{
			failureContinuation.accept(
				new ApplicationException(
					ExitCode.UNEXPECTED_EXCEPTION, "Unexpected Error", e));
		}
		finally
		{
			if (connection != null)
			{
				connection.disconnect();
			}
		}
	}

	/**
	 * Process the server's response to an {@link APIRequest} and download
	 * the accompanying streaming data and save to disk.
	 *
	 * @param downloadRequest
	 *        The {@link APIRequest} that contains the specifics of the
	 *        download being performed.
	 */
	private void processDownloadRequest (
		final APIRequest<?> downloadRequest)
	{
		final Consumer<ApplicationException> failureContinuation =
			downloadRequest.failureContinuation();
		HttpURLConnection connection = null;
		try
		{
			connection = createConnection(downloadRequest, failureContinuation);
			final int code = connection.getResponseCode();
			if (code != 200)
			{
				final InputStream in =
					new BufferedInputStream(connection.getErrorStream());
				final StringBuilder sb = new StringBuilder();
				try (final InputStreamReader reader = new InputStreamReader(in))
				{
					int c = reader.read();
					while (c != -1)
					{
						sb.append((char) c);
						c = reader.read();
					}
					System.err.println("Attempted to reach: "
						+ connection.getURL().toString());
					System.err.println(sb);
					failureContinuation.accept(new ResponseException(
						code,
						connection.getResponseMessage(),
						sb.toString()));
				}
			}
			else
			{
				File targetFile = new File(
					downloadRequest.outputPath() + File.separator
						+ downloadRequest.file().fileNameOnly());

				try (final InputStream stream = connection.getInputStream())
				{
					Files.copy(
						stream,
						targetFile.toPath(),
						StandardCopyOption.REPLACE_EXISTING);
					downloadRequest.contentConsumer().accept(
						(JSONObject) new JSONReader(
							new StringReader("{}")).read());
				}
				catch (final IOException e)
				{
					failureContinuation.accept(
						new DownloadException(
							"could not download data to " +
								targetFile.getAbsolutePath(),
							e));
				}
			}
		}
		catch (final IOException e)
		{
			// Probably can do something better here but as it stands if the
			// connection fails here, a good thing to do is to exit the
			// application as the entire health of the application depends
			failureContinuation.accept(
				new ConnectionException("Unexpected Error", e));
		}
		catch (final Throwable e)
		{
			failureContinuation.accept(
				new ApplicationException(
					ExitCode.UNEXPECTED_EXCEPTION, "Unexpected Error", e));
		}
		finally
		{
			connection.disconnect();
		}
	}
}
