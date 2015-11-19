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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.gen5.api.Condition.Result;
import org.junit.gen5.api.extension.TestExecutionContext;
import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.junit5.descriptor.MethodTestDescriptor;
import org.opentestalliance.TestAbortedException;
import org.opentestalliance.TestSkippedException;

/**
 * @author Sam Brannen
 * @author Stefan Bechtold
 * @since 5.0
 */
class MethodTestExecutionNode extends TestExecutionNode {

	private final MethodTestDescriptor testDescriptor;

	MethodTestExecutionNode(MethodTestDescriptor testDescriptor) {
		this.testDescriptor = testDescriptor;
	}

	@Override
	MethodTestDescriptor getTestDescriptor() {
		return this.testDescriptor;
	}

	@Override
	void execute(ExecutionRequest request, TestExecutionContext context) {
		if (isTestDisabled(request, context)) {
			// Abort execution of the test completely at this point.
			return;
		}

		request.getTestExecutionListener().testStarted(getTestDescriptor());

		List<Throwable> exceptionsCollector = new ArrayList<>();

		try {
			executeBeforeEachMethods(context);
			invokeTestMethod(context.getTestMethod().get(), context);
		}
		catch (Throwable ex) {
			exceptionsCollector.add(ex);
		}
		finally {
			executeAfterEachMethods(context, exceptionsCollector);
		}

		if (!exceptionsCollector.isEmpty()) {
			Throwable mainException = wrapInCollectingException(exceptionsCollector);
			if (mainException instanceof TestSkippedException) {
				request.getTestExecutionListener().testSkipped(getTestDescriptor(), mainException);
			}
			else if (mainException instanceof TestAbortedException) {
				request.getTestExecutionListener().testAborted(getTestDescriptor(), mainException);
			}
			else {
				request.getTestExecutionListener().testFailed(getTestDescriptor(), mainException);
			}
		}
		else {
			request.getTestExecutionListener().testSucceeded(getTestDescriptor());
		}
	}

	protected Throwable wrapInCollectingException(List<Throwable> exceptionsCollector) {
		Throwable mainException = exceptionsCollector.remove(0);
		exceptionsCollector.stream().forEach(ex -> mainException.addSuppressed(ex));
		return mainException;
	}

	@Override
	protected String buildTestSkippedMessage(Result result, TestExecutionContext context) {
		return String.format("Skipped test method [%s]; reason: %s", context.getTestMethod().get().toGenericString(),
			result.getReason().orElse("unknown"));
	}

	private void invokeTestMethod(Method method, TestExecutionContext context) {
		Object target = context.getTestInstance().get();
		invokeMethodInContext(method, context, context.getParameterResolvers(), target);
	}

	private void executeBeforeEachMethods(TestExecutionContext context) {
		Object target = context.getTestInstance().get();
		getParent().executeBeforeEachTest(context, context.getParent().get(), target);
	}

	private void executeAfterEachMethods(TestExecutionContext context, List<Throwable> exceptionsCollector) {
		Object target = context.getTestInstance().get();
		getParent().executeAfterEachTest(context, context.getParent().get(), target, exceptionsCollector);
	}

}
