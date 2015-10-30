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

import java.lang.reflect.InvocationTargetException;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.EngineExecutionContext;
import org.junit.gen5.engine.junit5.descriptor.JavaClassTestDescriptor;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
public class JavaClassTestExecutionNode extends TestExecutionNode<JavaClassTestDescriptor> {

	public JavaClassTestExecutionNode(JavaClassTestDescriptor testDescriptor) {
		super(testDescriptor);
	}

	@Override
	public Object createTestInstance() {
		try {
			return ReflectionUtils.newInstance(getTestDescriptor().getTestClass());
		}
		catch (InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException ex) {
			throw new IllegalStateException(
				String.format("Test %s is not well-formed and cannot be executed", getTestDescriptor().getUniqueId()),
				ex);
		}
	}

	@Override
	public void execute(EngineExecutionContext context) {
		for (TestExecutionNode child : getChildren()) {
			child.execute(context);
		}
	}
}
