/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.config;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Optional;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.commons.util.ClassNamePatternFilterUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ConfigurationParameters;

/**
 * Default implementation of the {@link JupiterConfiguration} API.
 *
 * @since 5.4
 */
@API(status = INTERNAL, since = "5.4")
public class DefaultJupiterConfiguration implements JupiterConfiguration {

	private static final EnumConfigurationParameterConverter<ExecutionMode> executionModeConverter = //
		new EnumConfigurationParameterConverter<>(ExecutionMode.class, "parallel execution mode");

	private static final EnumConfigurationParameterConverter<Lifecycle> lifecycleConverter = //
		new EnumConfigurationParameterConverter<>(Lifecycle.class, "test instance lifecycle mode");

	private static final DisplayNameGeneratorParameterConverter displayNameGeneratorConverter = //
		new DisplayNameGeneratorParameterConverter();

	private final ConfigurationParameters configurationParameters;

	public DefaultJupiterConfiguration(ConfigurationParameters configurationParameters) {
		this.configurationParameters = Preconditions.notNull(configurationParameters,
			"ConfigurationParameters must not be null");
	}

	@Override
	public Optional<String> getRawConfigurationParameter(String key) {
		return configurationParameters.get(key);
	}

	@Override
	public boolean isParallelExecutionEnabled() {
		return configurationParameters.getBoolean(PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME).orElse(false);
	}

	@Override
	public boolean isExtensionAutoDetectionEnabled() {
		return configurationParameters.getBoolean(EXTENSIONS_AUTODETECTION_ENABLED_PROPERTY_NAME).orElse(false);
	}

	@Override
	public ExecutionMode getDefaultExecutionMode() {
		return executionModeConverter.get(configurationParameters, DEFAULT_EXECUTION_MODE_PROPERTY_NAME,
			ExecutionMode.SAME_THREAD);
	}

	@Override
	public ExecutionMode getDefaultClassesExecutionMode() {
		return executionModeConverter.get(configurationParameters, DEFAULT_CLASSES_EXECUTION_MODE_PROPERTY_NAME,
			getDefaultExecutionMode());
	}

	@Override
	public Lifecycle getDefaultTestInstanceLifecycle() {
		return lifecycleConverter.get(configurationParameters, DEFAULT_TEST_INSTANCE_LIFECYCLE_PROPERTY_NAME,
			Lifecycle.PER_METHOD);
	}

	@Override
	public Predicate<ExecutionCondition> getExecutionConditionFilter() {
		return ClassNamePatternFilterUtils.excludeMatchingClasses(
			configurationParameters.get(DEACTIVATE_CONDITIONS_PATTERN_PROPERTY_NAME).orElse(null));
	}

	@Override
	public DisplayNameGenerator getDefaultDisplayNameGenerator() {
		return displayNameGeneratorConverter.get(configurationParameters, DEFAULT_DISPLAY_NAME_GENERATOR_PROPERTY_NAME,
			DisplayNameGenerator.Standard::new);
	}
}
