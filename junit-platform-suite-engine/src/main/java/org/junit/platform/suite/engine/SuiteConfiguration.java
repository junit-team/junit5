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
import org.junit.platform.engine.UniqueId;

class SuiteConfiguration {
	static final String PARENT_SUITE_ID = "junit.platform.suite.engine.parent.suite-id";

	private final ConfigurationParameters configurationParameters;

	SuiteConfiguration(ConfigurationParameters configurationParameters) {
		this.configurationParameters = configurationParameters;
	}

	Optional<UniqueId> parentSuiteId() {
		return configurationParameters.get(PARENT_SUITE_ID, UniqueId::parse);
	}

}
