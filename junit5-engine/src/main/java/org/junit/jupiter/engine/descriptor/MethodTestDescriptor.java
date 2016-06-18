/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.descriptor;

import static java.util.stream.Collectors.toList;
import static org.junit.gen5.commons.meta.API.Usage.Internal;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.ExceptionUtils;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.StringUtils;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestTag;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.support.descriptor.JavaMethodSource;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.engine.execution.AfterEachMethodAdapter;
import org.junit.jupiter.engine.execution.BeforeEachMethodAdapter;
import org.junit.jupiter.engine.execution.ConditionEvaluator;
import org.junit.jupiter.engine.execution.ExecutableInvoker;
import org.junit.jupiter.engine.execution.JUnit5EngineExecutionContext;
import org.junit.jupiter.engine.execution.ThrowableCollector;
import org.junit.jupiter.engine.extension.ExtensionRegistry;

/**
 * {@link TestDescriptor} for tests based on Java methods.
 *
 * <h3>Default Display Names</h3>
 *
 * <p>The default display name for a test method is the name of the method
 * concatenated with a comma-separated list of parameter types in parentheses.
 * The names of parameter types are retrieved using {@link Class#getSimpleName()}.
 * For example, the default display name for the following test method is
 * {@code testUser(TestInfo, User)}.
 *
 * <pre style="code">
 *   {@literal @}Test
 *   void testUser(TestInfo testInfo, {@literal @}Mock User user) { ... }
 * </pre>
 *
 * @since 5.0
 */
@API(Internal)
public class MethodTestDescriptor extends JUnit5TestDescriptor {

	private static final ConditionEvaluator conditionEvaluator = new ConditionEvaluator();
	private static final ExecutableInvoker executableInvoker = new ExecutableInvoker();

	private final String displayName;
	private final Class<?> testClass;
	private final Method testMethod;

	public MethodTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method testMethod) {
		super(uniqueId);

		this.testClass = Preconditions.notNull(testClass, "Class must not be null");
		this.testMethod = Preconditions.notNull(testMethod, "Method must not be null");
		this.displayName = determineDisplayName(testMethod);

		setSource(new JavaMethodSource(testMethod));
	}

	// --- TestDescriptor ------------------------------------------------------

	@Override
	public final Set<TestTag> getTags() {
		Set<TestTag> methodTags = getTags(getTestMethod());
		getParent().ifPresent(parentDescriptor -> methodTags.addAll(parentDescriptor.getTags()));
		return methodTags;
	}

	@Override
	public final String getDisplayName() {
		return this.displayName;
	}

	public final Class<?> getTestClass() {
		return this.testClass;
	}

	public final Method getTestMethod() {
		return this.testMethod;
	}

	@Override
	public boolean isTest() {
		return true;
	}

	@Override
	public boolean isContainer() {
		return false;
	}

	@Override
	protected String generateDefaultDisplayName() {
		return String.format("%s(%s)", this.testMethod.getName(),
			StringUtils.nullSafeToString(Class::getSimpleName, this.testMethod.getParameterTypes()));
	}

	// --- Node ----------------------------------------------------------------

	@Override
	public JUnit5EngineExecutionContext prepare(JUnit5EngineExecutionContext context) throws Exception {
		ExtensionRegistry extensionRegistry = populateNewExtensionRegistryFromExtendWith(this.testMethod,
			context.getExtensionRegistry());
		Object testInstance = context.getTestInstanceProvider().getTestInstance();
		TestExtensionContext testExtensionContext = new MethodBasedTestExtensionContext(context.getExtensionContext(),
			context.getExecutionListener(), this, testInstance);

		// @formatter:off
		return context.extend()
				.withExtensionRegistry(extensionRegistry)
				.withExtensionContext(testExtensionContext)
				.build();
		// @formatter:on
	}

	@Override
	public SkipResult shouldBeSkipped(JUnit5EngineExecutionContext context) throws Exception {
		ConditionEvaluationResult evaluationResult = conditionEvaluator.evaluateForTest(context.getExtensionRegistry(),
			context.getConfigurationParameters(), (TestExtensionContext) context.getExtensionContext());
		if (evaluationResult.isDisabled()) {
			return SkipResult.skip(evaluationResult.getReason().orElse("<unknown>"));
		}
		return SkipResult.doNotSkip();
	}

	@Override
	public JUnit5EngineExecutionContext execute(JUnit5EngineExecutionContext context) throws Exception {
		ExtensionRegistry registry = context.getExtensionRegistry();
		TestExtensionContext testExtensionContext = (TestExtensionContext) context.getExtensionContext();
		ThrowableCollector throwableCollector = new ThrowableCollector();

		// @formatter:off
		invokeBeforeEachCallbacks(registry, testExtensionContext);
			invokeBeforeEachMethods(registry, testExtensionContext);
				invokeBeforeTestExecutionCallbacks(registry, testExtensionContext);
					invokeTestMethod(context, testExtensionContext, throwableCollector);
				invokeAfterTestExecutionCallbacks(registry, testExtensionContext, throwableCollector);
			invokeAfterEachMethods(registry, testExtensionContext, throwableCollector);
		invokeAfterEachCallbacks(registry, testExtensionContext, throwableCollector);
		// @formatter:on

		throwableCollector.assertEmpty();

		return context;
	}

	private void invokeBeforeEachCallbacks(ExtensionRegistry registry, TestExtensionContext context) {
		registry.stream(BeforeEachCallback.class)//
				.forEach(extension -> executeAndMaskThrowable(() -> extension.beforeEach(context)));
	}

	private void invokeBeforeEachMethods(ExtensionRegistry registry, TestExtensionContext context) {
		registry.stream(BeforeEachMethodAdapter.class)//
				.forEach(adapter -> executeAndMaskThrowable(() -> adapter.invokeBeforeEachMethod(context)));
	}

	private void invokeBeforeTestExecutionCallbacks(ExtensionRegistry registry, TestExtensionContext context) {
		registry.stream(BeforeTestExecutionCallback.class)//
				.forEach(extension -> executeAndMaskThrowable(() -> extension.beforeTestExecution(context)));
	}

	protected void invokeTestMethod(JUnit5EngineExecutionContext context, TestExtensionContext testExtensionContext,
			ThrowableCollector throwableCollector) {

		throwableCollector.execute(() -> {
			try {
				Method method = testExtensionContext.getTestMethod().get();
				Object instance = testExtensionContext.getTestInstance();
				executableInvoker.invoke(method, instance, testExtensionContext, context.getExtensionRegistry());
			}
			catch (Throwable throwable) {
				invokeTestExecutionExceptionHandlers(context.getExtensionRegistry(), testExtensionContext, throwable);
			}
		});
	}

	private void invokeTestExecutionExceptionHandlers(ExtensionRegistry registry, TestExtensionContext context,
			Throwable ex) {

		List<TestExecutionExceptionHandler> exceptionHandlers = registry.stream(
			TestExecutionExceptionHandler.class).collect(toList());

		invokeTestExecutionExceptionHandlers(ex, exceptionHandlers, context);
	}

	private void invokeTestExecutionExceptionHandlers(Throwable ex, List<TestExecutionExceptionHandler> handlers,
			TestExtensionContext context) {
		// No handlers left?
		if (handlers.isEmpty()) {
			ExceptionUtils.throwAsUncheckedException(ex);
		}

		try {
			// Invoke next available handler
			handlers.remove(0).handleTestExecutionException(context, ex);
		}
		catch (Throwable t) {
			invokeTestExecutionExceptionHandlers(t, handlers, context);
		}
	}

	private void invokeAfterTestExecutionCallbacks(ExtensionRegistry registry, TestExtensionContext context,
			ThrowableCollector throwableCollector) {

		registry.reverseStream(AfterTestExecutionCallback.class)//
				.forEach(extension -> throwableCollector.execute(() -> extension.afterTestExecution(context)));
	}

	private void invokeAfterEachMethods(ExtensionRegistry registry, TestExtensionContext context,
			ThrowableCollector throwableCollector) {

		registry.reverseStream(AfterEachMethodAdapter.class)//
				.forEach(adapter -> throwableCollector.execute(() -> adapter.invokeAfterEachMethod(context)));
	}

	private void invokeAfterEachCallbacks(ExtensionRegistry registry, TestExtensionContext context,
			ThrowableCollector throwableCollector) {

		registry.reverseStream(AfterEachCallback.class)//
				.forEach(extension -> throwableCollector.execute(() -> extension.afterEach(context)));
	}

}
