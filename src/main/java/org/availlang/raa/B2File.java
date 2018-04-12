/*
 * B2File.java
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

package org.availlang.raa;

import java.util.Objects;

/**
 * A {@code B2File} represents the metadata (including an identifier) of a file
 * stored in a B2 {@link B2Bucket}.
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
public class B2File
{
	/**
	 * The name of the file.
	 */
	public final String fileName;

	/**
	 * Return the name of the file absent any directory information.
	 *
	 * <p>
	 * <strong>NOTE:</strong> It has been observed that the B2 server return
	 * file names with the directory structure with path using a '/' as the
	 * directory separator. This method is written with that as the base
	 * understanding.
	 * </p>
	 *
	 * @return A String.
	 */
	public String fileNameOnly ()
	{
		assert !fileName.isEmpty() : "A file should have a name!";
		final String[] path = fileName.split("/");
		return path[path.length - 1];
	}

	/**
	 * The unique id of the file.
	 */
	public final String fileId;

	/**
	 * Construct a {@link B2File}.
	 *
	 * @param fileName
	 *        The name of the file.
	 * @param fileId
	 *        The unique id of the file.
	 */
	public B2File (
		final String fileName,
		final String fileId)
	{
		this.fileName = fileName;
		this.fileId = fileId;
	}
}
