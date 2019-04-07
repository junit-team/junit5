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

import java.util.Optional;

import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.engine.ConfigurationParameters;

/**
 * @since 5.5
 */
class DisplayNameGeneratorClassParameterConverter {

	private static final Logger logger = LoggerFactory.getLogger(DisplayNameGeneratorClassParameterConverter.class);

	Optional<Class<? extends DisplayNameGenerator>> get(ConfigurationParameters configurationParameters, String key) {
		return configurationParameters.get(key).map(String::trim).map(this::getClassFrom);
	}

	@SuppressWarnings({ "unchecked" })
	private Class<? extends DisplayNameGenerator> getClassFrom(String className) {
		try {
			Class<?> aClass = Class.forName(className);
			if (DisplayNameGenerator.class.isAssignableFrom(aClass)) {
				logger.info(() -> String.format(
					"Using default display name generator '%s' set via the '%s' configuration parameter.", className,
					JupiterConfiguration.DEFAULT_DISPLAY_NAME_GENERATOR_PROPERTY_NAME));
				return (Class<? extends DisplayNameGenerator>) aClass;
			}
			else {
				logger.warn(() -> String.format(
					"Default display name generator class '%s' set via the '%s' configuration parameter "
							+ "is not of type `org.junit.jupiter.api.DisplayNameGenerator`."
							+ "Falling back to default behaviour.",
					className, JupiterConfiguration.DEFAULT_DISPLAY_NAME_GENERATOR_PROPERTY_NAME));
				return null;
			}
		}
		catch (ClassNotFoundException cnfe) {
			logger.warn(() -> String.format(
				"No default display name generator class '%s' set via the '%s' configuration parameter "
						+ "is not not found. Falling back to default behaviour.",
				className, JupiterConfiguration.DEFAULT_DISPLAY_NAME_GENERATOR_PROPERTY_NAME));
			return null;
		}
	}
}
