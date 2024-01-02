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

import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.SelectorResolutionResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.fakes.TestEngineStub;

abstract class AbstractLauncherDiscoveryListenerTests {

	protected TestEngineStub createEngineThatCannotResolveAnything(String engineId) {
		return new TestEngineStub(engineId) {
			@Override
			public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
				discoveryRequest.getSelectorsByType(DiscoverySelector.class) //
						.forEach(selector -> discoveryRequest.getDiscoveryListener().selectorProcessed(uniqueId,
							selector, SelectorResolutionResult.unresolved()));
				return new EngineDescriptor(uniqueId, "Some Engine");
			}
		};
	}

	protected TestEngineStub createEngineThatFailsToResolveAnything(String engineId, RuntimeException rootCause) {
		return new TestEngineStub(engineId) {
			@Override
			public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
				discoveryRequest.getSelectorsByType(DiscoverySelector.class) //
						.forEach(selector -> discoveryRequest.getDiscoveryListener().selectorProcessed(uniqueId,
							selector, SelectorResolutionResult.failed(rootCause)));
				return new EngineDescriptor(uniqueId, "Some Engine");
			}
		};
	}

}
