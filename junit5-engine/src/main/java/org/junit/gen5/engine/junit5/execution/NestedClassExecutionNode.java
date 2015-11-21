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

import java.util.List;
import java.util.Optional;

import org.junit.gen5.api.extension.TestExecutionContext;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;

/**
 * @since 5.0
 */
class NestedClassExecutionNode extends ClassExecutionNode {

	NestedClassExecutionNode(ClassTestDescriptor testDescriptor) {
		super(testDescriptor);
	}

	@Override
	protected void createTestInstanceAndUpdateContext(TestExecutionContext context) {
		ClassExecutionNode parent = (ClassExecutionNode) getParent();
		parent.createTestInstanceAndUpdateContext(context);
		Object testInstance = ReflectionUtils.newInstance(getTestDescriptor().getTestClass(),
			context.getTestInstance().get());

		((DescriptorBasedTestExecutionContext) context).setTestInstance(testInstance);
		postProcessTestInstance(context);
	}

	@Override
	void executeBeforeEachTest(TestExecutionContext methodContext, TestExecutionContext resolutionContext,
			Object testInstance) throws Exception {
		executeBeforeEachTestOfParent(methodContext, resolutionContext, testInstance);
		super.executeBeforeEachTest(methodContext, resolutionContext, testInstance);
	}

	private void executeBeforeEachTestOfParent(TestExecutionContext methodContext,
			TestExecutionContext resolutionContext, Object testInstance) throws Exception {
		Optional<Object> optionalParentInstance = ReflectionUtils.getOuterInstance(testInstance);
		if (optionalParentInstance.isPresent()) {
			getParent().executeBeforeEachTest(methodContext, resolutionContext.getParent().get(),
				optionalParentInstance.get());
		}
	}

	@Override
	void executeAfterEachTest(TestExecutionContext methodContext, TestExecutionContext resolutionContext,
			Object testInstance, List<Throwable> exceptionCollector) {

		super.executeAfterEachTest(methodContext, resolutionContext, testInstance, exceptionCollector);
		executeAfterEachTestOfParent(methodContext, resolutionContext, testInstance, exceptionCollector);
	}

	private void executeAfterEachTestOfParent(TestExecutionContext methodContext,
			TestExecutionContext resolutionContext, Object testInstance, List<Throwable> exceptionsCollector) {
		Optional<Object> optionalParentInstance = ReflectionUtils.getOuterInstance(testInstance);
		optionalParentInstance.ifPresent(parentInstance -> {
			getParent().executeAfterEachTest(methodContext, resolutionContext.getParent().get(), parentInstance,
				exceptionsCollector);
		});
	}
}
