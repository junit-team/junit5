/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.config;

import java.util.Optional;
import java.util.function.Supplier;

import org.junit.platform.commons.function.Try;
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
		return supply(configurationParameters, key).get();
	}

	Supplier<Optional<T>> supply(ConfigurationParameters configurationParameters, String key) {
		// @formatter:off
		return configurationParameters.get(key)
				.map(String::trim)
				.filter(className -> !className.isEmpty())
				.map(className -> newInstanceSupplier(className, key))
				.orElse(Optional::empty);
		// @formatter:on
	}

	private Supplier<Optional<T>> newInstanceSupplier(String className, String key) {
		Try<Class<?>> clazz = ReflectionUtils.tryToLoadClass(className);
		// @formatter:off
		return () -> clazz.andThenTry(ReflectionUtils::newInstance)
				.andThenTry(this.clazz::cast)
				.ifSuccess(generator -> logSuccessMessage(className, key))
				.ifFailure(cause -> logFailureMessage(className, key, cause))
				.toOptional();
		// @formatter:on
	}

	private void logFailureMessage(String className, String key, Exception cause) {
		logger.warn(cause,
			() -> String.format("Failed to load default %s class '%s' set via the '%s' configuration parameter."
					+ " Falling back to default behavior.",
				this.name, className, key));
	}

	private void logSuccessMessage(String className, String key) {
		logger.config(() -> String.format("Using default %s '%s' set via the '%s' configuration parameter.", this.name,
			className, key));
	}

}
