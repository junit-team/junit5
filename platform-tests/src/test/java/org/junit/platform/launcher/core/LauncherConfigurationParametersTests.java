/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.engine.ConfigurationParameters;

/**
 * Unit tests for {@link LauncherConfigurationParameters}.
 *
 * @since 1.0
 */
class LauncherConfigurationParametersTests {

	private static final String CONFIG_FILE_NAME = "test-junit-platform.properties";
	private static final String KEY = LauncherConfigurationParametersTests.class.getName();
	private static final String CONFIG_PARAM = "explicit config param";
	private static final String CONFIG_FILE = "from config file";
	private static final String SYSTEM_PROPERTY = "system property";

	@BeforeEach
	@AfterEach
	void reset() {
		System.clearProperty(KEY);
	}

	@Test
	void constructorPreconditions() {
		assertThrows(PreconditionViolationException.class, () -> fromMap(null));
		assertThrows(PreconditionViolationException.class, () -> fromMap(emptyMap(), null));
		assertThrows(PreconditionViolationException.class, () -> fromMap(emptyMap(), ""));
		assertThrows(PreconditionViolationException.class, () -> fromMap(emptyMap(), "  "));
	}

	@Test
	void getPreconditions() {
		ConfigurationParameters configParams = fromMap(emptyMap());
		assertThrows(PreconditionViolationException.class, () -> configParams.get(null));
		assertThrows(PreconditionViolationException.class, () -> configParams.get(""));
		assertThrows(PreconditionViolationException.class, () -> configParams.get("  "));
	}

	@Test
	void noConfigParams() {
		ConfigurationParameters configParams = fromMap(emptyMap());
		assertThat(configParams.size()).isEqualTo(0);
		assertThat(configParams.get(KEY)).isEmpty();
		assertThat(configParams.toString()).doesNotContain(KEY);
	}

	@Test
	void explicitConfigParam() {
		ConfigurationParameters configParams = fromMap(singletonMap(KEY, CONFIG_PARAM));
		assertThat(configParams.get(KEY)).contains(CONFIG_PARAM);
		assertThat(configParams.toString()).contains(CONFIG_PARAM);
	}

	@Test
	void systemProperty() {
		System.setProperty(KEY, SYSTEM_PROPERTY);
		ConfigurationParameters configParams = fromMap(emptyMap());
		assertThat(configParams.get(KEY)).contains(SYSTEM_PROPERTY);
		assertThat(configParams.toString()).doesNotContain(KEY);
	}

	@Test
	void configFile() {
		ConfigurationParameters configParams = fromMap(emptyMap(), CONFIG_FILE_NAME);
		assertThat(configParams.get(KEY)).contains(CONFIG_FILE);
		assertThat(configParams.toString()).contains(CONFIG_FILE);
	}

	@Test
	void explicitConfigParamOverridesSystemProperty() {
		System.setProperty(KEY, SYSTEM_PROPERTY);
		ConfigurationParameters configParams = fromMap(singletonMap(KEY, CONFIG_PARAM));
		assertThat(configParams.get(KEY)).contains(CONFIG_PARAM);
		assertThat(configParams.toString()).contains(CONFIG_PARAM);
	}

	@Test
	void explicitConfigParamOverridesConfigFile() {
		ConfigurationParameters configParams = fromMap(singletonMap(KEY, CONFIG_PARAM), CONFIG_FILE_NAME);
		assertThat(configParams.get(KEY)).contains(CONFIG_PARAM);
		assertThat(configParams.toString()).contains(CONFIG_PARAM);
		assertThat(configParams.toString()).doesNotContain(CONFIG_FILE);
	}

	@Test
	void systemPropertyOverridesConfigFile() {
		System.setProperty(KEY, SYSTEM_PROPERTY);
		ConfigurationParameters configParams = fromMap(emptyMap(), CONFIG_FILE_NAME);
		assertThat(configParams.get(KEY)).contains(SYSTEM_PROPERTY);
		assertThat(configParams.toString()).contains(CONFIG_FILE);
	}

	private static LauncherConfigurationParameters fromMap(Map<String, String> map) {
		return new LauncherConfigurationParameters(map);
	}

	private static LauncherConfigurationParameters fromMap(Map<String, String> map, String configFileName) {
		return new LauncherConfigurationParameters(map, configFileName);
	}

}
