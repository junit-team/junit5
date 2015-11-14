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
import java.util.Stack;

import lombok.Value;

@Value
public class ExecutionRequest {

	private EngineDescriptor engineDescriptor;

	private TestExecutionListener testExecutionListener;

	private Map<String, Object> attributes = new HashMap<>();

	private Stack<Object> testInstances = new Stack<>();

}