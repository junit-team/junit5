/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.support.AnnotationSupport;

/**
 * @since 5.0
 */
class ParameterizedTestParameterResolver implements ParameterResolver, AfterTestExecutionCallback {

	private static final Namespace NAMESPACE = Namespace.create(ParameterizedTestParameterResolver.class);

	private final ParameterizedTestMethodContext methodContext;
	private final EvaluatedArgumentSet arguments;
	private final int invocationIndex;

	ParameterizedTestParameterResolver(ParameterizedTestMethodContext methodContext, EvaluatedArgumentSet arguments,
			int invocationIndex) {

		this.methodContext = methodContext;
		this.arguments = arguments;
		this.invocationIndex = invocationIndex;
	}

	@Override
	public ExtensionContextScope getTestInstantiationExtensionContextScope(ExtensionContext rootContext) {
		return ExtensionContextScope.TEST_METHOD;
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Executable declaringExecutable = parameterContext.getDeclaringExecutable();
		Method testMethod = extensionContext.getTestMethod().orElse(null);
		int parameterIndex = parameterContext.getIndex();

		// Not a @ParameterizedTest method?
		if (!declaringExecutable.equals(testMethod)) {
			return false;
		}

		// Current parameter is an aggregator?
		if (this.methodContext.isAggregator(parameterIndex)) {
			return true;
		}

		// Ensure that the current parameter is declared before aggregators.
		// Otherwise, a different ParameterResolver should handle it.
		if (this.methodContext.hasAggregator()) {
			return parameterIndex < this.methodContext.indexOfFirstAggregator();
		}

		// Else fallback to behavior for parameterized test methods without aggregators.
		return parameterIndex < this.arguments.getConsumedLength();
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return this.methodContext.resolve(parameterContext, extensionContext, this.arguments.getConsumedPayloads(),
			this.invocationIndex);
	}

	/**
	 * @since 5.8
	 */
	@Override
	public void afterTestExecution(ExtensionContext context) {
		ParameterizedTest parameterizedTest = AnnotationSupport.findAnnotation(context.getRequiredTestMethod(),
			ParameterizedTest.class).get();
		if (!parameterizedTest.autoCloseArguments()) {
			return;
		}

		Store store = context.getStore(NAMESPACE);
		AtomicInteger argumentIndex = new AtomicInteger();

		Arrays.stream(this.arguments.getConsumedPayloads()) //
				.filter(AutoCloseable.class::isInstance) //
				.map(AutoCloseable.class::cast) //
				.map(CloseableArgument::new) //
				.forEach(closeable -> store.put("closeableArgument#" + argumentIndex.incrementAndGet(), closeable));
	}

	private static class CloseableArgument implements Store.CloseableResource {

		private final AutoCloseable autoCloseable;

		CloseableArgument(AutoCloseable autoCloseable) {
			this.autoCloseable = autoCloseable;
		}

		@Override
		public void close() throws Throwable {
			this.autoCloseable.close();
		}

	}

}
