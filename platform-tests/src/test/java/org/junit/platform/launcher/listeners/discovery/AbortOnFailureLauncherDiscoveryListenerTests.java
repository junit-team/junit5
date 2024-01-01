/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.listeners.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;
import static org.junit.platform.launcher.core.LauncherFactoryForTestingPurposesOnly.createLauncher;
import static org.junit.platform.launcher.listeners.discovery.LauncherDiscoveryListeners.abortOnFailure;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.fakes.TestEngineStub;

class AbortOnFailureLauncherDiscoveryListenerTests extends AbstractLauncherDiscoveryListenerTests {

	@Test
	void abortsDiscoveryOnUnresolvedUniqueIdSelectorWithEnginePrefix() {
		var engine = createEngineThatCannotResolveAnything("some-engine");
		var request = request() //
				.listeners(abortOnFailure()) //
				.selectors(selectUniqueId(UniqueId.forEngine(engine.getId()))) //
				.build();
		var launcher = createLauncher(engine);

		var exception = assertThrows(JUnitException.class, () -> launcher.discover(request));
		assertThat(exception).hasMessage("TestEngine with ID 'some-engine' failed to discover tests");
		assertThat(exception.getCause()).hasMessage(
			"UniqueIdSelector [uniqueId = [engine:some-engine]] could not be resolved");
	}

	@Test
	void doesNotAbortDiscoveryOnUnresolvedUniqueIdSelectorWithoutEnginePrefix() {
		var engine = createEngineThatCannotResolveAnything("some-engine");
		var request = request() //
				.listeners(abortOnFailure()) //
				.selectors(selectUniqueId(UniqueId.forEngine("some-other-engine"))) //
				.build();
		var launcher = createLauncher(engine);

		assertDoesNotThrow(() -> launcher.discover(request));
	}

	@Test
	void abortsDiscoveryOnSelectorResolutionFailure() {
		var rootCause = new RuntimeException();
		var engine = createEngineThatFailsToResolveAnything("some-engine", rootCause);
		var request = request() //
				.listeners(abortOnFailure()) //
				.selectors(selectClass(Object.class)) //
				.build();
		var launcher = createLauncher(engine);

		var exception = assertThrows(JUnitException.class, () -> launcher.discover(request));
		assertThat(exception).hasMessage("TestEngine with ID 'some-engine' failed to discover tests");
		assertThat(exception.getCause()) //
				.hasMessageEndingWith("resolution failed") //
				.hasCauseReference(rootCause);
	}

	@Test
	void abortsDiscoveryOnEngineDiscoveryFailure() {
		var rootCause = new RuntimeException();
		var engine = new TestEngineStub("some-engine") {
			@Override
			public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
				throw rootCause;
			}
		};
		var request = request() //
				.listeners(abortOnFailure()) //
				.selectors(selectUniqueId(UniqueId.forEngine(engine.getId()))) //
				.build();
		var launcher = createLauncher(engine);

		var exception = assertThrows(JUnitException.class, () -> launcher.discover(request));
		assertThat(exception) //
				.hasMessage("TestEngine with ID 'some-engine' failed to discover tests") //
				.hasCauseReference(rootCause);
	}

}
