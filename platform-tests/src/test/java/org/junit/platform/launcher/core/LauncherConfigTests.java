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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.fakes.TestEngineStub;
import org.junit.platform.launcher.TestExecutionListener;

/**
 * Unit tests for {@link LauncherConfig} and {@link LauncherConfig.Builder}.
 *
 * @since 1.3
 */
class LauncherConfigTests {

	@Test
	void preconditions() {
		assertThrows(PreconditionViolationException.class,
			() -> LauncherConfig.builder().addTestEngines((TestEngine[]) null));
		assertThrows(PreconditionViolationException.class,
			() -> LauncherConfig.builder().addTestExecutionListeners((TestExecutionListener[]) null));

		TestEngine engine = new TestEngineStub();
		TestExecutionListener listener = new TestExecutionListener() {
		};
		assertThrows(PreconditionViolationException.class,
			() -> LauncherConfig.builder().addTestEngines(engine, engine, null));
		assertThrows(PreconditionViolationException.class,
			() -> LauncherConfig.builder().addTestExecutionListeners(listener, listener, null));
	}

	@Test
	void defaultConfig() {
		LauncherConfig config = LauncherConfig.builder().build();

		assertTrue(config.isTestEngineAutoRegistrationEnabled(),
			"Test engine auto-registration should be enabled by default");
		assertTrue(config.isTestExecutionListenerAutoRegistrationEnabled(),
			"Test execution listener auto-registration should be enabled by default");

		assertThat(config.getAdditionalTestEngines()).isEmpty();

		assertThat(config.getAdditionalTestExecutionListeners()).isEmpty();
	}

	@Test
	void disableTestEngineAutoRegistration() {
		LauncherConfig config = LauncherConfig.builder().enableTestEngineAutoRegistration(false).build();

		assertFalse(config.isTestEngineAutoRegistrationEnabled());
	}

	@Test
	void disableTestExecutionListenerAutoRegistration() {
		LauncherConfig config = LauncherConfig.builder().enableTestExecutionListenerAutoRegistration(false).build();

		assertFalse(config.isTestExecutionListenerAutoRegistrationEnabled());
	}

	@Test
	void addTestEngines() {
		TestEngine first = new TestEngineStub();
		TestEngine second = new TestEngineStub();

		LauncherConfig config = LauncherConfig.builder().addTestEngines(first, second).build();

		assertThat(config.getAdditionalTestEngines()).containsOnly(first, second);
	}

	@Test
	void addTestExecutionListeners() {
		TestExecutionListener first = new TestExecutionListener() {
		};
		TestExecutionListener second = new TestExecutionListener() {
		};

		LauncherConfig config = LauncherConfig.builder().addTestExecutionListeners(first, second).build();

		assertThat(config.getAdditionalTestExecutionListeners()).containsOnly(first, second);
	}

}
