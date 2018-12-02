/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.config;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.ConfigurationParameters;

@API(status = INTERNAL, since = "5.4")
public class DefaultJupiterConfiguration implements JupiterConfiguration {

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
		EnumConfigurationParameterConverter<ExecutionMode> converter = new EnumConfigurationParameterConverter<>(
			ExecutionMode.class, "parallel execution mode");
		return converter.get(configurationParameters, DEFAULT_EXECUTION_MODE_PROPERTY_NAME, ExecutionMode.SAME_THREAD);
	}

	@Override
	public Lifecycle getDefaultTestInstanceLifecycle() {
		EnumConfigurationParameterConverter<Lifecycle> converter = new EnumConfigurationParameterConverter<>(
			Lifecycle.class, "test instance lifecycle mode");
		return converter.get(configurationParameters, DEFAULT_TEST_INSTANCE_LIFECYCLE_PROPERTY_NAME,
			Lifecycle.PER_METHOD);
	}

	@Override
	public Optional<String> getDeactivateExecutionConditionsPattern() {
		// @formatter:off
        return configurationParameters.get(DEACTIVATE_CONDITIONS_PATTERN_PROPERTY_NAME)
                .filter(StringUtils::isNotBlank)
                .map(String::trim);
        // @formatter:on
	}
}
