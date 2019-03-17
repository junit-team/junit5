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

import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.ConfigurationParameters;

/**
 * @since 5.5
 */
class DisplayNameGeneratorClassParameterConverter {

	Optional<Class<?>> get(ConfigurationParameters configurationParameters, String key) {
		return configurationParameters.get(key).filter(StringUtils::isNotBlank).map(String::trim).map(
			this::getClassFrom);
	}

	private Class<?> getClassFrom(String className) {
		try {
			return Class.forName(className);
		}
		catch (ClassNotFoundException cnfe) {
			return null;
		}
	}
}
