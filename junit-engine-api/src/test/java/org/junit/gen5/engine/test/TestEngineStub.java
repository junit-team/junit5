/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.test;

import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.UniqueId;

/**
 * @since 5.0
 */
public class TestEngineStub implements TestEngine {

	private final String id;

	public TestEngineStub() {
		this(TestEngineStub.class.getSimpleName());
	}

	public TestEngineStub(String id) {
		this.id = id;
	}

	@Override
	public String getId() {
		return this.id;
	}

	@Override
	public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
		return new TestDescriptorStub(UniqueId.forEngine(getId()), getId());
	}

	@Override
	public void execute(ExecutionRequest request) {
	}

}
