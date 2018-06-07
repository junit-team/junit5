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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.STABLE;
import static org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy.CONFIG_CUSTOM_CLASS_PROPERTY_NAME;
import static org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy.CONFIG_DYNAMIC_FACTOR_PROPERTY_NAME;
import static org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy.CONFIG_FIXED_PARALLELISM_PROPERTY_NAME;
import static org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy.CONFIG_STRATEGY_PROPERTY_NAME;

import org.apiguardian.api.API;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfigurationStrategy;

/**
 * Collection of constants related to the {@link JupiterTestEngine}.
 *
 * @see org.junit.platform.engine.ConfigurationParameters
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

	/**
	 * Property name used to enable parallel test execution: {@value}
	 *
	 * <p>By default, tests are executed sequentially in a single thread.
	 *
	 * @since 5.3
	 */
	@API(status = EXPERIMENTAL, since = "5.3")
	public static final String PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME = "junit.jupiter.execution.parallel.enabled";

	static final String PARALLEL_CONFIG_PREFIX = "junit.jupiter.execution.parallel.config.";

	/**
	 * Property name used to select the
	 * {@link ParallelExecutionConfigurationStrategy}: {@value}
	 *
	 * <p>Potential values: {@code dynamic} (default), {@code fixed}, or
	 * {@code custom}.
	 *
	 * @since 5.3
	 */
	@API(status = EXPERIMENTAL, since = "5.3")
	public static final String PARALLEL_CONFIG_STRATEGY_PROPERTY_NAME = PARALLEL_CONFIG_PREFIX
			+ CONFIG_STRATEGY_PROPERTY_NAME;

	/**
	 * Property name used to set the desired parallelism for the {@code fixed}
	 * configuration strategy: {@value}
	 *
	 * <p>No default value; must be an integer.
	 *
	 * @since 5.3
	 */
	@API(status = EXPERIMENTAL, since = "5.3")
	public static final String PARALLEL_CONFIG_FIXED_PARALLELISM_PROPERTY_NAME = PARALLEL_CONFIG_PREFIX
			+ CONFIG_FIXED_PARALLELISM_PROPERTY_NAME;

	/**
	 * Property name used to set the factor to be multiplied with the number of
	 * available processors/cores to determine the desired parallelism for the
	 * {@code dynamic} configuration strategy: {@value}
	 *
	 * <p>Value must be a decimal number; defaults to {@code 1}.
	 *
	 * @since 5.3
	 */
	@API(status = EXPERIMENTAL, since = "5.3")
	public static final String PARALLEL_CONFIG_DYNAMIC_FACTOR_PROPERTY_NAME = PARALLEL_CONFIG_PREFIX
			+ CONFIG_DYNAMIC_FACTOR_PROPERTY_NAME;

	/**
	 * Property name used to specify the fully qualified class name of the
	 * {@link ParallelExecutionConfigurationStrategy} to be used for the
	 * {@code custom} configuration strategy: {@value}
	 *
	 * @since 5.3
	 */
	@API(status = EXPERIMENTAL, since = "5.3")
	public static final String PARALLEL_CONFIG_CUSTOM_CLASS_PROPERTY_NAME = PARALLEL_CONFIG_PREFIX
			+ CONFIG_CUSTOM_CLASS_PROPERTY_NAME;

	private Constants() {
		/* no-op */
	}

}
