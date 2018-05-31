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

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.ConfigurationParameters;

public enum DefaultParallelExecutionConfigurationStrategy implements ParallelExecutionConfigurationStrategy {

	FIXED {
		@Override
		public ParallelExecutionConfiguration createConfiguration(ConfigurationParameters configurationParameters) {
			int parallelism = configurationParameters.get(CONFIG_FIXED_PARALLELISM, Integer::valueOf).orElseThrow(
				() -> new JUnitException(CONFIG_FIXED_PARALLELISM + " must be set"));
			return new DefaultParallelExecutionConfiguration(parallelism, parallelism, 256 + parallelism, parallelism,
				30);
		}
	},

	DYNAMIC {
		@Override
		public ParallelExecutionConfiguration createConfiguration(ConfigurationParameters configurationParameters) {
			BigDecimal factor = configurationParameters.get(CONFIG_DYNAMIC_FACTOR, BigDecimal::new).orElse(
				BigDecimal.ONE);
			Preconditions.condition(factor.compareTo(BigDecimal.ZERO) > 0,
				() -> CONFIG_DYNAMIC_FACTOR + " must be greater than 0");
			int parallelism = Math.max(1,
				factor.multiply(BigDecimal.valueOf(Runtime.getRuntime().availableProcessors())).intValue());
			return new DefaultParallelExecutionConfiguration(parallelism, parallelism, 256 + parallelism, parallelism,
				30);
		}
	},

	CUSTOM {
		@Override
		public ParallelExecutionConfiguration createConfiguration(ConfigurationParameters configurationParameters) {
			String className = configurationParameters.get(CONFIG_CUSTOM_CLASS).orElseThrow(
				() -> new JUnitException(CONFIG_CUSTOM_CLASS + " must be set"));
			Class<?> strategyClass = ReflectionUtils.loadClass(className).orElseThrow(
				() -> new JUnitException("Could not load class for " + CONFIG_CUSTOM_CLASS));
			Preconditions.condition(ParallelExecutionConfigurationStrategy.class.isAssignableFrom(strategyClass),
				CONFIG_CUSTOM_CLASS + " does not implement " + ParallelExecutionConfigurationStrategy.class);
			ParallelExecutionConfigurationStrategy strategy = (ParallelExecutionConfigurationStrategy) ReflectionUtils.newInstance(
				strategyClass);
			return strategy.createConfiguration(configurationParameters);
		}
	};

	public static final String CONFIG_STRATEGY = "strategy";
	public static final String CONFIG_FIXED_PARALLELISM = "fixed.parallelism";
	public static final String CONFIG_DYNAMIC_FACTOR = "dynamic.factor";
	public static final String CONFIG_CUSTOM_CLASS = "custom.class";

	static ParallelExecutionConfigurationStrategy getStrategy(ConfigurationParameters configurationParameters) {
		return valueOf(configurationParameters.get(CONFIG_STRATEGY).orElse("dynamic").toUpperCase());
	}

}
