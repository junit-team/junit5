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

import java.util.Optional;

import org.junit.gen5.api.extension.TestExecutionContext;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;

/**
 * @since 5.0
 */
class ContextTestExecutionNode extends ClassTestExecutionNode {

	ContextTestExecutionNode(ClassTestDescriptor testDescriptor) {
		super(testDescriptor);
	}

	@Override
	public Object createTestInstance() {
		Object parentInstance = ((ClassTestExecutionNode) getParent()).createTestInstance();
		return ReflectionUtils.newInstance(getTestDescriptor().getTestClass(), parentInstance);
	}

	@Override
	public void executeBeforeEachTest(TestExecutionContext context, Object testInstance) {
		executeBeforeEachTestOfParent(context, testInstance);
		super.executeBeforeEachTest(context, testInstance);
	}

	private void executeBeforeEachTestOfParent(TestExecutionContext context, Object testInstance) {
		Optional<Object> optionalParentInstance = ReflectionUtils.getOuterInstance(testInstance);
		optionalParentInstance.ifPresent(parentInstance -> {
			getParent().executeBeforeEachTest(context.getParent().get(), parentInstance);
		});
	}

	@Override
	// TODO Change the exception thing into an exception aggregator
	public Throwable executeAfterEachTest(TestExecutionContext context, Object testInstance,
			Throwable previousException) {
		super.executeAfterEachTest(context, testInstance, previousException);
		previousException = executeAfterEachTestOfParent(context, testInstance, previousException);
		return previousException;
	}

	private Throwable executeAfterEachTestOfParent(TestExecutionContext context, Object testInstance,
			Throwable previousException) {
		Optional<Object> optionalParentInstance = ReflectionUtils.getOuterInstance(testInstance);
		Throwable[] previousExceptionContainer = new Throwable[1];
		optionalParentInstance.ifPresent(parentInstance -> {
			previousExceptionContainer[0] = getParent().executeAfterEachTest(context, parentInstance,
				previousException);
		});
		return previousExceptionContainer[0];
	}
}
