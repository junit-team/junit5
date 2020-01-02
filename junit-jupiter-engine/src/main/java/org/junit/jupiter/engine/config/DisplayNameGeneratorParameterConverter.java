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
import java.util.function.Supplier;

import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.ConfigurationParameters;

/**
 * @since 5.5
 */
class DisplayNameGeneratorParameterConverter {

	private static final Logger logger = LoggerFactory.getLogger(DisplayNameGeneratorParameterConverter.class);

	DisplayNameGenerator get(ConfigurationParameters configurationParameters, String key,
			Supplier<? extends DisplayNameGenerator> defaultValueSupplier) {
		// @formatter:off
		return configurationParameters.get(key)
				.map(String::trim)
				.filter(className -> !className.isEmpty())
				.flatMap(className -> instantiateGenerator(className, key))
				.orElseGet(defaultValueSupplier);
		// @formatter:on
	}

	private Optional<DisplayNameGenerator> instantiateGenerator(String className, String key) {
		// @formatter:off
		return ReflectionUtils.tryToLoadClass(className)
				.andThenTry(ReflectionUtils::newInstance)
				.andThenTry(DisplayNameGenerator.class::cast)
				.ifSuccess(generator -> logGeneratorClassMessage(className, key))
				.ifFailure(cause -> logFailureMessage(className, key, cause))
				.toOptional();
		// @formatter:on
	}

	private void logFailureMessage(String className, String key, Exception cause) {
		logger.warn(cause,
			() -> String.format(
				"Failed to load default display name generator class '%s' set via the '%s' configuration parameter."
						+ " Falling back to default behavior.",
				className, key));
	}

	private void logGeneratorClassMessage(String className, String key) {
		logger.info(() -> String.format(
			"Using default display name generator '%s' set via the '%s' configuration parameter.", className, key));
	}

}
