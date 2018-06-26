/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.parallel;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.io.PrintStream;
import java.util.Properties;

import org.apiguardian.api.API;

/**
 * Common resource names for synchronizing test execution.
 *
 * @see ResourceLock
 * @since 5.3
 */
@API(status = EXPERIMENTAL, since = "5.3")
public class Resources {

	/**
	 * Represents Java's system properties.
	 *
	 * @see System#getProperties()
	 * @see System#setProperties(Properties)
	 */
	public static final String SYSTEM_PROPERTIES = "java.lang.System.properties";

	/**
	 * Represents the standard output stream of the current process.
	 *
	 * @see System#out
	 * @see System#setOut(PrintStream)
	 */
	public static final String SYSTEM_OUT = "java.lang.System.out";

	/**
	 * Represents the standard error stream of the current process.
	 *
	 * @see System#err
	 * @see System#setErr(PrintStream)
	 */
	public static final String SYSTEM_ERR = "java.lang.System.err";

	private Resources() {
		/* no-op */
	}

}
