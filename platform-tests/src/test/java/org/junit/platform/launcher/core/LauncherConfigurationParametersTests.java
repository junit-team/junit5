/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

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
		assertThrows(PreconditionViolationException.class, () -> fromMapAndFile(emptyMap(), null));
		assertThrows(PreconditionViolationException.class, () -> fromMapAndFile(emptyMap(), ""));
		assertThrows(PreconditionViolationException.class, () -> fromMapAndFile(emptyMap(), "  "));
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
		ConfigurationParameters configParams = fromMapAndFile(emptyMap(), CONFIG_FILE_NAME);
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
		ConfigurationParameters configParams = fromMapAndFile(singletonMap(KEY, CONFIG_PARAM), CONFIG_FILE_NAME);
		assertThat(configParams.get(KEY)).contains(CONFIG_PARAM);
		assertThat(configParams.toString()).contains(CONFIG_PARAM);
	}

	@Test
	void systemPropertyOverridesConfigFile() {
		System.setProperty(KEY, SYSTEM_PROPERTY);
		ConfigurationParameters configParams = fromMapAndFile(emptyMap(), CONFIG_FILE_NAME);
		assertThat(configParams.get(KEY)).contains(SYSTEM_PROPERTY);
		assertThat(configParams.toString()).contains(CONFIG_FILE);
	}

	@Test
	void getValueInExtensionContext() {
		LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request() //
				.configurationParameter("thing", "one else!") //
				.selectors(DiscoverySelectors.selectClass(Something.class)).build();
		SummaryGeneratingListener summary = new SummaryGeneratingListener();
		LauncherFactory.create().execute(request, summary);
		assertEquals(0, summary.getSummary().getTestsFailedCount());
	}

	@Test
	void getWithSuccessfulTransformer() {
		ConfigurationParameters configParams = fromMap(singletonMap(KEY, "42"));
		assertThat(configParams.get(KEY, Integer::valueOf)).contains(42);
	}

	@Test
	void getWithErroneousTransformer() {
		ConfigurationParameters configParams = fromMap(singletonMap(KEY, "42"));
		JUnitException exception = assertThrows(JUnitException.class, () -> configParams.get(KEY, input -> {
			throw new RuntimeException("foo");
		}));
		assertThat(exception).hasMessageContaining(
			"Failed to transform configuration parameter with key '" + KEY + "' and initial value '42'");
	}

	@Test
	void ignoresSystemPropertyAndConfigFileWhenImplicitLookupsAreDisabled() {
		System.setProperty(KEY, SYSTEM_PROPERTY);
		ConfigurationParameters configParams = LauncherConfigurationParameters.builder() //
				.enableImplicitProviders(false) //
				.build();
		assertThat(configParams.get(KEY)).isEmpty();
	}

	private static LauncherConfigurationParameters fromMap(Map<String, String> map) {
		return LauncherConfigurationParameters.builder().explicitParameters(map).build();
	}

	private static LauncherConfigurationParameters fromMapAndFile(Map<String, String> map, String configFileName) {
		return LauncherConfigurationParameters.builder() //
				.explicitParameters(map) //
				.configFileName(configFileName) //
				.build();
	}

	private static class Mutator implements TestInstancePostProcessor {
		@Override
		public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
			String value = context.getConfigurationParameter("thing").orElse("thing");
			Something.class.getField("thing").set(testInstance, value);
		}
	}

	@ExtendWith(Mutator.class)
	static class Something {

		// `public` is needed for simple "Class#getField(String)" to work
		public String thing = "body.";

		@Test
		void some() {
			assertEquals("Someone else!", "Some" + thing);
		}
	}

}
