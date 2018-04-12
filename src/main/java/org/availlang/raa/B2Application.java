/*
 * B2Application.java
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

package org.availlang.raa;

import org.availlang.raa.ApplicationRuntime.ExitCode;
import org.availlang.raa.api.AuthenticationContext;
import org.availlang.raa.api.b2api.B2AuthorizeAccountRequest;
import org.availlang.raa.api.b2api.B2BucketListFileNamesRequest;
import org.availlang.raa.api.b2api.B2BucketListFileNamesResponse;
import org.availlang.raa.api.b2api.B2DownloadFileByIdRequest;
import org.availlang.raa.api.b2api.B2ListBucketsRequest;
import org.availlang.raa.client.http.HTTPClient;
import org.availlang.raa.exceptions.ApplicationException;
import org.availlang.raa.utilities.ConsoleUtility;
import org.availlang.raa.utilities.PropertiesManager;

import javax.swing.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

/**
 * An {@code B2Application} is the entry point for the command line application
 * that allows a user to inspect the files in their BackBlaze B2 account and
 * download any of those files.
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
public class B2Application
{
	/**
	 * This is a stand-in failure handling strategy for this application. If
	 * this application were to enter serious use, each failure condition should
	 * be evaluated and handled separately. As it stands, shutting down the
	 * application is good enough for the current purpose.
	 */
	private static final Consumer<ApplicationException> failureContinuation =
		appEx ->
		{
			appEx.printStackTrace();
			appEx.exitCode.shutdown();
		};


	/**
	 * Prompt the user to perform one of the top level actions:
	 *
	 * <ol>
	 *     <li>Update account info</li>
	 *     <li>Authenticate a session with the B2 server</li>
	 *     <li>Exit the application</li>
	 * </ol>
	 *
	 * @param consoleUtility
	 *        The {@link ConsoleUtility} used to interact with the user.
	 */
	private static void selectTopLevelOption (
		final ConsoleUtility consoleUtility)
	{
		@SuppressWarnings("StringBufferReplaceableByString")
		final String prompt = new StringBuilder("Select an option:\n")
			.append("\t1. Update account info\n")
			.append("\t2. Authenticate session\n")
			.append("\t3. Exit\n")
			.append("Choice: ")
			.toString();

		final int choice =
			consoleUtility.readIntFromChoices(prompt, 1, 2, 3);

		switch (choice)
		{
			case 1:
			{
				setupAccount(consoleUtility);
				PropertiesManager.retrieveAccountInfo();
				selectTopLevelOption(consoleUtility);
				break;
			}
			case 2:
			{
				AuthenticationContext.authenticate();
				selectAuthenticatedOption(consoleUtility);
				break;
			}
			case 3:
			{
				ExitCode.NORMAL_EXIT.shutdown();
				break;
			}
			default:
			{
				consoleUtility.println("Invalid choice!");
				selectTopLevelOption(consoleUtility);
			}
		}
	}

	/**
	 * Prompt the user to perform one of the entry level authenticated actions:
	 *
	 * <ol>
	 *     <li>Get account buckets</li>
	 *     <li>Go back to {@link #selectTopLevelOption(ConsoleUtility)}</li>
	 *     <li>Exit the application</li>
	 * </ol>
	 *
	 * @param consoleUtility
	 *        The {@link ConsoleUtility} used to interact with the user.
	 */
	private static void selectAuthenticatedOption (
		final ConsoleUtility consoleUtility)
	{
		@SuppressWarnings("StringBufferReplaceableByString")
		final String prompt = new StringBuilder("Select an option:\n")
			.append("\t1. List Buckets\n")
			.append("\t2. Go Back\n")
			.append("\t3. Exit\n")
			.append("Choice: ")
			.toString();

		final int choice =
			consoleUtility.readIntFromChoices(prompt, 1, 2, 3);

		switch (choice)
		{
			case 1:
			{
				AuthenticationContext.processRequest(
					new B2ListBucketsRequest(
						response -> selectBucketOption(
							response.buckets(), consoleUtility),
						failureContinuation));
				break;
			}
			case 2:
			{
				selectTopLevelOption(consoleUtility);
				break;
			}
			case 3:
			{
				ExitCode.NORMAL_EXIT.shutdown();
				break;
			}
			default:
			{
				consoleUtility.println("Invalid choice!");
				selectTopLevelOption(consoleUtility);
			}
		}
	}

	/**
	 * Prompt the user to perform one of the top level actions:
	 *
	 * <ul>
	 *     <li>Select one of the buckets</li>
	 *     <li>Go back to {@link #selectAuthenticatedOption(ConsoleUtility)}</li>
	 *     <li>Exit the application</li>
	 * </ul>
	 *
	 * @param buckets
	 *        The {@link List} of {@link B2Bucket} options to choose from.
	 * @param consoleUtility
	 *        The {@link ConsoleUtility} used to interact with the user.
	 */
	private static void selectBucketOption (
		final List<B2Bucket> buckets,
		final ConsoleUtility consoleUtility)
	{
		final StringBuilder sb = new StringBuilder()
			.append("Select a bucket to download files from:\n");

		final int bucketCount = buckets.size();
		final Set<Integer> options = new HashSet<>();
		for (int i = 0; i < bucketCount; i++)
		{
			final B2Bucket bucket = buckets.get(i);
			final int choice = i + 1;
			sb.append('\t').append(choice).append(". ")
				.append(bucket.bucketName).append('\n');
			options.add(choice);
		}
		final int goBack = bucketCount + 1;
		final int exit = bucketCount + 2;
		options.add(goBack);
		options.add(exit);
		sb.append('\t').append(goBack).append(". Go Back\n")
			.append('\t').append(exit).append(". Exit\n")
			.append("Choice: ");
		final String prompt = sb.toString();

		final int choice =
			consoleUtility.readIntFromChoices(prompt, options);

		if (choice == exit)
		{
			ExitCode.NORMAL_EXIT.shutdown();
		}
		else if (choice == goBack)
		{
			selectAuthenticatedOption(consoleUtility);
		}
		else
		{
			final B2Bucket bucket = buckets.get(choice - 1);
			final String pathPrompt =
				"Enter absolute directory path where the files should be "
					+ "downloaded to: ";
			final String outputDirectory =
				consoleUtility.readDirectoryPath(pathPrompt);
			AuthenticationContext.processRequest(
				new B2BucketListFileNamesRequest(
					bucket,
					response ->
						selectFilesToDownloadOption(
							response,
							bucket,
							outputDirectory,
							buckets,
							consoleUtility),
					failureContinuation));
		}
	}

	/**
	 * Prompt the user to perform one of the top level actions:
	 *
	 * <ul>
	 *     <li>Select one of the buckets</li>
	 *     <li>Go back to {@link #selectAuthenticatedOption(ConsoleUtility)}</li>
	 *     <li>Exit the application</li>
	 * </ul>
	 *
	 * @param consoleUtility
	 *        The {@link ConsoleUtility} used to interact with the user.
	 */
	private static void selectFilesToDownloadOption (
		final B2BucketListFileNamesResponse response,
		final B2Bucket chosenBucket,
		final String outputDirectory,
		final List<B2Bucket> buckets,
		final ConsoleUtility consoleUtility)
	{
		final StringBuilder sb =
			new StringBuilder("Select ")
			.append(chosenBucket.bucketName)
			.append(" files to download\n");

		final List<B2File> files = response.files();
		final int fileCount = files.size();
		final Set<Integer> options = new HashSet<>();
		for (int i = 0; i < fileCount; i++)
		{
			final B2File file = files.get(i);
			final int choice = i + 1;
			sb.append('\t').append(choice).append(". ")
				.append(file.fileName).append('\n');
			options.add(choice);
		}
		int incrementBy = 1;
		final String nextFile = response.nextFile();
		final int seeMore;
		if (nextFile != null)
		{
			seeMore = fileCount + incrementBy++;
			options.add(seeMore);
			sb.append('\t').append(seeMore)
				.append(". See next set of files\n");
		}
		else
		{
			seeMore = -1;
		}
		final int goBack = fileCount + incrementBy++;
		final int exit = fileCount + incrementBy;
		options.add(goBack);
		options.add(exit);
		sb.append('\t').append(goBack).append(". Go Back\n")
			.append('\t').append(exit).append(". Exit\n")
			.append("Space-delimited choices (e.g. 1 4 5); choosing ")
			.append("\"Go Back\" or \"Exit\" will cancel download\n")
			.append("Select Multiple Files: ");
		final String prompt = sb.toString();

		final Set<Integer> choiceSet =
			consoleUtility.multiSelectIntsFromChoices(prompt, options);

		final boolean seeMoreRequested = choiceSet.contains(seeMore);
		final boolean retrieveMore = nextFile != null && seeMoreRequested;

		if (choiceSet.contains(exit))
		{
			ExitCode.NORMAL_EXIT.shutdown();
		}
		else if (choiceSet.contains(goBack))
		{
			selectBucketOption(buckets, consoleUtility);
		}
		else if (choiceSet.size() == 1 && retrieveMore)
		{
			AuthenticationContext.processRequest(
				new B2BucketListFileNamesRequest(
					chosenBucket,
					nextFile,
					nextResponse ->
						selectFilesToDownloadOption(
							nextResponse,
							chosenBucket,
							outputDirectory,
							buckets,
							consoleUtility),
					failureContinuation));
		}
		else
		{
			final int[] outstandingRequests =
				{seeMoreRequested ? choiceSet.size() - 1: choiceSet.size()};
			final StringBuilder report =
				new StringBuilder("Download Results:\n");
			choiceSet.forEach(choice ->
			{
				if (choice != seeMore)
				{
					final B2File file = files.get(choice - 1);
					AuthenticationContext.processRequest(
						new B2DownloadFileByIdRequest(
							file,
							outputDirectory,
							downloadResponse ->
							{
								synchronized (choiceSet)
								{
									report.append('\n')
										.append(file.fileNameOnly())
										.append(" download complete");
								if ((outstandingRequests[0] -= 1) == 0)
									{
										// This is where something can go to notify the
										// user that the file is done downloading.
										// Don't want to muck up the console with that,
										// so a very basic pop-up is used.
										final JFrame frame = new JFrame();
										frame.setDefaultCloseOperation(
											WindowConstants.DISPOSE_ON_CLOSE);
										JOptionPane.showMessageDialog(
											new JFrame(),
											report.toString(),
											"B2 Download",
											JOptionPane.PLAIN_MESSAGE);
									}
								}
							},
							ex ->
							{
								// This is the only place with a clear-cut
								// exception handling that isn't shutting down
								// the application.
								synchronized (choiceSet)
								{
									outstandingRequests[0] -= 1;
								}
								final JFrame frame = new JFrame();
								frame.setDefaultCloseOperation(
									WindowConstants.DISPOSE_ON_CLOSE);
								JOptionPane.showMessageDialog(
									new JFrame(),
									file.fileNameOnly()
										+ " download failed",
									"Failure",
									JOptionPane.ERROR_MESSAGE);
							}));
				}
			});

			if (retrieveMore || (nextFile != null
				&& consoleUtility.readBoolean(
					"There are more files available for download, "
						+ "would you like to see them? (y/n) ")))
			{
				AuthenticationContext.processRequest(
					new B2BucketListFileNamesRequest(
						chosenBucket,
						nextFile,
						nextResponse ->
						selectFilesToDownloadOption(
							nextResponse,
							chosenBucket,
							outputDirectory,
							buckets,
							consoleUtility),
						failureContinuation));
			}
			else
			{
				selectBucketOption(buckets, consoleUtility);
			}
		}
	}

	/**
	 * Set up a B2 account.
	 *
	 * @param consoleUtility
	 *        The {@link ConsoleUtility} used to interact with the user.
	 */
	private static void setupAccount (final ConsoleUtility consoleUtility)
	{
		final String accountId =
			consoleUtility.readNonEmptyString("Enter account id: ");
		final String appKey =
			consoleUtility.readNonEmptyString(
				"Enter application key: ");
		PropertiesManager.updateAccountInfo(accountId, appKey);
	}

	/**
	 * The main entry point for this application.
	 *
	 * <p>
	 * The {@link System#exit(int) system exit} codes are defined by {@link
	 * ExitCode}.
	 * </p>
	 *
	 * @param args
	 *        Expects a single String argument that is the location of the file
	 *        to read and perform the aggregation on.
	 */
	public static void main (final String[] args)
	{
		final ConsoleUtility consoleUtility = ConsoleUtility.newUtility();
		ApplicationRuntime.initialize(
			new HTTPClient(),
			B2AuthorizeAccountRequest::authenticate);
		if (!PropertiesManager.propertiesFileExists())
		{
			consoleUtility.println("No account info present");
			setupAccount(consoleUtility);
		}
		PropertiesManager.retrieveAccountInfo();
		selectTopLevelOption(consoleUtility);
		ApplicationRuntime.block();
	}
}
