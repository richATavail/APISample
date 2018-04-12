/*
 * Bucket.java
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
/**
 * A {@code B2Bucket} is a grouping of Backblaze B2 storage.
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 * @see <a href="https://www.backblaze.com/b2/docs/buckets.html">B2 Buckets</a>
 */
public class B2Bucket
{
	/**
	 * The types of B2 buckets.
	 */
	public enum BucketType
	{
		/** The private buckets */
		PRIVATE { @Override public String key () { return "allPrivate"; } },

		/** The public buckets */
		PUBLIC { @Override public String key () { return "allPublic"; } };

		/**
		 * The key that identifies the bucket type.
		 *
		 * @return A String.
		 */
		public abstract String key ();
	}

	/**
	 * The unique identifier of the bucket.
	 */
	public final String bucketId;

	/**
	 * The name of the bucket.
	 */
	public final String bucketName;

	/**
	 * Construct a {@link B2Bucket}.
	 *
	 * @param bucketId
	 *        The unique identifier of the bucket.
	 * @param bucketName
	 *        The name of the bucket.
	 */
	public B2Bucket (
		final String bucketId,
		final String bucketName)
	{
		this.bucketId = bucketId;
		this.bucketName = bucketName;
	}
}
