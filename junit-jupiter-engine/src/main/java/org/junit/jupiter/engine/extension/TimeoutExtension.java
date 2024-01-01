/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.junit.jupiter.api.Timeout.TIMEOUT_MODE_PROPERTY_NAME;
import static org.junit.jupiter.api.Timeout.ThreadMode.SAME_THREAD;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.Timeout.ThreadMode;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.engine.extension.TimeoutInvocationFactory.TimeoutInvocationParameters;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.RuntimeUtils;

/**
 * @since 5.5
 */
class TimeoutExtension implements BeforeAllCallback, BeforeEachCallback, InvocationInterceptor {

	private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(Timeout.class);
	private static final String TESTABLE_METHOD_TIMEOUT_KEY = "testable_method_timeout_from_annotation";
	private static final String TESTABLE_METHOD_TIMEOUT_THREAD_MODE_KEY = "testable_method_timeout_thread_mode_from_annotation";
	private static final String GLOBAL_TIMEOUT_CONFIG_KEY = "global_timeout_config";
	private static final String ENABLED_MODE_VALUE = "enabled";
	private static final String DISABLED_MODE_VALUE = "disabled";
	private static final String DISABLED_ON_DEBUG_MODE_VALUE = "disabled_on_debug";

	@Override
	public void beforeAll(ExtensionContext context) {
		readAndStoreTimeoutSoChildrenInheritIt(context);
	}

	@Override
	public void beforeEach(ExtensionContext context) {
		readAndStoreTimeoutSoChildrenInheritIt(context);
	}

	private void readAndStoreTimeoutSoChildrenInheritIt(ExtensionContext context) {
		readTimeoutFromAnnotation(context.getElement()).ifPresent(
			timeout -> context.getStore(NAMESPACE).put(TESTABLE_METHOD_TIMEOUT_KEY, timeout));
		readTimeoutThreadModeFromAnnotation(context.getElement()).ifPresent(
			timeoutThreadMode -> context.getStore(NAMESPACE).put(TESTABLE_METHOD_TIMEOUT_THREAD_MODE_KEY,
				timeoutThreadMode));
	}

	@Override
	public void interceptBeforeAllMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {

		interceptLifecycleMethod(invocation, invocationContext, extensionContext,
			TimeoutConfiguration::getDefaultBeforeAllMethodTimeout);
	}

	@Override
	public void interceptBeforeEachMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {

		interceptLifecycleMethod(invocation, invocationContext, extensionContext,
			TimeoutConfiguration::getDefaultBeforeEachMethodTimeout);
	}

	@Override
	public void interceptTestMethod(Invocation<Void> invocation, ReflectiveInvocationContext<Method> invocationContext,
			ExtensionContext extensionContext) throws Throwable {

		interceptTestableMethod(invocation, invocationContext, extensionContext,
			TimeoutConfiguration::getDefaultTestMethodTimeout);
	}

	@Override
	public void interceptTestTemplateMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {

		interceptTestableMethod(invocation, invocationContext, extensionContext,
			TimeoutConfiguration::getDefaultTestTemplateMethodTimeout);
	}

	@Override
	public <T> T interceptTestFactoryMethod(Invocation<T> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {

		return interceptTestableMethod(invocation, invocationContext, extensionContext,
			TimeoutConfiguration::getDefaultTestFactoryMethodTimeout);
	}

	@Override
	public void interceptAfterEachMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {

		interceptLifecycleMethod(invocation, invocationContext, extensionContext,
			TimeoutConfiguration::getDefaultAfterEachMethodTimeout);
	}

	@Override
	public void interceptAfterAllMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {

		interceptLifecycleMethod(invocation, invocationContext, extensionContext,
			TimeoutConfiguration::getDefaultAfterAllMethodTimeout);
	}

	private void interceptLifecycleMethod(Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext,
			TimeoutProvider defaultTimeoutProvider) throws Throwable {

		TimeoutDuration timeout = readTimeoutFromAnnotation(Optional.of(invocationContext.getExecutable())).orElse(
			null);
		intercept(invocation, invocationContext, extensionContext, timeout, defaultTimeoutProvider);
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private Optional<TimeoutDuration> readTimeoutFromAnnotation(Optional<AnnotatedElement> element) {
		return AnnotationSupport.findAnnotation(element, Timeout.class).map(TimeoutDuration::from);
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private Optional<ThreadMode> readTimeoutThreadModeFromAnnotation(Optional<AnnotatedElement> element) {
		return AnnotationSupport.findAnnotation(element, Timeout.class).map(Timeout::threadMode);
	}

	private <T> T interceptTestableMethod(Invocation<T> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext,
			TimeoutProvider defaultTimeoutProvider) throws Throwable {

		TimeoutDuration timeout = extensionContext.getStore(NAMESPACE).get(TESTABLE_METHOD_TIMEOUT_KEY,
			TimeoutDuration.class);
		return intercept(invocation, invocationContext, extensionContext, timeout, defaultTimeoutProvider);
	}

	private <T> T intercept(Invocation<T> invocation, ReflectiveInvocationContext<Method> invocationContext,
			ExtensionContext extensionContext, TimeoutDuration explicitTimeout, TimeoutProvider defaultTimeoutProvider)
			throws Throwable {

		TimeoutDuration timeout = explicitTimeout == null ? getDefaultTimeout(extensionContext, defaultTimeoutProvider)
				: explicitTimeout;
		return decorate(invocation, invocationContext, extensionContext, timeout).proceed();
	}

	private TimeoutDuration getDefaultTimeout(ExtensionContext extensionContext,
			TimeoutProvider defaultTimeoutProvider) {

		return defaultTimeoutProvider.apply(getGlobalTimeoutConfiguration(extensionContext)).orElse(null);
	}

	private TimeoutConfiguration getGlobalTimeoutConfiguration(ExtensionContext extensionContext) {
		ExtensionContext root = extensionContext.getRoot();
		return root.getStore(NAMESPACE).getOrComputeIfAbsent(GLOBAL_TIMEOUT_CONFIG_KEY,
			key -> new TimeoutConfiguration(root), TimeoutConfiguration.class);
	}

	private <T> Invocation<T> decorate(Invocation<T> invocation, ReflectiveInvocationContext<Method> invocationContext,
			ExtensionContext extensionContext, TimeoutDuration timeout) {

		if (timeout == null || isTimeoutDisabled(extensionContext)) {
			return invocation;
		}

		ThreadMode threadMode = resolveTimeoutThreadMode(extensionContext);
		return new TimeoutInvocationFactory(extensionContext.getRoot().getStore(NAMESPACE)).create(threadMode,
			new TimeoutInvocationParameters<>(invocation, timeout,
				() -> describe(invocationContext, extensionContext)));
	}

	private ThreadMode resolveTimeoutThreadMode(ExtensionContext extensionContext) {
		ThreadMode annotationThreadMode = getAnnotationThreadMode(extensionContext);
		if (annotationThreadMode == null || annotationThreadMode == ThreadMode.INFERRED) {
			return getGlobalTimeoutConfiguration(extensionContext).getDefaultTimeoutThreadMode().orElse(SAME_THREAD);
		}
		return annotationThreadMode;
	}

	private ThreadMode getAnnotationThreadMode(ExtensionContext extensionContext) {
		return extensionContext.getStore(NAMESPACE).get(TESTABLE_METHOD_TIMEOUT_THREAD_MODE_KEY, ThreadMode.class);
	}

	private String describe(ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) {
		Method method = invocationContext.getExecutable();
		Optional<Class<?>> testClass = extensionContext.getTestClass();
		if (testClass.isPresent() && invocationContext.getTargetClass().equals(testClass.get())) {
			return String.format("%s(%s)", method.getName(), ClassUtils.nullSafeToString(method.getParameterTypes()));
		}
		return ReflectionUtils.getFullyQualifiedMethodName(invocationContext.getTargetClass(), method);
	}

	/**
	 * Determine if timeouts are disabled for the supplied extension context.
	 */
	private boolean isTimeoutDisabled(ExtensionContext extensionContext) {
		Optional<String> mode = extensionContext.getConfigurationParameter(TIMEOUT_MODE_PROPERTY_NAME);
		return mode.map(this::isTimeoutDisabled).orElse(false);
	}

	/**
	 * Determine if timeouts are disabled for the supplied mode.
	 */
	private boolean isTimeoutDisabled(String mode) {
		switch (mode) {
			case ENABLED_MODE_VALUE:
				return false;
			case DISABLED_MODE_VALUE:
				return true;
			case DISABLED_ON_DEBUG_MODE_VALUE:
				return RuntimeUtils.isDebugMode();
			default:
				throw new ExtensionConfigurationException("Unsupported timeout mode: " + mode);
		}
	}

	@FunctionalInterface
	private interface TimeoutProvider extends Function<TimeoutConfiguration, Optional<TimeoutDuration>> {
	}
}
