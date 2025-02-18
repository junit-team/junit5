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

import static org.junit.platform.commons.support.AnnotationSupport.findRepeatableAnnotations;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ParameterDeclarations;
import org.junit.jupiter.params.support.AnnotationConsumerInitializer;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.Preconditions;

class ParameterizedInvocationContextProvider<T> {

	protected Stream<T> provideInvocationContexts(ExtensionContext extensionContext,
			ParameterizedDeclarationContext<?> declarationContext,
			ParameterizedInvocationContextFactory<T> parameterizedInvocationContextFactory) {

		List<ArgumentsSource> argumentsSources = collectArgumentSources(declarationContext);
		ParameterDeclarations parameters = declarationContext.getResolverFacade().getRegularParameterDeclarations();
		ParameterizedInvocationNameFormatter formatter = ParameterizedInvocationNameFormatter.create(extensionContext,
			declarationContext);
		AtomicLong invocationCount = new AtomicLong(0);

		// @formatter:off
		return argumentsSources
				.stream()
				.map(ArgumentsSource::value)
				.map(clazz -> ParameterizedTestSpiInstantiator.instantiate(ArgumentsProvider.class, clazz, extensionContext))
				.map(provider -> AnnotationConsumerInitializer.initialize(declarationContext.getAnnotatedElement(), provider))
				.flatMap(provider -> arguments(provider, parameters, extensionContext))
				.map(arguments -> {
					invocationCount.incrementAndGet();
					return parameterizedInvocationContextFactory.create(formatter, arguments, invocationCount.intValue());
				})
				.onClose(() ->
						Preconditions.condition(invocationCount.get() > 0 || declarationContext.isAllowingZeroInvocations(),
								() -> String.format("Configuration error: You must configure at least one set of arguments for this @%s", declarationContext.getAnnotationName())));
		// @formatter:on
	}

	private static List<ArgumentsSource> collectArgumentSources(ParameterizedDeclarationContext<?> declarationContext) {
		List<ArgumentsSource> argumentsSources = findRepeatableAnnotations(declarationContext.getAnnotatedElement(),
			ArgumentsSource.class);

		Preconditions.notEmpty(argumentsSources,
			() -> String.format("Configuration error: You must configure at least one arguments source for this @%s",
				declarationContext.getAnnotationName()));

		return argumentsSources;
	}

	protected static Stream<? extends Arguments> arguments(ArgumentsProvider provider, ParameterDeclarations parameters,
			ExtensionContext context) {
		try {
			return provider.provideArguments(parameters, context);
		}
		catch (Exception e) {
			throw ExceptionUtils.throwAsUncheckedException(e);
		}
	}

	interface ParameterizedInvocationContextFactory<T> {
		T create(ParameterizedInvocationNameFormatter formatter, Arguments arguments, int invocationIndex);
	}

}
