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

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.engine.ConfigurationParameters;

/**
 * @since 5.0
 */
@API(Experimental)
class LauncherConfigurationParameters implements ConfigurationParameters {
	private final Map<String, String> configurationParameters = new HashMap<>();

	@Override
	public Optional<String> get(String key) {
		return Optional.ofNullable(this.configurationParameters.get(key));
	}

	@Override
	public int getSize() {
		return configurationParameters.size();
	}

	void addAll(Map<String, String> configurationParameters) {
		this.configurationParameters.putAll(configurationParameters);
	}
}
