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
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.params.provider.Arguments;

class ParameterizedInvocationContext<T extends ParameterizedDeclarationContext<?>> {

	private static final Namespace NAMESPACE = Namespace.create(ParameterizedTestInvocationContext.class);

	private final ParameterizedInvocationNameFormatter formatter;
	protected final T declarationContext;
	protected final EvaluatedArgumentSet arguments;
	protected final int invocationIndex;

	ParameterizedInvocationContext(ParameterizedInvocationNameFormatter formatter, T declarationContext,
			Arguments arguments, int invocationIndex) {

		this.formatter = formatter;
		this.declarationContext = declarationContext;
		this.arguments = EvaluatedArgumentSet.of(arguments, this::determineConsumedArgumentCount);
		this.invocationIndex = invocationIndex;
	}

	public String getDisplayName(int invocationIndex) {
		return this.formatter.format(invocationIndex, this.arguments);
	}

	public void prepareInvocation(ExtensionContext context) {
		if (this.declarationContext.isAutoClosingArguments()) {
			ExtensionContext.Store store = context.getStore(NAMESPACE);
			AtomicInteger argumentIndex = new AtomicInteger();

			Arrays.stream(this.arguments.getAllPayloads()) //
					.filter(AutoCloseable.class::isInstance) //
					.map(AutoCloseable.class::cast) //
					.map(CloseableArgument::new) //
					.forEach(closeable -> store.put(argumentIndex.incrementAndGet(), closeable));
		}
	}

	private int determineConsumedArgumentCount(int totalLength) {
		return this.declarationContext.getResolverFacade().hasAggregator() //
				? totalLength //
				: Math.min(totalLength, this.declarationContext.getResolverFacade().getParameterCount());
	}

	private static class CloseableArgument implements ExtensionContext.Store.CloseableResource {

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
