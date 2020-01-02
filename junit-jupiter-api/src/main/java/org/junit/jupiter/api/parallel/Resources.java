/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.parallel;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

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
	 * @see System#setProperties(java.util.Properties)
	 */
	public static final String SYSTEM_PROPERTIES = "java.lang.System.properties";

	/**
	 * Represents the standard output stream of the current process.
	 *
	 * @see System#out
	 * @see System#setOut(java.io.PrintStream)
	 */
	public static final String SYSTEM_OUT = "java.lang.System.out";

	/**
	 * Represents the standard error stream of the current process.
	 *
	 * @see System#err
	 * @see System#setErr(java.io.PrintStream)
	 */
	public static final String SYSTEM_ERR = "java.lang.System.err";

	/**
	 * Represents the default locale for the current instance of the JVM.
	 *
	 * @since 5.4
	 * @see java.util.Locale#setDefault(java.util.Locale)
	 */
	@API(status = EXPERIMENTAL, since = "5.4")
	public static final String LOCALE = "java.util.Locale.default";

	/**
	 * Represents the default time zone for the current instance of the JVM.
	 *
	 * @since 5.4
	 * @see java.util.TimeZone#setDefault(java.util.TimeZone)
	 */
	@API(status = EXPERIMENTAL, since = "5.4")
	public static final String TIME_ZONE = "java.util.TimeZone.default";

	private Resources() {
		/* no-op */
	}

}
