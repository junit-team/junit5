/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4;

import lombok.Value;

import org.junit.gen5.engine.EngineDescriptor;
import org.junit.runner.Description;
import org.junit.runner.Runner;

@Value
class RunnerTestDescriptor implements JUnit4TestDescriptor {

	EngineDescriptor parent;
	Runner runner;
	Description description;

	public RunnerTestDescriptor(EngineDescriptor parent, Runner runner) {
		this.parent = parent;
		this.runner = runner;
		this.description = runner.getDescription();
	}

	@Override
	public Description getDescription() {
		return description;
	}

}
