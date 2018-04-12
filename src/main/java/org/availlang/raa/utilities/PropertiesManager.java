/*
 * PropertiesManager.java
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

package org.availlang.raa.utilities;
import org.availlang.raa.ApplicationRuntime.ExitCode;
import org.availlang.raa.api.AuthenticationContext;
import org.availlang.raa.exceptions.PropertiesException;

import javax.annotation.Nullable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * A {@code PropertiesManager} manages the properties file for this application.
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
public class PropertiesManager
{
	/**
	 * An enum of all the properties in this application's {@link Properties}.
	 */
	enum PropertyKey
	{
		/** The {@link AuthenticationContext#accountId} properties key. */
		ACCOUNT_ID("accountId"),

		/** The {@link AuthenticationContext#applicationKey} properties key. */
		APPLICATION_KEY("applicationKey");

		/**
		 * The key to access the related field in the {@link
		 * PropertiesManager#properties}.
		 */
		final String key;

		/**
		 * Construct a {@link PropertyKey}.
		 *
		 * @param key
		 *        The key to access the related field in the {@link
		 *        PropertiesManager#properties}.
		 */
		PropertyKey (final String key)
		{
			this.key = key;
		}
	}

	/**
	 * Answer the sole {@link PropertiesManager} used by this application.
	 */
	private static final PropertiesManager soleInstance = new PropertiesManager();

	/** The location of the resources folder. */
	private final String appConfigPath = "config/app.properties";

	/**
	 * The application's {@link Properties}.
	 */
	private final Properties properties = new Properties();

	/**
	 * Retrieve the properties file and answer a {@linkplain
	 * Properties#load(InputStream) loaded} {@link Properties}.
	 *
	 * @throws PropertiesException When the properties file cannot be accessed.
	 */
	private void retrieveProperties ()
		throws PropertiesException
	{
		try (final FileInputStream fileInputStream =
			     new FileInputStream(appConfigPath))
		{
			properties.load(fileInputStream);
		}
		catch (final IOException e)
		{
			throw new PropertiesException(
				"Could not access properties file", e);
		}
	}

	/**
	 * Check to see if there is a {@link Properties} file in the resources
	 * folder.
	 *
	 * @return {@code true} the file exists; {@code false} otherwise.
	 */
	public static boolean propertiesFileExists ()
	{
		return FileUtility.fileExists(soleInstance.appConfigPath);
	}

	/**
	 * Answer the String property for the given {@link PropertyKey}.
	 *
	 * @param key
	 *        The {@code PropertyKey} to retrieve the value for.
	 * @return A String if the property exists; {@code null} otherwise.
	 */
	private @Nullable String getProperty (final PropertyKey key)
	{
		return properties.getProperty(key.key);
	}

	/**
	 * Update the application's {@link AuthenticationContext} with the account
	 * information from the file.
	 *
	 * <p>
	 * This could evolve into something arbitrarily complex if more properties
	 * were to develop, however as it stands only account info storage is needed
	 * to survive between running instances of the application.
	 * </p>
	 */
	public static void retrieveAccountInfo () throws PropertiesException
	{
		if (soleInstance.properties.isEmpty())
		{
			soleInstance.retrieveProperties();
		}
		final String accountId =
			soleInstance.getProperty(PropertyKey.ACCOUNT_ID);
		final String applicationKey =
			soleInstance.getProperty(PropertyKey.APPLICATION_KEY);
		if (accountId == null || applicationKey == null)
		{
			throw new PropertiesException(
				"A properties file exists but it is missing account info");
		}
		AuthenticationContext.soleInstance
			.updateAccountInfo(accountId, applicationKey);
	}

	/**
	 * Update the account information being used.
	 *
	 * @param accountId
	 *        The identifier for the account.
	 * @param applicationKey
	 *        The application key for the account.
	 */
	public static void updateAccountInfo (
		final String accountId,
		final String applicationKey)
	{
		soleInstance.properties.setProperty(
			PropertyKey.ACCOUNT_ID.key, accountId);
		soleInstance.properties.setProperty(
			PropertyKey.APPLICATION_KEY.key, applicationKey);
		try (final FileOutputStream stream =
			     FileUtility.fileOutputStream(soleInstance.appConfigPath))
		{
			soleInstance.properties.store(stream, "");
		}
		catch (final IOException e)
		{
			System.err.println("Could not update properties file");
			e.printStackTrace();
			ExitCode.UNEXPECTED_EXCEPTION.shutdown();
		}
	}

	/**
	 * Construct the {@link PropertiesManager}.
	 *
	 * <p>
	 * No public instantiation as this application should only have one instance
	 * of {@code PropertiesManger}.
	 * </p>
	 */
	private PropertiesManager () { /* Do nothing */ }
}
