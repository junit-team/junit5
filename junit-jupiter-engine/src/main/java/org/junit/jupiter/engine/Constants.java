/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine;

import static org.apiguardian.api.API.Status.STABLE;

import org.apiguardian.api.API;

/**
 * Collection of constants related to the {@link JupiterTestEngine}.
 *
 * @since 5.0
 */
@API(status = STABLE, since = "5.0")
public final class Constants {

	/**
	 * Property name used to provide a pattern for deactivating conditions: {@value}
	 *
	 * <h3>Pattern Matching Syntax</h3>
	 *
	 * <p>If the pattern consists solely of an asterisk ({@code *}), all conditions
	 * will be deactivated. Otherwise, the pattern will be used to match against
	 * the fully qualified class name (<em>FQCN</em>) of each registered condition.
	 * Any dot ({@code .}) in the pattern will match against a dot ({@code .})
	 * or a dollar sign ({@code $}) in the FQCN. Any asterisk ({@code *}) will match
	 * against one or more characters in the FQCN. All other characters in the
	 * pattern will be matched one-to-one against the FQCN.
	 *
	 * <h3>Examples</h3>
	 *
	 * <ul>
	 * <li>{@code *}: deactivates all conditions.
	 * <li>{@code org.junit.*}: deactivates every condition under the {@code org.junit}
	 * base package and any of its subpackages.
	 * <li>{@code *.MyCondition}: deactivates every condition whose simple class name is
	 * exactly {@code MyCondition}.
	 * <li>{@code *System*}: deactivates every condition whose simple class name contains
	 * {@code System}.
	 * <li>{@code org.example.MyCondition}: deactivates the condition whose FQCN is
	 * exactly {@code org.example.MyCondition}.
	 * </ul>
	 *
	 * @see #DEACTIVATE_ALL_CONDITIONS_PATTERN
	 * @see org.junit.jupiter.api.extension.ExecutionCondition
	 */
	public static final String DEACTIVATE_CONDITIONS_PATTERN_PROPERTY_NAME = "junit.jupiter.conditions.deactivate";

	/**
	 * Wildcard pattern which signals that all conditions should be deactivated: {@value}
	 *
	 * @see #DEACTIVATE_CONDITIONS_PATTERN_PROPERTY_NAME
	 * @see org.junit.jupiter.api.extension.ExecutionCondition
	 */
	public static final String DEACTIVATE_ALL_CONDITIONS_PATTERN = "*";

	/**
	 * Property name used to enable auto-detection and registration of extensions via
	 * Java's {@link java.util.ServiceLoader} mechanism: {@value}
	 *
	 * <p>The default behavior is not to perform auto-detection.
	 */
	public static final String EXTENSIONS_AUTODETECTION_ENABLED_PROPERTY_NAME = "junit.jupiter.extensions.autodetection.enabled";

	/**
	 * Property name used to set the default test instance lifecycle mode: {@value}
	 *
	 * <h3>Supported Values</h3>
	 *
	 * <p>Supported values include names of enum constants defined in
	 * {@link org.junit.jupiter.api.TestInstance.Lifecycle}, ignoring case.
	 *
	 * <p>If not specified, the default is "per_method" which corresponds to
	 * {@code @TestInstance(Lifecycle.PER_METHOD)}.
	 *
	 * @see org.junit.jupiter.api.TestInstance
	 */
	public static final String DEFAULT_TEST_INSTANCE_LIFECYCLE_PROPERTY_NAME = "junit.jupiter.testinstance.lifecycle.default";

	private Constants() {
		/* no-op */
	}

}
