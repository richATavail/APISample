/*
 * FileUtility.java
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

package org.availlang.raa.utilities;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * A {@code FileUtility} is a class that contains static helper methods.
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
@SuppressWarnings("WeakerAccess")
public class FileUtility
{
	/**
	 * Answer the platform specific path for the entered path.
	 *
	 * @param path
	 *        A String directory/file path.
	 * @return A String.
	 */
	public static String platformAppropriatePath (final String path)
	{
		final String[] pathArray = path.contains("/")
			? path.split("/")
			: path.split("\\\\");
		return String.join(File.separator, pathArray);
	}

	/**
	 * Check to see if the provided file path exists.
	 *
	 * @return {@code true} the file exists; {@code false} otherwise.
	 */
	public static boolean fileExists (final String path)
	{
		final File f = new File(platformAppropriatePath(path));
		return f.exists() && !f.isDirectory();
	}

	/**
	 * Check to see if the provided directory exists.
	 *
	 * @return {@code true} the file exists; {@code false} otherwise.
	 */
	public static boolean directoryExists (final String path)
	{
		final File f = new File(platformAppropriatePath(path));
		return f.exists() && f.isDirectory();
	}

	/**
	 * Open the indicated file (create it if necessary) and answer a {@link
	 * FileOutputStream} to that file.
	 *
	 * @param path
	 *        The location of the file.
	 * @return A {@code FileOutputStream}.
	 * @throws IOException If there are any issues opening the file.
	 */
	public static FileOutputStream fileOutputStream (final String path)
		throws IOException
	{
		final File file = new File(platformAppropriatePath(path));
		//noinspection ResultOfMethodCallIgnored
		file.createNewFile(); // if file already exists will do nothing
		return new FileOutputStream(file, false);
	}

	/**
	 * No instances of {@link FileUtility} should be constructed.
	 */
	private FileUtility () { /* Do Nothing */ }
}
