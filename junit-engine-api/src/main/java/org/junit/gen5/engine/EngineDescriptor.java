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

import lombok.Value;

@Value
public class EngineDescriptor implements TestDescriptor {

	private TestEngine engine;

	public EngineDescriptor(TestEngine engine) {
		this.engine = engine;
	}

	@Override
	public String getUniqueId() {
		return engine.getId();
	}

	@Override
	public String getDisplayName() {
		return "Test engine: " + engine.getId();
	}

	@Override
	public TestDescriptor getParent() {
		return null;
	}

	@Override
	public boolean isTest() {
		return false;
	}
}