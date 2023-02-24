/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.options;

import java.io.PrintWriter;

public class ErrorHandlingCommand<T> implements SafeCommand<T> {

	private final CommandLineOptions options;
	private final Command<T> delegate;
	private final HelpCommand<T> helpCommand;

	public ErrorHandlingCommand(CommandLineOptions options, Command<T> delegate, HelpCommand<T> helpCommandSupplier) {
		this.options = options;
		this.delegate = delegate;
		this.helpCommand = helpCommandSupplier;
	}

	@Override
	public CommandResult<T> run(PrintWriter out, PrintWriter err) {
		try {
			return delegate.run(out, err);
		}
		catch (Exception exception) {
			exception.printStackTrace(err);
			err.println();
			helpCommand.run(out, err);
			return CommandResult.failure();
		}
	}

}
