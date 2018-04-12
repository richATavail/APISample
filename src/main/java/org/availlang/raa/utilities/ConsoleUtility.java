/*
 * ConsoleUtility.java
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
import java.io.Console;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

/**
 * A {@code ConsoleUtility} is a class that provides helper methods for
 * interacting with a user through a console/terminal.
 *
 * <p>
 * This utility relies on recursion to handle errors to ensure a user responds
 * appropriately to prompts. Java has a limited amount of stack space available
 * for each thread. A sufficiently malicious/incompetent user could crash the
 * thread if they continue to enter invalid responses. Note that each recursion
 * is a tail recursion, so if the JVM implements tail-recursion elimination,
 * this concern goes away.
 * </p>
 *
 * @author Richard Arriaga &lt;rich@availlang.org&gt;
 */
public abstract class ConsoleUtility
{
	/**
	 * Print a String.
	 *
	 * @param s
	 *        The String to print.
	 */
	public abstract void print (final String s);

	/**
	 * Print a String and then terminate the line.
	 *
	 * @param s
	 *        The String to print.
	 */
	public abstract void println (final String s);

	/**
	 * Read a line of text from input.
	 *
	 * @param prompt
	 *        The String to print to prompt the user for input.
	 * @return A String.
	 */
	protected abstract String readLine (final String prompt);

	/**
	 * Read a nonempty String from input.
	 *
	 * @param prompt
	 *        The String to print to prompt the user for input.
	 * @return A String.
	 */
	public String readNonEmptyString (final String prompt)
	{
		final String line = readLine(prompt);
		if (line.isEmpty())
		{
			println("Nothing was entered!");
			return readNonEmptyString(prompt);
		}
		return line;
	}

	/**
	 * Read a String directory path from input.
	 *
	 * @param prompt
	 *        The String to print to prompt the user for input.
	 * @return A String.
	 */
	public String readDirectoryPath (final String prompt)
	{
		final String rawPath = readNonEmptyString(prompt);
		final String path = FileUtility.platformAppropriatePath(rawPath);

		if (FileUtility.directoryExists(path))
		{
			return path;
		}
		else
		{
			println(rawPath + " is not a valid directory!");
			return readDirectoryPath(prompt);
		}
	}

	/**
	 * Read a String file path from input.
	 *
	 * @param prompt
	 *        The String to print to prompt the user for input.
	 * @return A String.
	 */
	@SuppressWarnings("WeakerAccess")
	public String readFilePath (final String prompt)
	{
		final String rawPath = readNonEmptyString(prompt);
		final String path = FileUtility.platformAppropriatePath(rawPath);

		if (FileUtility.directoryExists(path))
		{
			return path;
		}
		else
		{
			println(rawPath + " is not a valid file!");
			return readFilePath(prompt);
		}
	}

	/**
	 * Read a {@code boolean} from input.
	 *
	 * <p>{@code true} is indicated by:</p>
	 * <ul>
	 *     <li>y</li>
	 *     <li>yes</li>
	 *     <li>true</li>
	 * </ul>
	 *
	 * <p>All other entered values are considered {@code false}.</p>
	 *
	 * @param prompt
	 *        The String to print to prompt the user for input.
	 * @return A {@code boolean}.
	 */
	@SuppressWarnings("unused")
	public boolean readBoolean (final String prompt)
	{
		final String line = readNonEmptyString(prompt);
		return line.equalsIgnoreCase("y")
			|| line.equalsIgnoreCase("yes")
			|| line.equalsIgnoreCase("true");
	}

	/**
	 * Read an {@code integer} from input.
	 *
	 * @param prompt
	 *        The String to print to prompt the user for input.
	 * @return An {@code integer}.
	 */
	@SuppressWarnings("WeakerAccess")
	public int readInt (final String prompt)
	{
		final String line = readNonEmptyString(prompt);

		int choice;
		try
		{
			choice = Integer.parseInt(line);
		}
		catch (final NumberFormatException e)
		{
			println(line + " is not a valid integer");
			return readInt(prompt);
		}
		return choice;
	}

	/**
	 * Read many space-delimited {@code integers} from input for the provided
	 * options.
	 *
	 * @param prompt
	 *        The String to print to prompt the user for input.
	 * @param options
	 *        A {@link Set} of {@code integers} that are the available options
	 *        to choose from.
	 * @return An {@code integer}.
	 */
	@SuppressWarnings("WeakerAccess")
	public Set<Integer> multiSelectIntsFromChoices (
		final String prompt,
		final Set<Integer> options)
	{
		final String line = readNonEmptyString(prompt);
		final String[] selected = line.split(" ");
		final Set<Integer> choices = new HashSet<>();

		for (final String value : selected)
		{
			try
			{
				int choice = Integer.parseInt(value);
				if (!options.contains(choice))
				{
					println(choice + " is not a valid option");
					return multiSelectIntsFromChoices(prompt, options);
				}
				choices.add(choice);
			}
			catch (final NumberFormatException e)
			{
				println(line + " contains invalid options");
				return multiSelectIntsFromChoices(prompt, options);
			}
		}
		return choices;
	}

	/**
	 * Read an {@code integer} from the input that is one of the values from
	 * the provided options.
	 *
	 * @param prompt
	 *        The String to print to prompt the user for input.
	 * @param options
	 *        An array of {@code integers} that are the available options to
	 *        choose from.
	 * @return An {@code integer}.
	 */
	public int readIntFromChoices (final String prompt, Integer... options)
	{
		assert options.length > 0 : "Need options to choose from!";
		final Set<Integer> choices = new HashSet<>(Arrays.asList(options));
		final int choice = readInt(prompt);

		if (!choices.contains(choice))
		{
			println(choice + " is not a valid option");
			return readIntFromChoices(prompt, options);
		}
		return choice;
	}

	/**
	 * Read an {@code integer} from the input that is one of the values from
	 * the provided options.
	 *
	 * @param prompt
	 *        The String to print to prompt the user for input.
	 * @param options
	 *        A {@link Set} of {@code integers} that are the available options
	 *        to choose from.
	 * @return An {@code integer}.
	 */
	public int readIntFromChoices (final String prompt, Set<Integer> options)
	{
		assert options.size() > 0 : "Need options to choose from!";
		final int choice = readInt(prompt);
		if (!options.contains(choice))
		{
			println(choice + " is not a valid option");
			return readIntFromChoices(prompt, options);
		}
		return choice;
	}

	/**
	 * Answer a new {@link ConsoleUtility} appropriate to the environment.
	 *
	 * @return A {@code ConsoleUtility}.
	 */
	public static ConsoleUtility newUtility ()
	{
		return System.console() == null
			? new ScannerPrintStreamConsoleUtility()
			: new SystemConsoleUtility();
	}

	/**
	 * A {@code SystemConsoleUtility} is a {@link ConsoleUtility} that
	 * specifically utilizes the {@link System#console()} for input and output.
	 */
	private static class SystemConsoleUtility extends ConsoleUtility
	{
		/**
		 * The {@link System#console()}.
		 */
		private final Console console = System.console();

		@Override
		public void print (final String s)
		{
			console.readLine(s);
		}

		@Override
		public void println (final String s)
		{
			console.printf("%s%n", s);
		}

		@Override
		public String readLine (final String prompt)
		{
			return console.readLine(prompt);
		}

		/* Prevent instantiation outside the outer class */
		private SystemConsoleUtility () { /* Do Nothing */ }
	}

	/**
	 * A {@code SystemConsoleUtility} is a {@link ConsoleUtility} that
	 * specifically utilizes the {@link System#in} in a {@link Scanner} for
	 * input and the {@link System#in} {@link PrintStream} for output.
	 */
	private static class ScannerPrintStreamConsoleUtility extends ConsoleUtility
	{
		/**
		 * A {@link Scanner} to be used for getting user input if the {@link
		 * Console} is not available.
		 */
		private final Scanner scanner = new Scanner(System.in);

		/**
		 * The {@link PrintStream} to print data if the {@link Console} is not
		 * available.
		 */
		private final PrintStream out = System.out;

		@Override
		public void print (final String s)
		{
			out.print(s);
		}

		@Override
		public void println (final String s)
		{
			out.println(s);
		}

		@Override
		public String readLine (final String prompt)
		{
			out.print(prompt);
			return scanner.nextLine();
		}
		/* Prevent instantiation outside the outer class */
		private ScannerPrintStreamConsoleUtility () { /* Do Nothing */ }
	}
}
