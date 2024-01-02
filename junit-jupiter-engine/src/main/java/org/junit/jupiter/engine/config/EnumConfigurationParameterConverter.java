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

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Locale;
import java.util.Optional;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ConfigurationParameters;

/**
 * @since 5.4
 */
@API(status = INTERNAL, since = "5.8")
public class EnumConfigurationParameterConverter<E extends Enum<E>> {

	private static final Logger logger = LoggerFactory.getLogger(EnumConfigurationParameterConverter.class);

	private final Class<E> enumType;
	private final String enumDisplayName;

	public EnumConfigurationParameterConverter(Class<E> enumType, String enumDisplayName) {
		this.enumType = enumType;
		this.enumDisplayName = enumDisplayName;
	}

	E get(ConfigurationParameters configParams, String key, E defaultValue) {
		Preconditions.notNull(configParams, "ConfigurationParameters must not be null");

		return get(key, configParams::get, defaultValue);
	}

	public E get(String key, Function<String, Optional<String>> lookup, E defaultValue) {

		Optional<String> value = lookup.apply(key);

		if (value.isPresent()) {
			String constantName = null;
			try {
				constantName = value.get().trim().toUpperCase(Locale.ROOT);
				E result = Enum.valueOf(enumType, constantName);
				logger.config(() -> String.format("Using %s '%s' set via the '%s' configuration parameter.",
					enumDisplayName, result, key));
				return result;
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
