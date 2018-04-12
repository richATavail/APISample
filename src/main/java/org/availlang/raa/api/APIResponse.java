/*
 * APIResponse.java
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

package org.availlang.raa.api;
import com.avail.utility.json.JSONObject;

/**
 * An {@code APIResponse} contains the JSON response to a {@link APIRequest}.
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
public abstract class APIResponse
{
	/**
	 * The {@link JSONObject} that represents the raw content of the {@link
	 * APIResponse}.
	 */
	private final JSONObject content;

	/**
	 * Answer the {@link JSONObject} that represents the raw content of the
	 * {@link APIResponse}.
	 *
	 * @return A {@code JSONObject}.
	 */
	protected JSONObject content ()
	{
		return content;
	}

	/**
	 * Construct a {@link APIResponse}.
	 *
	 * @param content
	 *        The {@link JSONObject} that represents the raw content of the
	 *        {@link APIResponse}.
	 */
	protected APIResponse (
		final JSONObject content)
	{
		this.content = content;
	}
}
