/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.STABLE;
import static org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy.CONFIG_CUSTOM_CLASS_PROPERTY_NAME;
import static org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy.CONFIG_DYNAMIC_FACTOR_PROPERTY_NAME;
import static org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy.CONFIG_FIXED_PARALLELISM_PROPERTY_NAME;
import static org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy.CONFIG_STRATEGY_PROPERTY_NAME;

import org.apiguardian.api.API;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfigurationStrategy;

/**
 * Collection of constants related to the {@link JupiterTestEngine}.
 *
 * <h3 id="supported-values-timeouts">Supported Values for Timeouts</h3>
 *
 * <p>Values for timeouts must be in the following, case-insensitive format:
 * {@code <number> [ns|μs|ms|s|m|h|d]}. The space between the number and the
 * unit may be omitted. Specifying no unit is equivalent to using seconds.
 *
 * <table class="plain">
 * <tr><th> Value         </th><th> Equivalent annotation                             </th></tr>
 * <tr><td> {@code 42}    </td><td> {@code @Timeout(42)}                              </td></tr>
 * <tr><td> {@code 42 ns} </td><td> {@code @Timeout(value = 42, unit = NANOSECONDS)}  </td></tr>
 * <tr><td> {@code 42 μs} </td><td> {@code @Timeout(value = 42, unit = MICROSECONDS)} </td></tr>
 * <tr><td> {@code 42 ms} </td><td> {@code @Timeout(value = 42, unit = MILLISECONDS)} </td></tr>
 * <tr><td> {@code 42 s}  </td><td> {@code @Timeout(value = 42, unit = SECONDS)}      </td></tr>
 * <tr><td> {@code 42 m}  </td><td> {@code @Timeout(value = 42, unit = MINUTES)}      </td></tr>
 * <tr><td> {@code 42 h}  </td><td> {@code @Timeout(value = 42, unit = HOURS)}        </td></tr>
 * <tr><td> {@code 42 d}  </td><td> {@code @Timeout(value = 42, unit = DAYS)}         </td></tr>
 * </table>
 *
 * @see org.junit.platform.engine.ConfigurationParameters
 * @since 5.0
 */
@API(status = STABLE, since = "5.0")
public final class Constants {

	/**
	 * Property name used to provide patterns for deactivating conditions: {@value}
	 *
	 * <h3>Pattern Matching Syntax</h3>
	 *
	 * <p>If the property value consists solely of an asterisk ({@code *}), all
	 * conditions will be deactivated. Otherwise, the property value will be treated
	 * as a comma-separated list of patterns where each individual pattern will be
	 * matched against the fully qualified class name (<em>FQCN</em>) of each registered
	 * condition. Any dot ({@code .}) in a pattern will match against a dot ({@code .})
	 * or a dollar sign ({@code $}) in a FQCN. Any asterisk ({@code *}) will match
	 * against one or more characters in a FQCN. All other characters in a pattern
	 * will be matched one-to-one against a FQCN.
	 *
	 * <h3>Examples</h3>
	 *
	 * <ul>
	 * <li>{@code *}: deactivates all conditions.
	 * <li>{@code org.junit.*}: deactivates every condition under the {@code org.junit}
	 * base package and any of its subpackages.
	 * <li>{@code *.MyCondition}: deactivates every condition whose simple class name is
	 * exactly {@code MyCondition}.
	 * <li>{@code *System*}: deactivates every condition whose FQCN contains
	 * {@code System}.
	 * <li>{@code *System*, *Dev*}: deactivates every condition whose FQCN contains
	 * {@code System} or {@code Dev}.
	 * <li>{@code org.example.MyCondition, org.example.TheirCondition}: deactivates
	 * conditions whose FQCN is exactly {@code org.example.MyCondition} or
	 * {@code org.example.TheirCondition}.
	 * </ul>
	 *
	 * @see #DEACTIVATE_ALL_CONDITIONS_PATTERN
	 * @see org.junit.jupiter.api.extension.ExecutionCondition
	 */
	public static final String DEACTIVATE_CONDITIONS_PATTERN_PROPERTY_NAME = JupiterConfiguration.DEACTIVATE_CONDITIONS_PATTERN_PROPERTY_NAME;

	/**
	 * Wildcard pattern which signals that all conditions should be deactivated: {@value}
	 *
	 * @see #DEACTIVATE_CONDITIONS_PATTERN_PROPERTY_NAME
	 * @see org.junit.jupiter.api.extension.ExecutionCondition
	 */
	public static final String DEACTIVATE_ALL_CONDITIONS_PATTERN = JupiterConfiguration.DEACTIVATE_ALL_CONDITIONS_PATTERN;

	/**
	 * Property name used to set the default display name generator class name: {@value}
	 *
	 * <h3>Supported Values</h3>
	 *
	 * <p>Supported values include fully qualified class names for types that implement
	 * {@link org.junit.jupiter.api.DisplayNameGenerator}.
	 *
	 * <p>If not specified, the default is
	 * {@link org.junit.jupiter.api.DisplayNameGenerator.Standard}.
	 */
	public static final String DEFAULT_DISPLAY_NAME_GENERATOR_PROPERTY_NAME = JupiterConfiguration.DEFAULT_DISPLAY_NAME_GENERATOR_PROPERTY_NAME;

	/**
	 * Property name used to enable auto-detection and registration of extensions via
	 * Java's {@link java.util.ServiceLoader} mechanism: {@value}
	 *
	 * <p>The default behavior is not to perform auto-detection.
	 */
	public static final String EXTENSIONS_AUTODETECTION_ENABLED_PROPERTY_NAME = JupiterConfiguration.EXTENSIONS_AUTODETECTION_ENABLED_PROPERTY_NAME;

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
	public static final String DEFAULT_TEST_INSTANCE_LIFECYCLE_PROPERTY_NAME = JupiterConfiguration.DEFAULT_TEST_INSTANCE_LIFECYCLE_PROPERTY_NAME;

	/**
	 * Property name used to enable parallel test execution: {@value}
	 *
	 * <p>By default, tests are executed sequentially in a single thread.
	 *
	 * @since 5.3
	 */
	@API(status = EXPERIMENTAL, since = "5.3")
	public static final String PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME = JupiterConfiguration.PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME;

	/**
	 * Property name used to set the default test execution mode: {@value}
	 *
	 * <p>This setting is only effective if parallel execution is enabled.
	 *
	 * <h3>Supported Values</h3>
	 *
	 * <p>Supported values include names of enum constants defined in
	 * {@link org.junit.jupiter.api.parallel.ExecutionMode}, ignoring case.
	 *
	 * <p>If not specified, the default is "same_thread" which corresponds to
	 * {@code @Execution(ExecutionMode.SAME_THREAD)}.
	 *
	 * @see org.junit.jupiter.api.parallel.Execution
	 * @see org.junit.jupiter.api.parallel.ExecutionMode
	 * @since 5.4
	 */
	@API(status = EXPERIMENTAL, since = "5.4")
	public static final String DEFAULT_PARALLEL_EXECUTION_MODE = JupiterConfiguration.DEFAULT_EXECUTION_MODE_PROPERTY_NAME;

	/**
	 * Property name used to set the default test execution mode for top-level
	 * classes: {@value}
	 *
	 * <p>This setting is only effective if parallel execution is enabled.
	 *
	 * <h3>Supported Values</h3>
	 *
	 * <p>Supported values include names of enum constants defined in
	 * {@link org.junit.jupiter.api.parallel.ExecutionMode}, ignoring case.
	 *
	 * <p>If not specified, it will be resolved into the same value as
	 * {@link #DEFAULT_PARALLEL_EXECUTION_MODE}.
	 *
	 * @see org.junit.jupiter.api.parallel.Execution
	 * @see org.junit.jupiter.api.parallel.ExecutionMode
	 * @since 5.4
	 */
	@API(status = EXPERIMENTAL, since = "5.5")
	public static final String DEFAULT_CLASSES_EXECUTION_MODE_PROPERTY_NAME = JupiterConfiguration.DEFAULT_CLASSES_EXECUTION_MODE_PROPERTY_NAME;

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
	 * <p>No default value; must be a positive integer.
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
	 * <p>Value must be a positive decimal number; defaults to {@code 1}.
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

	/**
	 * Property name used to set the default timeout for all testable and
	 * lifecycle methods.
	 *
	 * <p>The value of this property will be used unless overridden by a more
	 * specific property or a {@link org.junit.jupiter.api.Timeout @Timeout}
	 * annotation present on the method or on an enclosing test class (for testable
	 * methods).
	 *
	 * <p>Please refer to the <a href="#supported-values-timeouts">class
	 * description</a> for the definition of supported values.
	 *
	 * @see org.junit.jupiter.api.Timeout
	 * @since 5.5
	 */
	@API(status = EXPERIMENTAL, since = "5.5")
	public static final String DEFAULT_TIMEOUT_PROPERTY_NAME = JupiterConfiguration.DEFAULT_TIMEOUT_PROPERTY_NAME;

	/**
	 * Property name used to set the default timeout for all testable methods.
	 *
	 * <p>The value of this property will be used unless overridden by a more
	 * specific property or a {@link org.junit.jupiter.api.Timeout @Timeout}
	 * annotation present on the testable method or on an enclosing test class.
	 *
	 * <p>This property overrides the {@value #DEFAULT_TIMEOUT_PROPERTY_NAME}
	 * property.
	 *
	 * <p>Please refer to the <a href="#supported-values-timeouts">class
	 * description</a> for the definition of supported values.
	 *
	 * @see org.junit.jupiter.api.Timeout
	 * @since 5.5
	 */
	@API(status = EXPERIMENTAL, since = "5.5")
	public static final String DEFAULT_TESTABLE_METHOD_TIMEOUT_PROPERTY_NAME = JupiterConfiguration.DEFAULT_TESTABLE_METHOD_TIMEOUT_PROPERTY_NAME;

	/**
	 * Property name used to set the default timeout for all
	 * {@link org.junit.jupiter.api.Test @Test} methods.
	 *
	 * <p>The value of this property will be used unless overridden by a
	 * {@link org.junit.jupiter.api.Timeout @Timeout} annotation present on the
	 * {@link org.junit.jupiter.api.Test @Test} method or on an enclosing test
	 * class.
	 *
	 * <p>This property overrides the
	 * {@value #DEFAULT_TESTABLE_METHOD_TIMEOUT_PROPERTY_NAME} property.
	 *
	 * <p>Please refer to the <a href="#supported-values-timeouts">class
	 * description</a> for the definition of supported values.
	 *
	 * @see org.junit.jupiter.api.Timeout
	 * @since 5.5
	 */
	@API(status = EXPERIMENTAL, since = "5.5")
	public static final String DEFAULT_TEST_METHOD_TIMEOUT_PROPERTY_NAME = JupiterConfiguration.DEFAULT_TEST_METHOD_TIMEOUT_PROPERTY_NAME;

	/**
	 * Property name used to set the default timeout for all
	 * {@link org.junit.jupiter.api.TestTemplate @TestTemplate} methods.
	 *
	 * <p>The value of this property will be used unless overridden by a
	 * {@link org.junit.jupiter.api.Timeout @Timeout} annotation present on the
	 * {@link org.junit.jupiter.api.TestTemplate @TestTemplate} method or on an
	 * enclosing test class.
	 *
	 * <p>This property overrides the
	 * {@value #DEFAULT_TESTABLE_METHOD_TIMEOUT_PROPERTY_NAME} property.
	 *
	 * <p>Please refer to the <a href="#supported-values-timeouts">class
	 * description</a> for the definition of supported values.
	 *
	 * @see org.junit.jupiter.api.Timeout
	 * @since 5.5
	 */
	@API(status = EXPERIMENTAL, since = "5.5")
	public static final String DEFAULT_TEST_TEMPLATE_METHOD_TIMEOUT_PROPERTY_NAME = JupiterConfiguration.DEFAULT_TEST_TEMPLATE_METHOD_TIMEOUT_PROPERTY_NAME;

	/**
	 * Property name used to set the default timeout for all
	 * {@link org.junit.jupiter.api.TestFactory @TestFactory} methods.
	 *
	 * <p>The value of this property will be used unless overridden by a
	 * {@link org.junit.jupiter.api.Timeout @Timeout} annotation present on the
	 * {@link org.junit.jupiter.api.TestFactory @TestFactory} method or on an
	 * enclosing test class.
	 *
	 * <p>This property overrides the
	 * {@value #DEFAULT_TESTABLE_METHOD_TIMEOUT_PROPERTY_NAME} property.
	 *
	 * <p>Please refer to the <a href="#supported-values-timeouts">class
	 * description</a> for the definition of supported values.
	 *
	 * @see org.junit.jupiter.api.Timeout
	 * @since 5.5
	 */
	@API(status = EXPERIMENTAL, since = "5.5")
	public static final String DEFAULT_TEST_FACTORY_METHOD_TIMEOUT_PROPERTY_NAME = JupiterConfiguration.DEFAULT_TEST_FACTORY_METHOD_TIMEOUT_PROPERTY_NAME;

	/**
	 * Property name used to set the default timeout for all lifecycle methods.
	 *
	 * <p>The value of this property will be used unless overridden by a more
	 * specific property or a {@link org.junit.jupiter.api.Timeout @Timeout}
	 * annotation present on the lifecycle method.
	 *
	 * <p>This property overrides the {@value #DEFAULT_TIMEOUT_PROPERTY_NAME}
	 * property.
	 *
	 * <p>Please refer to the <a href="#supported-values-timeouts">class
	 * description</a> for the definition of supported values.
	 *
	 * @see org.junit.jupiter.api.Timeout
	 * @since 5.5
	 */
	@API(status = EXPERIMENTAL, since = "5.5")
	public static final String DEFAULT_LIFECYCLE_METHOD_TIMEOUT_PROPERTY_NAME = JupiterConfiguration.DEFAULT_LIFECYCLE_METHOD_TIMEOUT_PROPERTY_NAME;

	/**
	 * Property name used to set the default timeout for all
	 * {@link org.junit.jupiter.api.BeforeAll @BeforeAll} methods.
	 *
	 * <p>The value of this property will be used unless overridden by a
	 * {@link org.junit.jupiter.api.Timeout @Timeout} annotation present on the
	 * {@link org.junit.jupiter.api.BeforeAll @BeforeAll} method.
	 *
	 * <p>This property overrides the
	 * {@value #DEFAULT_LIFECYCLE_METHOD_TIMEOUT_PROPERTY_NAME} property.
	 *
	 * <p>Please refer to the <a href="#supported-values-timeouts">class
	 * description</a> for the definition of supported values.
	 *
	 * @see org.junit.jupiter.api.Timeout
	 * @since 5.5
	 */
	@API(status = EXPERIMENTAL, since = "5.5")
	public static final String DEFAULT_BEFORE_ALL_METHOD_TIMEOUT_PROPERTY_NAME = JupiterConfiguration.DEFAULT_BEFORE_ALL_METHOD_TIMEOUT_PROPERTY_NAME;

	/**
	 * Property name used to set the default timeout for all
	 * {@link org.junit.jupiter.api.BeforeEach @BeforeEach} methods.
	 *
	 * <p>The value of this property will be used unless overridden by a
	 * {@link org.junit.jupiter.api.Timeout @Timeout} annotation present on the
	 * {@link org.junit.jupiter.api.BeforeEach @BeforeEach} method.
	 *
	 * <p>This property overrides the
	 * {@value #DEFAULT_LIFECYCLE_METHOD_TIMEOUT_PROPERTY_NAME} property.
	 *
	 * <p>Please refer to the <a href="#supported-values-timeouts">class
	 * description</a> for the definition of supported values.
	 *
	 * @see org.junit.jupiter.api.Timeout
	 * @since 5.5
	 */
	@API(status = EXPERIMENTAL, since = "5.5")
	public static final String DEFAULT_BEFORE_EACH_METHOD_TIMEOUT_PROPERTY_NAME = JupiterConfiguration.DEFAULT_BEFORE_EACH_METHOD_TIMEOUT_PROPERTY_NAME;

	/**
	 * Property name used to set the default timeout for all
	 * {@link org.junit.jupiter.api.AfterEach @AfterEach} methods.
	 *
	 * <p>The value of this property will be used unless overridden by a
	 * {@link org.junit.jupiter.api.Timeout @Timeout} annotation present on the
	 * {@link org.junit.jupiter.api.AfterEach @AfterEach} method.
	 *
	 * <p>This property overrides the
	 * {@value #DEFAULT_LIFECYCLE_METHOD_TIMEOUT_PROPERTY_NAME} property.
	 *
	 * <p>Please refer to the <a href="#supported-values-timeouts">class
	 * description</a> for the definition of supported values.
	 *
	 * @see org.junit.jupiter.api.Timeout
	 * @since 5.5
	 */
	@API(status = EXPERIMENTAL, since = "5.5")
	public static final String DEFAULT_AFTER_EACH_METHOD_TIMEOUT_PROPERTY_NAME = JupiterConfiguration.DEFAULT_AFTER_EACH_METHOD_TIMEOUT_PROPERTY_NAME;

	/**
	 * Property name used to set the default timeout for all
	 * {@link org.junit.jupiter.api.AfterAll @AfterAll} methods.
	 *
	 * <p>The value of this property will be used unless overridden by a
	 * {@link org.junit.jupiter.api.Timeout @Timeout} annotation present on the
	 * {@link org.junit.jupiter.api.AfterAll @AfterAll} method.
	 *
	 * <p>This property overrides the
	 * {@value #DEFAULT_LIFECYCLE_METHOD_TIMEOUT_PROPERTY_NAME} property.
	 *
	 * <p>Please refer to the <a href="#supported-values-timeouts">class
	 * description</a> for the definition of supported values.
	 *
	 * @see org.junit.jupiter.api.Timeout
	 * @since 5.5
	 */
	@API(status = EXPERIMENTAL, since = "5.5")
	public static final String DEFAULT_AFTER_ALL_METHOD_TIMEOUT_PROPERTY_NAME = JupiterConfiguration.DEFAULT_AFTER_ALL_METHOD_TIMEOUT_PROPERTY_NAME;

	/**
	 * Property used to determine if timeouts are applied to tests: {@value}.
	 *
	 * <p>The value of this property will be used to toggle whether
	 * {@link org.junit.jupiter.api.Timeout @Timeout} is applied to tests.</p>
	 *
	 * <h3>Supported timeout mode values:</h3>
	 * <ul>
	 * <li>{@code enabled}: enables timeouts
	 * <li>{@code disabled}: disables timeouts
	 * <li>{@code disabled_on_debug}: disables timeouts while debugging
	 * </ul>
	 *
	 * <p>If not specified, the default is {@code "enabled"}.
	 *
	 * @since 5.6
	 */
	@API(status = EXPERIMENTAL, since = "5.6")
	public static final String TIMEOUT_MODE_PROPERTY_NAME = JupiterConfiguration.TIMEOUT_MODE_PROPERTY_NAME;

	private Constants() {
		/* no-op */
	}

}
