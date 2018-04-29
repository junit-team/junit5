/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.function.Function;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.ConfigurationParameters;

public enum DefaultParallelExecutionConfigurationStrategy implements ParallelExecutionConfigurationStrategy {

	FIXED {
		@Override
		public ParallelExecutionConfiguration createConfiguration(String engineSpecificPrefix,
				ConfigurationParameters configurationParameters) {
			String key = engineSpecificPrefix + "." + CONFIG_FIXED_PARALLELISM;
			int parallelism = getSafely(configurationParameters, key, Integer::valueOf).orElseThrow(
				() -> new JUnitException(key + " must be set"));
			return new DefaultParallelExecutionConfiguration(parallelism, parallelism, 256 + parallelism, parallelism,
				30);
		}
	},

	DYNAMIC {
		@Override
		public ParallelExecutionConfiguration createConfiguration(String engineSpecificPrefix,
				ConfigurationParameters configurationParameters) {
			String key = engineSpecificPrefix + "." + CONFIG_DYNAMIC_FACTOR;
			BigDecimal factor = getSafely(configurationParameters, key, BigDecimal::new).orElse(BigDecimal.ONE);
			Preconditions.condition(factor.compareTo(BigDecimal.ZERO) > 0, () -> key + " must be greater than 0");
			int parallelism = Math.max(1,
				factor.multiply(BigDecimal.valueOf(Runtime.getRuntime().availableProcessors())).intValue());
			return new DefaultParallelExecutionConfiguration(parallelism, parallelism, 256 + parallelism, parallelism,
				30);
		}
	},

	CUSTOM {
		@Override
		public ParallelExecutionConfiguration createConfiguration(String engineSpecificPrefix,
				ConfigurationParameters configurationParameters) {
			String key = engineSpecificPrefix + "." + CONFIG_CUSTOM_CLASS;
			String className = configurationParameters.get(key).orElseThrow(
				() -> new JUnitException(key + " must be set"));
			Class<?> strategyClass = ReflectionUtils.loadClass(className).orElseThrow(
				() -> new JUnitException("Could not load class for " + key));
			Preconditions.condition(ParallelExecutionConfigurationStrategy.class.isAssignableFrom(strategyClass),
				key + " does not implement " + ParallelExecutionConfigurationStrategy.class);
			ParallelExecutionConfigurationStrategy strategy = (ParallelExecutionConfigurationStrategy) ReflectionUtils.newInstance(
				strategyClass);
			return strategy.createConfiguration(engineSpecificPrefix, configurationParameters);
		}
	};

	public static final String CONFIG_STRATEGY = "strategy";
	public static final String CONFIG_FIXED_PARALLELISM = "fixed.parallelism";
	public static final String CONFIG_DYNAMIC_FACTOR = "dynamic.factor";
	public static final String CONFIG_CUSTOM_CLASS = "custom.class";

	private static <T> Optional<T> getSafely(ConfigurationParameters configurationParameters, String key,
			Function<String, T> transformation) {
		return configurationParameters.get(key).map(withExceptionHandling(key, transformation));
	}

	private static <T, R> Function<T, R> withExceptionHandling(String key, Function<T, R> transformation) {
		return input -> {
			try {
				return transformation.apply(input);
			}
			catch (RuntimeException exception) {
				throw new JUnitException(String.format("Failed to convert key '%s' for input: %s", key, input),
					exception);
			}
		};
	}

	static ParallelExecutionConfigurationStrategy getStrategy(String engineSpecificPrefix,
			ConfigurationParameters configurationParameters) {
		return valueOf(
			configurationParameters.get(engineSpecificPrefix + "." + CONFIG_STRATEGY).orElse("dynamic").toUpperCase());
	}

}
