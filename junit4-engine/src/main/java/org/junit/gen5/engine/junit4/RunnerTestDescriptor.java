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

import org.junit.runner.Description;
import org.junit.runner.Runner;

class RunnerTestDescriptor extends JUnit4TestDescriptor {

	final Runner runner;

	final Description description;

	RunnerTestDescriptor(Runner runner) {
		// TODO Use unique ID if set, too
		super(ENGINE_ID + ":" + runner.getDescription().getDisplayName());
		this.runner = runner;
		this.description = runner.getDescription();
	}

	@Override
	public Description getDescription() {
		return description;
	}

	public Runner getRunner() {
		return runner;
	}

}
