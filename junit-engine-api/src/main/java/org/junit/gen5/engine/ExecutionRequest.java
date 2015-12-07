/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import java.util.HashMap;
import java.util.Map;

public class ExecutionRequest {

	private final TestDescriptor rootTestDescriptor;

	private final EngineExecutionListener engineExecutionListener;

	private final Map<String, Object> attributes = new HashMap<>();

	public ExecutionRequest(TestDescriptor rootTestDescriptor, EngineExecutionListener engineExecutionListener) {
		this.rootTestDescriptor = rootTestDescriptor;
		this.engineExecutionListener = engineExecutionListener;
	}

	public TestDescriptor getRootTestDescriptor() {
		return rootTestDescriptor;
	}

	public EngineExecutionListener getEngineExecutionListener() {
		return engineExecutionListener;
	}

	public Map<String, Object> getAttributes() {
		return this.attributes;
	}

}