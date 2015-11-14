/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution;

import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.EngineExecutionContext;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
public class EngineTestExecutionNode extends TestExecutionNode {

	private final EngineDescriptor testDescriptor;

	public EngineTestExecutionNode(EngineDescriptor testDescriptor) {
		this.testDescriptor = testDescriptor;
	}

	@Override
	public EngineDescriptor getTestDescriptor() {
		return this.testDescriptor;
	}

	@Override
	public void execute(EngineExecutionContext context) {
		for (TestExecutionNode child : getChildren()) {
			child.execute(context);
		}
	}

	public void executeRequest(EngineExecutionContext context) {
		execute(context);
	}

}
