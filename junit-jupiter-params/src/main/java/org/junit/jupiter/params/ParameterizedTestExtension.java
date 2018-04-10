/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import static org.junit.jupiter.params.aggregator.AggregationUtils.hasAggregator;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;
import static org.junit.platform.commons.util.AnnotationUtils.findRepeatableAnnotations;
import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.AnnotationConsumerInitializer;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * @since 5.0
 */
class ParameterizedTestExtension implements TestTemplateInvocationContextProvider {

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		return isAnnotated(context.getTestMethod(), ParameterizedTest.class);
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
		Method templateMethod = context.getRequiredTestMethod();
		ParameterizedTestNameFormatter formatter = createNameFormatter(templateMethod);
		AtomicLong invocationCount = new AtomicLong(0);
		// @formatter:off
		return findRepeatableAnnotations(templateMethod, ArgumentsSource.class)
				.stream()
				.map(ArgumentsSource::value)
				.map(ReflectionUtils::newInstance)
				.map(provider -> AnnotationConsumerInitializer.initialize(templateMethod, provider))
				.flatMap(provider -> arguments(provider, context))
				.map(Arguments::get)
				.map(arguments -> consumedArguments(arguments, templateMethod))
				.map(arguments -> createInvocationContext(formatter, arguments))
				.peek(invocationContext -> invocationCount.incrementAndGet())
				.onClose(() ->
						Preconditions.condition(invocationCount.get() > 0,
								() -> "Configuration error: You must provide at least one argument for this @" + ParameterizedTest.class.getSimpleName()));
		// @formatter:on
	}

	private TestTemplateInvocationContext createInvocationContext(ParameterizedTestNameFormatter formatter,
			Object[] arguments) {
		return new ParameterizedTestInvocationContext(formatter, arguments);
	}

	private ParameterizedTestNameFormatter createNameFormatter(Method templateMethod) {
		ParameterizedTest parameterizedTest = findAnnotation(templateMethod, ParameterizedTest.class).get();
		String name = Preconditions.notBlank(parameterizedTest.name().trim(),
			() -> String.format(
				"Configuration error: @ParameterizedTest on method [%s] must be declared with a non-empty name.",
				templateMethod));
		return new ParameterizedTestNameFormatter(name);
	}

	protected static Stream<? extends Arguments> arguments(ArgumentsProvider provider, ExtensionContext context) {
		try {
			return provider.provideArguments(context);
		}
		catch (Exception e) {
			throw ExceptionUtils.throwAsUncheckedException(e);
		}
	}

	private Object[] consumedArguments(Object[] arguments, Method templateMethod) {
		int parameterCount = templateMethod.getParameterCount();
		return hasAggregator(templateMethod) ? arguments
				: (arguments.length > parameterCount ? Arrays.copyOf(arguments, parameterCount) : arguments);
	}

}
