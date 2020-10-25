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

import java.util.Optional;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.ConfigurationParameters;

/**
 * @since 5.5
 */
class InstantiatingConfigurationParameterConverter<T> {

	private static final Logger logger = LoggerFactory.getLogger(InstantiatingConfigurationParameterConverter.class);

	private final Class<T> clazz;
	private final String name;

	public InstantiatingConfigurationParameterConverter(Class<T> clazz, String name) {
		this.clazz = clazz;
		this.name = name;
	}

	Optional<T> get(ConfigurationParameters configurationParameters, String key) {
		// @formatter:off
		return configurationParameters.get(key)
				.map(String::trim)
				.filter(className -> !className.isEmpty())
				.flatMap(className -> instantiateGenerator(className, key));
		// @formatter:on
	}

	private Optional<T> instantiateGenerator(String className, String key) {
		// @formatter:off
		return ReflectionUtils.tryToLoadClass(className)
				.andThenTry(ReflectionUtils::newInstance)
				.andThenTry(clazz::cast)
				.ifSuccess(generator -> logSuccessMessage(className, key))
				.ifFailure(cause -> logFailureMessage(className, key, cause))
				.toOptional();
		// @formatter:on
	}

	private void logFailureMessage(String className, String key, Exception cause) {
		logger.warn(cause,
			() -> String.format("Failed to load default %s class '%s' set via the '%s' configuration parameter."
					+ " Falling back to default behavior.",
				name, className, key));
	}

	private void logSuccessMessage(String className, String key) {
		logger.info(() -> String.format("Using default %s '%s' set via the '%s' configuration parameter.", name,
			className, key));
	}

}
