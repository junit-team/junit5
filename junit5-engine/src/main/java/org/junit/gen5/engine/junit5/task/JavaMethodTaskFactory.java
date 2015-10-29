/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.task;

import java.util.*;

import org.junit.gen5.engine.junit5.*;

public class JavaMethodTaskFactory {

	public ExecutionTask createJavaTestMethodTask(JavaMethodTestDescriptor testDescriptor, Object instance) {
		JavaTestMethodTask javaTestMethodTask = new JavaTestMethodTask(testDescriptor.getTestClass(),
			testDescriptor.getTestMethod(), instance);

		return new CompositeTask(
			Arrays.asList(new DummyTask("BEFORE TASK"), javaTestMethodTask, new DummyTask("AFTER TASK")));

	}

}
