/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console.tasks;

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.io.PrintWriter;

import org.junit.platform.commons.meta.API;

/**
 * A task to be executed from the console.
 *
 * @since 1.0
 */
@API(Internal)
public interface ConsoleTask {

	/**
	 * Exit code indicating successful execution
	 */
	int SUCCESS = 0;

	/**
	 * Exit code indicating test failure(s)
	 */
	int TESTS_FAILED = 1;

	/**
	 * Execute this task and return an exit code.
	 *
	 * @param out writer for console output
	 * @return exit code indicating success ({@code 0}) or failure ({@code != 0})
	 * @see ConsoleTask#SUCCESS
	 */
	int execute(PrintWriter out) throws Exception;

}
