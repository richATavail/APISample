/*
 * HTTPRequest.java
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
import org.availlang.raa.api.APICatalogue;
import org.availlang.raa.api.APIRequest;

/**
 * A {@code HTTPProtocolMethod} is an enum for identifying a type of HTTP
 * protocol method. This is to enable mapping an {@link APIRequest} to the
 * protocol method via {@link APICatalogue}.
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
public enum HTTPProtocolMethod
{
	/** The HTTP GET method */
	GET,

	/** The HTTP POST method */
	POST,

	/** The HTTP PUT method */
	@SuppressWarnings("unused")
	PUT,

	/** The HTTP HEAD method */
	@SuppressWarnings("unused")
	HEAD,

	/** The HTTP DELETE method */
	@SuppressWarnings("unused")
	DELETE,

	/** The HTTP TRACE method */
	@SuppressWarnings("unused")
	TRACE,

	/** The HTTP OPTIONS method */
	@SuppressWarnings("unused")
	OPTIONS,

	/** The HTTP CONNECT method */
	@SuppressWarnings("unused")
	CONNECT,

	/** The HTTP PATCH method */
	@SuppressWarnings("unused")
	PATCH
}
