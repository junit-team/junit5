/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.support.hierarchical;

import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

/**
 * @since 1.0
 */
public final class DummyTestEngine extends HierarchicalTestEngine<DummyEngineExecutionContext> {

	private final String engineId;
	private final DummyEngineDescriptor engineDescriptor;

	public DummyTestEngine() {
		this("dummy");
	}

	public DummyTestEngine(String engineId) {
		this.engineId = engineId;
		this.engineDescriptor = new DummyEngineDescriptor(UniqueId.forEngine(getId()));
	}

	@Override
	public String getId() {
		return engineId;
	}

	public DummyEngineDescriptor getEngineDescriptor() {
		return engineDescriptor;
	}

	public DummyTestDescriptor addTest(String uniqueName, Runnable runnable) {
		return addTest(uniqueName, uniqueName, runnable);
	}

	public DummyTestDescriptor addTest(String uniqueName, String displayName, Runnable runnable) {
		UniqueId uniqueId = engineDescriptor.getUniqueId().append("test", uniqueName);
		DummyTestDescriptor child = new DummyTestDescriptor(uniqueId, displayName, runnable);
		engineDescriptor.addChild(child);
		return child;
	}

	@Override
	public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
		return engineDescriptor;
	}

	@Override
	protected DummyEngineExecutionContext createExecutionContext(ExecutionRequest request) {
		return new DummyEngineExecutionContext();
	}
}
