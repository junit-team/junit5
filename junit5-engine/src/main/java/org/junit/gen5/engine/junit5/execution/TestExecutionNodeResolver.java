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
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.JavaClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.JavaMethodTestDescriptor;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
public class TestExecutionNodeResolver {

	public static TestExecutionNode forDescriptor(TestDescriptor testDescriptor) {
		if (testDescriptor instanceof JavaMethodTestDescriptor) {
			return new JavaMethodTestExecutionNode((JavaMethodTestDescriptor) testDescriptor);
		}
		else if (testDescriptor instanceof JavaClassTestDescriptor) {
			return new JavaClassTestExecutionNode((JavaClassTestDescriptor) testDescriptor);
		}
		else if (testDescriptor instanceof EngineDescriptor) {
			return new RunAllChildrenTestExecutionNode((EngineDescriptor) testDescriptor);
		}
		else {
			throw new UnsupportedOperationException(
				"Engine expects that classes implementing the TestDescriptor interface do also implement the TestExecutor interface!");
		}
	}
}