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

import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.Parent;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.junit5.JUnit5Context;
import org.junit.gen5.engine.junit5.execution.TestExtensionRegistry;

public class JUnit5EngineDescriptor extends EngineDescriptor implements Parent<JUnit5Context> {

	public JUnit5EngineDescriptor(TestEngine engine) {
		super(engine);
	}

	@Override
	public JUnit5Context beforeAll(JUnit5Context context) {
		return context.withTestExtensionRegistry(new TestExtensionRegistry());
	}

}
