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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.gen5.api.Condition.Result;
import org.junit.gen5.api.extension.MethodParameterResolver;
import org.junit.gen5.api.extension.TestExecutionContext;
import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.opentestalliance.TestSkippedException;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
abstract class TestExecutionNode {

	private TestExecutionNode parent;

	private List<TestExecutionNode> children = new LinkedList<>();

	private final ConditionEvaluator conditionEvaluator = new ConditionEvaluator();

	void addChild(TestExecutionNode child) {
		this.children.add(child);
		child.parent = this;
	}

	final TestExecutionNode getParent() {
		return this.parent;
	}

	final List<TestExecutionNode> getChildren() {
		return Collections.unmodifiableList(this.children);
	}

	abstract TestDescriptor getTestDescriptor();

	abstract void execute(ExecutionRequest request, TestExecutionContext context);

	protected final boolean isTestDisabled(ExecutionRequest request, TestExecutionContext context) {
		Result result = this.conditionEvaluator.evaluate(context);
		if (!result.isSuccess()) {
			// TODO Determine if we really need an explicit TestSkippedException.
			request.getTestExecutionListener().testSkipped(getTestDescriptor(),
				new TestSkippedException(buildTestSkippedMessage(result, context)));
			return true;
		}
		return false;
	}

	protected String buildTestSkippedMessage(Result result, TestExecutionContext context) {
		return String.format("Skipped [%s]; reason: %s", context.getDisplayName(),
			result.getReason().orElse("unknown"));
	}

	protected void executeChild(TestExecutionNode child, ExecutionRequest request, TestExecutionContext parentContext,
			Object testInstance) {

		TestExecutionContext childContext = createChildContext(child, parentContext, testInstance);
		child.execute(request, childContext);
	}

	private TestExecutionContext createChildContext(TestExecutionNode child, TestExecutionContext parentContext,
			Object testInstance) {
		return new DescriptorBasedTestExecutionContext(child.getTestDescriptor(), parentContext, testInstance);
	}

	void executeBeforeEachTest(TestExecutionContext methodContext, TestExecutionContext resolutionContext,
			Object testInstance) {
	}

	void executeAfterEachTest(TestExecutionContext methodContext, TestExecutionContext resolutionContext,
			Object testInstance, List<Throwable> exceptionsCollector) {
	}

	protected void invokeMethodInContext(Method method, TestExecutionContext methodContext,
			Set<MethodParameterResolver> parameterResolvers, Object target) {
		MethodInvoker methodInvoker = new MethodInvoker(method, target, parameterResolvers);
		methodInvoker.invoke(methodContext);
	}

	protected void invokeMethodInContextWithAggregatingExceptions(Method method, TestExecutionContext methodContext,
			Set<MethodParameterResolver> parameterResolvers, Object target, List<Throwable> exceptionsCollector) {

		try {
			invokeMethodInContext(method, methodContext, parameterResolvers, target);
		}
		catch (Throwable currentException) {
			// InvocationTargetException already handled in ReflectionUtils.invokeMethod()
			exceptionsCollector.add(currentException);
		}
	}

}
