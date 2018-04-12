/*
 * ApplicationState.java
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
import org.availlang.raa.client.Client;

/**
 * A {@code ApplicationState} describes the current state of the running
 * application.
 *
 * <p>
 * <em>NOTE:</em> This could be extended to make it more like a state machine
 * that knows its next acceptable states. Each state change location would need
 * to be evaluated to see what valid state transitions there might be. The
 * enum then could be extended to answer the acceptable state transitions
 * and have them be checked at each transition. I obviously didn't do this...
 * </p>
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
public enum ApplicationState
{
	/** Has no account info and is not authenticated */
	ACCOUNT_UNINITIALIZED,

	/** Has account info but is not authenticated */
	ACCOUNT_INITIALIZED,

	/** Is in the process of authenticating */
	AUTHENTICATING,

	/**
	 * The application is authenticated and can send secure requests to the
	 * server.
	 */
	AUTHENTICATED,

	/**
	 * Has no {@link Client}, o account info,  and is not authenticated.
	 */
	APPLICATION_UNINITIALIZED
}
