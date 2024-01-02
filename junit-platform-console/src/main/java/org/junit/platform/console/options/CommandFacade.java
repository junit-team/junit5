/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.options;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.io.PrintWriter;

import org.apiguardian.api.API;
import org.junit.platform.console.tasks.ConsoleTestExecutor;

/**
 * Internal facade to run a CLI command that exists to hide implementation
 * details such as the used library.
 *
 * @since 1.10
 */
@API(status = INTERNAL, since = "1.10")
public class CommandFacade {

	private final ConsoleTestExecutor.Factory consoleTestExecutorFactory;

	public CommandFacade(ConsoleTestExecutor.Factory consoleTestExecutorFactory) {
		this.consoleTestExecutorFactory = consoleTestExecutorFactory;
	}

	public CommandResult<?> run(PrintWriter out, PrintWriter err, String[] args) {
		return new MainCommand(consoleTestExecutorFactory).run(out, err, args);
	}
}
