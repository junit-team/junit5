/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console.tasks;

import static org.junit.gen5.commons.meta.API.Usage.Internal;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.function.Consumer;

import org.junit.gen5.commons.meta.API;

/**
 * @since 5.0
 */
@API(Internal)
public class ConsoleTaskExecutor {

	private final PrintStream out;
	private final PrintStream err;

	public ConsoleTaskExecutor(PrintStream out, PrintStream err) {
		this.out = out;
		this.err = err;
	}

	public int executeTask(ConsoleTask task, Consumer<PrintWriter> helpPrinter) {
		PrintWriter outWriter = new PrintWriter(out);
		try {
			return task.execute(outWriter);
		}
		catch (Exception e) {
			printException(e);
			printHelp(helpPrinter, outWriter);
			return -1;
		}
		finally {
			outWriter.flush();
		}
	}

	private void printHelp(Consumer<PrintWriter> helpPrinter, PrintWriter outWriter) {
		try {
			helpPrinter.accept(outWriter);
		}
		catch (Exception e) {
			err.print("Exception occurred while printing help: ");
			printException(e);
		}
	}

	private void printException(Exception exception) {
		exception.printStackTrace(err);
		err.println();
	}
}
