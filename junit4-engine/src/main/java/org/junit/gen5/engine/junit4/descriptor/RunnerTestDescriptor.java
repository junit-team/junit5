/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.descriptor;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.runner.Runner;

public class RunnerTestDescriptor extends JUnit4TestDescriptor {

	public RunnerTestDescriptor(TestDescriptor parent, Class<?> testClass, Runner runner) {
		super(parent, ':', testClass.getName(), runner.getDescription());
	}

}
