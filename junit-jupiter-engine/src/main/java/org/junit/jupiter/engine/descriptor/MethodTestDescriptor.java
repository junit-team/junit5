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

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.engine.execution.AfterEachMethodAdapter;
import org.junit.jupiter.engine.execution.BeforeEachMethodAdapter;
import org.junit.jupiter.engine.execution.ConditionEvaluator;
import org.junit.jupiter.engine.execution.ExecutableInvoker;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.execution.ThrowableCollector;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.JavaMethodSource;

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
public class MethodTestDescriptor extends JupiterTestDescriptor {

	private static final ConditionEvaluator conditionEvaluator = new ConditionEvaluator();
	private static final ExecutableInvoker executableInvoker = new ExecutableInvoker();

	private final Class<?> testClass;
	private final Method testMethod;

	public MethodTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method testMethod) {
		super(uniqueId, determineDisplayName(Preconditions.notNull(testMethod, "Method must not be null"),
			MethodTestDescriptor::generateDefaultDisplayName));

		this.testClass = Preconditions.notNull(testClass, "Class must not be null");
		this.testMethod = testMethod;

		setSource(new JavaMethodSource(testMethod));
	}

	// --- TestDescriptor ------------------------------------------------------

	@Override
	public final Set<TestTag> getTags() {
		Set<TestTag> methodTags = getTags(getTestMethod());
		getParent().ifPresent(parentDescriptor -> methodTags.addAll(parentDescriptor.getTags()));
		return methodTags;
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

	protected static String generateDefaultDisplayName(Method testMethod) {
		return String.format("%s(%s)", testMethod.getName(),
			StringUtils.nullSafeToString(Class::getSimpleName, testMethod.getParameterTypes()));
	}

	// --- Node ----------------------------------------------------------------

	@Override
	public JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) throws Exception {
		ExtensionRegistry extensionRegistry = populateNewExtensionRegistryFromExtendWith(this.testMethod,
			context.getExtensionRegistry());
		Object testInstance = context.getTestInstanceProvider().getTestInstance();
		ThrowableCollector throwableCollector = new ThrowableCollector();
		TestExtensionContext testExtensionContext = new MethodBasedTestExtensionContext(context.getExtensionContext(),
			context.getExecutionListener(), this, testInstance, throwableCollector);

		// @formatter:off
		return context.extend()
				.withExtensionRegistry(extensionRegistry)
				.withExtensionContext(testExtensionContext)
				.withThrowableCollector(throwableCollector)
				.build();
		// @formatter:on
	}

	@Override
	public SkipResult shouldBeSkipped(JupiterEngineExecutionContext context) throws Exception {
		ConditionEvaluationResult evaluationResult = conditionEvaluator.evaluateForTest(context.getExtensionRegistry(),
			context.getConfigurationParameters(), (TestExtensionContext) context.getExtensionContext());
		if (evaluationResult.isDisabled()) {
			return SkipResult.skip(evaluationResult.getReason().orElse("<unknown>"));
		}
		return SkipResult.doNotSkip();
	}

	@Override
	public JupiterEngineExecutionContext execute(JupiterEngineExecutionContext context) throws Exception {
		ThrowableCollector throwableCollector = context.getThrowableCollector();

		// @formatter:off
		invokeBeforeEachCallbacks(context);
			if (throwableCollector.isEmpty()) {
				invokeBeforeEachMethods(context);
				if (throwableCollector.isEmpty()) {
					invokeBeforeTestExecutionCallbacks(context);
					if (throwableCollector.isEmpty()) {
						invokeTestMethod(context);
					}
					invokeAfterTestExecutionCallbacks(context);
				}
				invokeAfterEachMethods(context);
			}
		invokeAfterEachCallbacks(context);
		// @formatter:on

		throwableCollector.assertEmpty();

		return context;
	}

	private void invokeBeforeEachCallbacks(JupiterEngineExecutionContext context) {
		invokeBeforeMethodsOrCallbacksUntilExceptionOccurs(context,
			((extensionContext, callback) -> () -> callback.beforeEach(extensionContext)), BeforeEachCallback.class);
	}

	private void invokeBeforeEachMethods(JupiterEngineExecutionContext context) {
		invokeBeforeMethodsOrCallbacksUntilExceptionOccurs(context,
			((extensionContext, callback) -> () -> callback.invokeBeforeEachMethod(extensionContext)),
			BeforeEachMethodAdapter.class);
	}

	private void invokeBeforeTestExecutionCallbacks(JupiterEngineExecutionContext context) {
		invokeBeforeMethodsOrCallbacksUntilExceptionOccurs(context,
			((extensionContext, callback) -> () -> callback.beforeTestExecution(extensionContext)),
			BeforeTestExecutionCallback.class);
	}

	private <T extends Extension> void invokeBeforeMethodsOrCallbacksUntilExceptionOccurs(
			JupiterEngineExecutionContext context, BiFunction<TestExtensionContext, T, Executable> generator,
			Class<T> type) {
		ExtensionRegistry registry = context.getExtensionRegistry();
		TestExtensionContext testExtensionContext = (TestExtensionContext) context.getExtensionContext();
		ThrowableCollector throwableCollector = context.getThrowableCollector();

		for (T callback : registry.getExtensions(type)) {
			Executable executable = generator.apply(testExtensionContext, callback);
			throwableCollector.execute(executable);
			if (throwableCollector.isNotEmpty()) {
				break;
			}
		}
	}

	protected void invokeTestMethod(JupiterEngineExecutionContext context) {
		TestExtensionContext testExtensionContext = (TestExtensionContext) context.getExtensionContext();
		ThrowableCollector throwableCollector = context.getThrowableCollector();

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

		invokeTestExecutionExceptionHandlers(ex, registry.getExtensions(TestExecutionExceptionHandler.class), context);
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

	private void invokeAfterTestExecutionCallbacks(JupiterEngineExecutionContext context) {
		invokeAllAfterMethodsOrCallbacks(context,
			((extensionContext, callback) -> () -> callback.afterTestExecution(extensionContext)),
			AfterTestExecutionCallback.class);
	}

	private void invokeAfterEachMethods(JupiterEngineExecutionContext context) {
		invokeAllAfterMethodsOrCallbacks(context,
			((extensionContext, callback) -> () -> callback.invokeAfterEachMethod(extensionContext)),
			AfterEachMethodAdapter.class);
	}

	private void invokeAfterEachCallbacks(JupiterEngineExecutionContext context) {
		invokeAllAfterMethodsOrCallbacks(context,
			((extensionContext, callback) -> () -> callback.afterEach(extensionContext)), AfterEachCallback.class);
	}

	private <T extends Extension> void invokeAllAfterMethodsOrCallbacks(JupiterEngineExecutionContext context,
			BiFunction<TestExtensionContext, T, Executable> generator, Class<T> type) {
		ExtensionRegistry registry = context.getExtensionRegistry();
		TestExtensionContext testExtensionContext = (TestExtensionContext) context.getExtensionContext();
		ThrowableCollector throwableCollector = context.getThrowableCollector();

		registry.getReversedExtensions(type).forEach(callback -> {
			Executable executable = generator.apply(testExtensionContext, callback);
			throwableCollector.execute(executable);
		});

	}

}
