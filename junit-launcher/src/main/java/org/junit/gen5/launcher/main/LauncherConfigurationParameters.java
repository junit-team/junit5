/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher.main;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ToStringBuilder;
import org.junit.gen5.engine.ConfigurationParameters;

/**
 * @since 5.0
 */
class LauncherConfigurationParameters implements ConfigurationParameters {

	private final Map<String, String> configurationParameters = new HashMap<>();

	@Override
	public Optional<String> get(String key) {
		Preconditions.notBlank(key, "key must not be null or empty");
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

	void addAll(Map<String, String> configurationParameters) {
		this.configurationParameters.putAll(configurationParameters);
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		this.configurationParameters.forEach(builder::append);
		return builder.toString();
	}

}
