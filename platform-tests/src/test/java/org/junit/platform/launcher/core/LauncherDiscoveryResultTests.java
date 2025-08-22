/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.core.LauncherDiscoveryResult.EngineResultInfo;

/**
 * Unit tests for {@link LauncherDiscoveryResult}.
 */
class LauncherDiscoveryResultTests {

	/**
	 * @see <a href="https://github.com/junit-team/junit-framework/issues/4862">GitHub issue #4862</>
	 */
	@Test
	void withRetainedEnginesRetainsLinkedHashMapSemantics() {
		TestEngine engine1 = new DummyEngine1();
		TestEngine engine2 = new DummyEngine2();
		TestEngine engine3 = new DummyEngine3();
		TestEngine engine4 = new DummyEngine4();

		TestDescriptor rootDescriptor1 = mock();
		TestDescriptor rootDescriptor2 = mock();
		TestDescriptor rootDescriptor3 = mock();
		TestDescriptor rootDescriptor4 = mock();
		when(rootDescriptor1.isTest()).thenReturn(true);
		when(rootDescriptor2.isTest()).thenReturn(false);
		when(rootDescriptor3.isTest()).thenReturn(false);
		when(rootDescriptor4.isTest()).thenReturn(true);

		EngineResultInfo engineResultInfo1 = mock();
		EngineResultInfo engineResultInfo2 = mock();
		EngineResultInfo engineResultInfo3 = mock();
		EngineResultInfo engineResultInfo4 = mock();
		when(engineResultInfo1.getRootDescriptor()).thenReturn(rootDescriptor1);
		when(engineResultInfo2.getRootDescriptor()).thenReturn(rootDescriptor2);
		when(engineResultInfo3.getRootDescriptor()).thenReturn(rootDescriptor3);
		when(engineResultInfo4.getRootDescriptor()).thenReturn(rootDescriptor4);

		@SuppressWarnings("serial")
		Map<TestEngine, EngineResultInfo> engineResults = new LinkedHashMap<>() {
			{
				put(engine1, engineResultInfo1);
				put(engine2, engineResultInfo2);
				put(engine3, engineResultInfo3);
				put(engine4, engineResultInfo4);
			}
		};

		assertThat(engineResults.keySet()).containsExactly(engine1, engine2, engine3, engine4);

		LauncherDiscoveryResult discoveryResult = new LauncherDiscoveryResult(engineResults, mock(), mock());
		assertThat(discoveryResult.getTestEngines()).containsExactly(engine1, engine2, engine3, engine4);

		LauncherDiscoveryResult withRetainedEngines = discoveryResult.withRetainedEngines(TestDescriptor::isTest);

		assertThat(withRetainedEngines.getTestEngines()).containsExactly(engine1, engine4);
	}

	private static abstract class AbstractDummyEngine implements TestEngine {

		@Override
		public String getId() {
			return getClass().getSimpleName();
		}

		@Override
		public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
			throw new UnsupportedOperationException("discover");
		}

		@Override
		public void execute(ExecutionRequest request) {
			throw new UnsupportedOperationException("execute");
		}
	}

	private static class DummyEngine1 extends AbstractDummyEngine {
	}

	private static class DummyEngine2 extends AbstractDummyEngine {
	}

	private static class DummyEngine3 extends AbstractDummyEngine {
	}

	private static class DummyEngine4 extends AbstractDummyEngine {
	}

}
