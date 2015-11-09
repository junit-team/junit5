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

import lombok.Value;

@Value
public class EngineExecutionContext {

	private EngineDescriptor engineDescriptor;

	private TestExecutionListener testExecutionListener;

	private Map<String, Object> attributes = new HashMap<>();

}