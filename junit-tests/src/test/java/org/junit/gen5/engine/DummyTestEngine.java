/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

public final class DummyTestEngine extends HierarchicalTestEngine<DummyEngineExecutionContext> {

	private final String engineId;
	private final EngineDescriptor root;

	public DummyTestEngine() {
		this("dummy");
	}

	public DummyTestEngine(String engineId) {
		this.engineId = engineId;
		this.root = new EngineDescriptor(this);
	}

	@Override
	public String getId() {
		return engineId;
	}

	public TestDescriptor addTest(String uniqueName, Runnable runnable) {
		DummyTestDescriptor child = new DummyTestDescriptor(engineId + ":" + uniqueName, uniqueName, runnable);
		root.addChild(child);
		return child;
	}

	@Override
	public EngineAwareTestDescriptor discoverTests(DiscoveryRequest discoveryRequest) {
		return root;
	}

	@Override
	protected DummyEngineExecutionContext createExecutionContext(ExecutionRequest request) {
		return new DummyEngineExecutionContext();
	}
}
