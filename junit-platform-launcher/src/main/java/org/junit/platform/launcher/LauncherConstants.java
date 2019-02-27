/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;
import org.junit.platform.engine.reporting.ReportEntry;

/**
 * Collection of constants related to {@link Launcher}.
 *
 * @see org.junit.platform.engine.ConfigurationParameters
 * @since 1.3
 */
@API(status = EXPERIMENTAL, since = "1.3")
public class LauncherConstants {

	/**
	 * Property name used to enable capturing output to {@link System#out}:
	 * {@value}
	 *
	 * <p>By default, output to {@link System#out} is not captured.
	 *
	 * <p>If enabled, the JUnit Platform captures the corresponding output and
	 * publishes it as a {@link ReportEntry} using the
	 * {@value #STDOUT_REPORT_ENTRY_KEY} key immediately before reporting the
	 * test identifier as finished.
	 *
	 * @see #STDOUT_REPORT_ENTRY_KEY
	 * @see ReportEntry
	 * @see TestExecutionListener#reportingEntryPublished(TestIdentifier, ReportEntry)
	 */
	public static final String CAPTURE_STDOUT_PROPERTY_NAME = "junit.platform.output.capture.stdout";

	/**
	 * Property name used to enable capturing output to {@link System#err}:
	 * {@value}
	 *
	 * <p>By default, output to {@link System#err} is not captured.
	 *
	 * <p>If enabled, the JUnit Platform captures the corresponding output and
	 * publishes it as a {@link ReportEntry} using the
	 * {@value #STDERR_REPORT_ENTRY_KEY} key immediately before reporting the
	 * test identifier as finished.
	 *
	 * @see #STDERR_REPORT_ENTRY_KEY
	 * @see ReportEntry
	 * @see TestExecutionListener#reportingEntryPublished(TestIdentifier, ReportEntry)
	 */
	public static final String CAPTURE_STDERR_PROPERTY_NAME = "junit.platform.output.capture.stderr";

	/**
	 * Property name used to configure the maximum number of bytes for buffering
	 * to use per thread and output type if output capturing is enabled:
	 * {@value}
	 *
	 * <p>Value must be an integer; defaults to {@value CAPTURE_MAX_BUFFER_DEFAULT}.
	 *
	 * @see #CAPTURE_MAX_BUFFER_DEFAULT
	 */
	public static final String CAPTURE_MAX_BUFFER_PROPERTY_NAME = "junit.platform.output.capture.maxBuffer";

	/**
	 * Default maximum number of bytes for buffering to use per thread and
	 * output type if output capturing is enabled.
	 *
	 * @see #CAPTURE_MAX_BUFFER_PROPERTY_NAME
	 */
	public static final int CAPTURE_MAX_BUFFER_DEFAULT = 4 * 1024 * 1024;

	/**
	 * Key used to publish captured output to {@link System#out} as part of a
	 * {@link ReportEntry}: {@value}
	 */
	public static final String STDOUT_REPORT_ENTRY_KEY = "stdout";

	/**
	 * Key used to publish captured output to {@link System#err} as part of a
	 * {@link ReportEntry}: {@value}
	 */
	public static final String STDERR_REPORT_ENTRY_KEY = "stderr";

	private LauncherConstants() {
		/* no-op */
	}

}
