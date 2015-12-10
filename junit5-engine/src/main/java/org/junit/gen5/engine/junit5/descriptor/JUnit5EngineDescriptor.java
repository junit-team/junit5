/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import org.junit.gen5.engine.Container;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.junit5.execution.JUnit5EngineExecutionContext;
import org.junit.gen5.engine.junit5.execution.TestExtensionRegistry;

public class JUnit5EngineDescriptor extends EngineDescriptor implements Container<JUnit5EngineExecutionContext> {

	public JUnit5EngineDescriptor(TestEngine engine) {
		super(engine);
	}

	@Override
	public JUnit5EngineExecutionContext beforeAll(JUnit5EngineExecutionContext context) {
		return context.extend().withTestExtensionRegistry(new TestExtensionRegistry()).build();
	}

}
