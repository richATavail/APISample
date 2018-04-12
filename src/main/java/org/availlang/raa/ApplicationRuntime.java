/*
 * ApplicationRuntime.java
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
import org.availlang.raa.api.AuthenticationContext;
import org.availlang.raa.client.Client;
import org.availlang.raa.exceptions.ApplicationException;
import org.availlang.raa.exceptions.InitializationException;

import javax.annotation.Nullable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.AbortPolicy;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

/**
 * An {@code ApplicationRuntime} contains the static runtime components of a
 * single running Application. It manages execution the execution of the
 * application.
 *
 * <p>
 * <em>NOTE:</em> {@code ApplicationRuntime} could be extended to manage other
 * potential runtime features such as logging.
 * </p>
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
@SuppressWarnings("NullableProblems")
public class ApplicationRuntime
{
	/**
	 * {@code ExitCode} abstracts the allowed values for calls of {@link
	 * System#exit(int)}.
	 *
	 * <ol start="0">
	 *     <li>Normal exit:   {@link #NORMAL_EXIT}</li>
	 *     <li>Abnormal exit: {@link #AUTHENTICATION_FAILURE}</li>
	 *     <li>Abnormal exit: {@link #COULD_NOT_CONNECT}</li>
	 *     <li>Abnormal exit: {@link #API_MISMATCH}</li>
	 *     <li>Abnormal exit: {@link #UNEXPECTED_EXCEPTION}</li>
	 *     <li>Abnormal exit: {@link #APPLICATION_DATA_ISSUE}</li>
	 *     <li>Abnormal exit: {@link #APPLICATION_STATE_ISSUE}</li>
	 *     <li>Abnormal exit: {@link #DATA_DOWNLOAD_ISSUE}</li>
	 *     <li>Abnormal exit: {@link #DATA_DOWNLOAD_ISSUE}</li>
	 *     <li>Abnormal exit: {@link #FAILED_CONNECTION}</li>
	 *     <li>Abnormal exit: {@link #BAD_RESPONSE}</li>
	 *     <li>Abnormal exit: {@link #BAD_STATE}</li>
	 * </ol>
	 */
	public enum ExitCode
	{
		/** Normal exit. */
		NORMAL_EXIT (0),

		/** Application could not authenticate Backblaze account. */
		AUTHENTICATION_FAILURE(1),

		/** Could not establish connection with Backblaze server */
		COULD_NOT_CONNECT(2),

		/** A mismatch in the API and what is received from the server */
		API_MISMATCH(3),

		/** Unexpected exception. */
		UNEXPECTED_EXCEPTION(4),

		/**
		 * When data required for the full operation of this application isn't
		 * in an expected state.
		 */
		APPLICATION_DATA_ISSUE(5),

		/**
		 * When the {@link ApplicationState} required this application isn't
		 * in the expected state.
		 */
		APPLICATION_STATE_ISSUE(6),

		/**
		 * When there is a problem with downloading data.
		 */
		DATA_DOWNLOAD_ISSUE(7),

		/**
		 * The connection failed.
		 */
		FAILED_CONNECTION(8),

		/**
		 * An issue with a response from the server.
		 */
		BAD_RESPONSE(9),

		/**
		 * The application attempted to perform an un-allowed action for the
		 * current {@link ApplicationState}.
		 */
		BAD_STATE(10);

		/**
		 * The status code for {@link System#exit(int)}.
		 */
		private final int status;

		/**
		 * Shutdown the application with this {@link ExitCode}.
		 */
		public void shutdown ()
		{
			System.exit(status);
		}

		/**
		 * Construct an {@link ExitCode}.
		 *
		 * @param status
		 *        The status code for {@link System#exit(int)}.
		 */
		ExitCode (int status)
		{
			this.status = status;
		}
	}

	/**
	 * A synchronization object for setting the state.
	 */
	public static final Object stateLock = new Object();

	/**
	 * The current {@link ApplicationState} of the running application.
	 */
	private static volatile ApplicationState state =
		ApplicationState.APPLICATION_UNINITIALIZED;

	/**
	 * Answer the current {@link ApplicationState} of the running application.
	 *
	 * @return An {@link ApplicationState}.
	 */
	public static ApplicationState state ()
	{
		return state;
	}

	/**
	 * Set the {@link #state}.
	 *
	 * @param state
	 *        An {@link ApplicationState}.
	 */
	public static void setState (final ApplicationState state)
	{
		synchronized (stateLock)
		{
			ApplicationRuntime.state = state;
		}
	}

	/**
	 * The {@link Semaphore} that is used to keep the application from shutting
	 * down at the end of the a {@code main} method.
	 */
	private final static Semaphore applicationSemaphore =
		new Semaphore(0);

	/**
	 * Block the main thread of execution from completing.
	 */
	public static void block ()
	{
		try
		{
			applicationSemaphore.acquire();
		}
		catch (final InterruptedException e)
		{
			ExitCode.NORMAL_EXIT.shutdown();
		}
	}

	/**
	 * The {@link Client} used by this {@link ApplicationRuntime}.
	 */
	private static @Nullable Client client;

	/**
	 * Set the {@link Client} used by this {@link ApplicationRuntime}.
	 *
	 * @param client
	 *        The {@code Client} to set.
	 * @param authenticator
	 *        The {@link BiConsumer} that accepts two Strings; the {@link
	 *        AuthenticationContext#accountId} and the {@link
	 *        AuthenticationContext#applicationKey} that can be run to
	 *        authenticate the account.
	 */
	public static void initialize (
		final Client client,
		final BiConsumer<String, String> authenticator)
	{
		synchronized (stateLock)
		{
			ApplicationRuntime.client = client;
			// Changing the could invalidate the account in the
			// AuthenticationContext, so best to clear it.
			AuthenticationContext.soleInstance
				.updateAccountInfo("", "");
			AuthenticationContext.setAuthenticator(authenticator);
			state = ApplicationState.ACCOUNT_UNINITIALIZED;
		}
	}

	/**
	 * Clear the {@link Client} and set the application back to {@link
	 * ApplicationState#ACCOUNT_UNINITIALIZED}. This is used for testing.
	 */
	public static void uninitializeApplication ()
	{
		ApplicationRuntime.client = null;
		// Changing the could invalidate the account in the
		// AuthenticationContext, so best to clear it.
		AuthenticationContext.soleInstance
			.updateAccountInfo("", "");
		state = ApplicationState.APPLICATION_UNINITIALIZED;
	}

	/**
	 * Answer the {@link Client} used by this {@link ApplicationRuntime}.
	 *
	 * @return A {@code Client}.
	 */
	public static Client client ()
	{
		if (client == null)
		{
			final ApplicationException ex =
			new InitializationException("Expected Client to be set");
			ex.exitCode.shutdown();
		}
		return client;
	}

	/**
	 * The {@link ThreadPoolExecutor} used by this application.
	 */
	private static final ThreadPoolExecutor threadPoolExecutor =
		new ThreadPoolExecutor(
			Runtime.getRuntime().availableProcessors(),
			Runtime.getRuntime().availableProcessors() << 2,
			10L,
			TimeUnit.SECONDS,
			new LinkedBlockingQueue<>(),
			runnable ->
			{
				final Thread thread = new Thread(runnable);
				thread.setDaemon(true);
				return thread;
			},
			new AbortPolicy());

	/**
	 * The {@link ScheduledThreadPoolExecutor} used by this application to run
	 * timers.
	 */
	private static final ScheduledThreadPoolExecutor timer =
		new ScheduledThreadPoolExecutor(
			1,
			runnable ->
			{
				final Thread thread = new Thread(runnable);
				thread.setDaemon(true);
				return thread;
			},
			new AbortPolicy());

	/**
	 * Answer a {@link ScheduledFuture} that will run in the future.
	 *
	 * @param millisFromNow
	 *        How far into the future in milliseconds when the {@link Runnable}
	 *        will run.
	 * @param task
	 *        The {@code Runnable} to run.
	 * @return A {@code ScheduledFuture}.
	 */
	public static ScheduledFuture<?> startTimer (
		long millisFromNow,
		final Runnable task)
	{
		return timer.schedule(task, millisFromNow, TimeUnit.MILLISECONDS);
	}

	/**
	 * Schedule the provided {@link Runnable} with the {@link
	 * #threadPoolExecutor}.
	 *
	 * @param r
	 *        The {@link Runnable} to execute.
	 */
	public static void scheduleTask (final Runnable r)
	{
		threadPoolExecutor.execute(r);
	}

	/**
	 * As {@link ApplicationRuntime} is only used for its static properties,
	 * block construction of an instance as it is not used.
	 */
	private ApplicationRuntime () { /* Do Nothing */}
}
