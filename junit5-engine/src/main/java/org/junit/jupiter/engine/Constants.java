/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine;

/**
 * Collection of constants related to the {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */
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
	 * @see org.junit.jupiter.api.extension.ContainerExecutionCondition
	 * @see org.junit.jupiter.api.extension.TestExecutionCondition
	 */
	public static final String DEACTIVATE_CONDITIONS_PATTERN_PROPERTY_NAME = "junit.conditions.deactivate";

	/**
	 * Wildcard pattern which signals that all conditions should be deactivated: {@value}
	 *
	 * @see #DEACTIVATE_CONDITIONS_PATTERN_PROPERTY_NAME
	 * @see org.junit.jupiter.api.extension.ContainerExecutionCondition
	 * @see org.junit.jupiter.api.extension.TestExecutionCondition
	 */
	public static final String DEACTIVATE_ALL_CONDITIONS_PATTERN = "*";

	private Constants() {
		/* no-op */
	}

}
