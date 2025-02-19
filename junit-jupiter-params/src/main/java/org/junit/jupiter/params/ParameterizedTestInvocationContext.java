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

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.params.provider.Arguments;

/**
 * @since 5.0
 */
class ParameterizedTestInvocationContext implements TestTemplateInvocationContext {

	private static final Namespace NAMESPACE = Namespace.create(ParameterizedTestInvocationContext.class);

	private final ParameterizedTestNameFormatter formatter;
	private final ParameterizedTestMethodContext methodContext;
	private final EvaluatedArgumentSet arguments;
	private final int invocationIndex;

	ParameterizedTestInvocationContext(ParameterizedTestNameFormatter formatter,
			ParameterizedTestMethodContext methodContext, Arguments arguments, int invocationIndex) {

		this.formatter = formatter;
		this.methodContext = methodContext;
		this.arguments = EvaluatedArgumentSet.of(arguments, this::determineConsumedArgumentCount);
		this.invocationIndex = invocationIndex;
	}

	@Override
	public String getDisplayName(int invocationIndex) {
		return this.formatter.format(invocationIndex, this.arguments);
	}

	@Override
	public List<Extension> getAdditionalExtensions() {
		return Arrays.asList(
			new ParameterizedTestParameterResolver(this.methodContext, this.arguments, this.invocationIndex),
			new ArgumentCountValidator(this.methodContext, this.arguments));
	}

	@Override
	public void prepareInvocation(ExtensionContext context) {
		if (this.methodContext.annotation.autoCloseArguments()) {
			Store store = context.getStore(NAMESPACE);
			AtomicInteger argumentIndex = new AtomicInteger();

			Arrays.stream(this.arguments.getAllPayloads()) //
					.filter(AutoCloseable.class::isInstance) //
					.map(AutoCloseable.class::cast) //
					.map(CloseableArgument::new) //
					.forEach(closeable -> store.put(argumentIndex.incrementAndGet(), closeable));
		}
	}

	private int determineConsumedArgumentCount(int totalLength) {
		return methodContext.hasAggregator() //
				? totalLength //
				: Math.min(totalLength, methodContext.getParameterCount());
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
