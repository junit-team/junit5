/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.config;

import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.platform.commons.function.Try;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.ConfigurationParameters;

/**
 * @since 5.5
 */
class DisplayNameGeneratorParameterConverter {

	private static final Logger logger = LoggerFactory.getLogger(DisplayNameGeneratorParameterConverter.class);

	DisplayNameGenerator get(ConfigurationParameters configurationParameters, String key) {
		// @formatter:off
        return configurationParameters.get(key)
                .map(String::trim)
                .filter(className -> !className.isEmpty())
                .<Class<? extends DisplayNameGenerator>>map(this::getClassFrom)
                .<DisplayNameGenerator>map(clazz -> ReflectionUtils.newInstance(clazz))
                .orElseGet(DisplayNameGenerator.Standard::new);
        // @formatter:on
	}

	@SuppressWarnings({ "unchecked" })
	private Class<? extends DisplayNameGenerator> getClassFrom(String className) {
		// @formatter:off
        return ReflectionUtils.tryToLoadClass(className)
                .andThen(generatorClass -> {
                    if (DisplayNameGenerator.class.isAssignableFrom(generatorClass)) {
                        return Try.success((Class<? extends DisplayNameGenerator>) generatorClass);
                    } else {
                        return Try.failure(classCastException(generatorClass));
                    }
                }).ifSuccess(generatorClass -> logGeneratorClassMessage(className))
                .ifFailure(cause -> logFailureMessage(className, cause))
                .toOptional()
                .orElse(null);
        // @formatter:on
	}

	private void logFailureMessage(String className, Exception cause) {
		logger.warn(cause,
			() -> String.format(
				"Failed to load default display name generator class '%s' set via the '%s' configuration parameter."
						+ " Falling back to default behaviour.",
				className, JupiterConfiguration.DEFAULT_DISPLAY_NAME_GENERATOR_PROPERTY_NAME));
	}

	private void logGeneratorClassMessage(String className) {
		logger.info(
			() -> String.format("Using default display name generator '%s' set via the '%s' configuration parameter.",
				className, JupiterConfiguration.DEFAULT_DISPLAY_NAME_GENERATOR_PROPERTY_NAME));
	}

	private ClassCastException classCastException(Class<?> generatorClass) {
		return new ClassCastException(
			String.format("Can not cast '%s' to org.junit.jupiter.api.DisplayNameGenerator", generatorClass));
	}

}
