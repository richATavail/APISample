/*
 * PropertiesException.java
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

package org.availlang.raa.exceptions;
import org.availlang.raa.ApplicationRuntime.ExitCode;
import org.availlang.raa.client.Client;

/**
 * A {@code PropertiesException} is an {@link ApplicationException} thrown when
 * issues arise with a {@link Client} connection.
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
public class ConnectionException
extends ApplicationException
{
	/**
	 * Construct a {@link ConnectionException}.
	 *
	 * @param message
	 *        The detailed error message.
	 * @param cause
	 *        The {@link Throwable} that triggered this exception.
	 */
	public ConnectionException (
		final String message,
		final Throwable cause)
	{
		super(ExitCode.FAILED_CONNECTION, message, cause);
	}

	/**
	 * Construct a {@link ConnectionException}.
	 *
	 * @param message
	 *        The detailed error message.
	 */
	public ConnectionException (final String message)
	{
		super(ExitCode.FAILED_CONNECTION, message);
	}
}
