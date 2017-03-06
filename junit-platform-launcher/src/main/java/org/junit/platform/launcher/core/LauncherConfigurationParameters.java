/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.launcher.core;

import java.util.Map;
import java.util.Optional;

import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.ConfigurationParameters;

/**
 * @since 1.0
 */
class LauncherConfigurationParameters implements ConfigurationParameters {

	private final Map<String, String> configurationParameters;

	LauncherConfigurationParameters(Map<String, String> configurationParameters) {
		Preconditions.notNull(configurationParameters, "configuration parameters must not be null");
		this.configurationParameters = configurationParameters;
	}

	@Override
	public Optional<String> get(String key) {
		Preconditions.notBlank(key, "key must not be null or blank");
		String value = this.configurationParameters.get(key);
		if (value == null) {
			try {
				value = System.getProperty(key);
			}
			catch (Exception ex) {
				/* ignore */
			}
		}
		return Optional.ofNullable(value);
	}

	@Override
	public int size() {
		return this.configurationParameters.size();
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		this.configurationParameters.forEach(builder::append);
		return builder.toString();
	}

}
