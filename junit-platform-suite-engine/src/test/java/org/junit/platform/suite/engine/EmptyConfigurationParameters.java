/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.engine;

import java.util.Optional;

import org.junit.platform.engine.ConfigurationParameters;

class EmptyConfigurationParameters implements ConfigurationParameters {

	@Override
	public Optional<String> get(String key) {
		return Optional.empty();
	}

	@Override
	public Optional<Boolean> getBoolean(String key) {
		return Optional.empty();
	}

	@Override
	public int size() {
		return 0;
	}

}
