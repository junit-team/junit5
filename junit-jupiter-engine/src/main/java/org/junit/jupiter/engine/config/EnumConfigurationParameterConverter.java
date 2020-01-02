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
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ConfigurationParameters;

/**
 * @since 5.4
 */
class EnumConfigurationParameterConverter<E extends Enum<E>> {

	private static final Logger logger = LoggerFactory.getLogger(EnumConfigurationParameterConverter.class);

	private final Class<E> enumType;
	private final String enumDisplayName;

	EnumConfigurationParameterConverter(Class<E> enumType, String enumDisplayName) {
		this.enumType = enumType;
		this.enumDisplayName = enumDisplayName;
	}

	E get(ConfigurationParameters configParams, String key, E defaultValue) {
		Preconditions.notNull(configParams, "ConfigurationParameters must not be null");

		Optional<String> optional = configParams.get(key);
		String constantName = null;
		if (optional.isPresent()) {
			try {
				constantName = optional.get().trim().toUpperCase();
				E value = Enum.valueOf(enumType, constantName);
				logger.info(() -> String.format("Using %s '%s' set via the '%s' configuration parameter.",
					enumDisplayName, value, key));
				return value;
			}
			catch (Exception ex) {
				// local copy necessary for use in lambda expression
				String constant = constantName;
				logger.warn(() -> String.format(
					"Invalid %s '%s' set via the '%s' configuration parameter. "
							+ "Falling back to the %s default value.",
					enumDisplayName, constant, key, defaultValue.name()));
			}
		}

		return defaultValue;
	}

}
