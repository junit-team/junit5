/*
 * Copyright 2015-2020 the original author or authors.
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
import org.junit.platform.commons.util.ClassNamePatternFilterUtils;
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

	/**
	 * Property name used to provide patterns for deactivating listeners registered
	 * via the {@link java.util.ServiceLoader ServiceLoader} mechanism: {@value}
	 *
	 * <h3>Pattern Matching Syntax</h3>
	 *
	 * <p>If the property value consists solely of an asterisk ({@code *}), all
	 * listeners will be deactivated. Otherwise, the property value will be treated
	 * as a comma-separated list of patterns where each individual pattern will be
	 * matched against the fully qualified class name (<em>FQCN</em>) of each registered
	 * listener. Any dot ({@code .}) in a pattern will match against a dot ({@code .})
	 * or a dollar sign ({@code $}) in a FQCN. Any asterisk ({@code *}) will match
	 * against one or more characters in a FQCN. All other characters in a pattern
	 * will be matched one-to-one against a FQCN.
	 *
	 * <h3>Examples</h3>
	 *
	 * <ul>
	 * <li>{@code *}: deactivates all listeners.
	 * <li>{@code org.junit.*}: deactivates every listener under the {@code org.junit}
	 * base package and any of its subpackages.
	 * <li>{@code *.MyListener}: deactivates every listener whose simple class name is
	 * exactly {@code MyListener}.
	 * <li>{@code *System*, *Dev*}: deactivates every listener whose FQCN contains
	 * {@code System} or {@code Dev}.
	 * <li>{@code org.example.MyListener, org.example.TheirListener}: deactivates
	 * listeners whose FQCN is exactly {@code org.example.MyListener} or
	 * {@code org.example.TheirListener}.
	 * </ul>
	 *
	 * @see #DEACTIVATE_ALL_LISTENERS_PATTERN
	 * @see org.junit.platform.launcher.TestExecutionListener
	 */
	public static final String DEACTIVATE_LISTENERS_PATTERN_PROPERTY_NAME = "junit.platform.execution.listeners.deactivate";

	/**
	 * Wildcard pattern which signals that all listeners registered via the
	 * {@link java.util.ServiceLoader ServiceLoader} mechanism should be deactivated:
	 * {@value}
	 *
	 * @see #DEACTIVATE_LISTENERS_PATTERN_PROPERTY_NAME
	 * @see org.junit.platform.launcher.TestExecutionListener
	 */
	public static final String DEACTIVATE_ALL_LISTENERS_PATTERN = ClassNamePatternFilterUtils.DEACTIVATE_ALL_PATTERN;

	private LauncherConstants() {
		/* no-op */
	}

}
