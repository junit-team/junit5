/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.test.TestEngineStub;
import org.junit.platform.launcher.TestExecutionListener;

/**
 * Unit tests for {@link LauncherConfig}.
 *
 * @since 1.3
 */
class LauncherConfigBuilderTests {

	@Test
	void defaultConfig() {
		LauncherConfig config = LauncherConfig.builder().build();

		assertTrue(config.isTestEngineAutoRegistrationEnabled(),
			"Test engines auto registration should be enabled by default");
		assertTrue(config.isTestExecutionListenerAutoRegistrationEnabled(),
			"Test execution listeners auto registration should be enabled by default");

		assertThat(config.getAdditionalTestEngines()).isEmpty();

		assertThat(config.getAdditionalTestExecutionListeners()).isEmpty();

	}

	@Test
	void disableTestEngineAutoRegistration() {
		LauncherConfig config = LauncherConfig.builder().setTestEngineAutoRegistrationEnabled(false).build();

		assertFalse(config.isTestEngineAutoRegistrationEnabled());
	}

	@Test
	void disableTestExecutionListenerAutoRegistration() {
		LauncherConfig config = LauncherConfig.builder().setTestExecutionListenerAutoRegistrationEnabled(false).build();

		assertFalse(config.isTestExecutionListenerAutoRegistrationEnabled());
	}

	@Test
	void addAdditionalTestEngine() {
		TestEngine first = new TestEngineStub();
		TestEngine second = new TestEngineStub();

		LauncherConfig config = LauncherConfig.builder().addAdditionalTestEngines(first, second).build();

		assertThat(config.getAdditionalTestEngines()).containsOnly(first, second);
	}

	@Test
	void addAdditionalTestExecutionListener() {
		TestExecutionListener first = new TestExecutionListener() {
		};
		TestExecutionListener second = new TestExecutionListener() {
		};

		LauncherConfig config = LauncherConfig.builder().addAdditionalTestExecutionListeners(first, second).build();

		assertThat(config.getAdditionalTestExecutionListeners()).containsOnly(first, second);
	}
}
