/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params;

import static java.util.Collections.singletonList;
import static org.junit.platform.commons.util.AnnotationUtils.findRepeatableAnnotations;
import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

class ParameterizedTestExtension implements TestTemplateInvocationContextProvider {

	@Override
	public boolean supports(ContainerExtensionContext context) {
		// @formatter:off
		return context.getTestMethod()
				.filter(method -> isAnnotated(method, ParameterizedTest.class))
				.map(method -> true)
				.orElse(false);
		// @formatter:on
	}

	@Override
	public Stream<TestTemplateInvocationContext> provide(ContainerExtensionContext context) {
		// TODO #14 Test that Streams returned by providers are closed
		Method templateMethod = Preconditions.notNull(context.getTestMethod().orElse(null),
			"test method must not be null");
		AnnotationInitializer annotationInitializer = new AnnotationInitializer(templateMethod);
		ParameterizedTestNameFormatter formatter = createNameFormatter(templateMethod);
		// @formatter:off
		return findRepeatableAnnotations(templateMethod, ArgumentsSource.class)
				.stream()
				.map(ArgumentsSource::value)
				.map(ReflectionUtils::newInstance)
				.peek(annotationInitializer::initialize)
				.flatMap(provider -> arguments(provider, context))
				.map(Arguments::get)
				.map(arguments -> toTestTemplateInvocationContext(formatter, arguments));
		// @formatter:on
	}

	private ParameterizedTestNameFormatter createNameFormatter(Method templateMethod) {
		ParameterizedTest parameterizedTestAnnotation = AnnotationUtils.findAnnotation(templateMethod,
			ParameterizedTest.class).get();
		return new ParameterizedTestNameFormatter(parameterizedTestAnnotation.name());
	}

	private static Stream<? extends Arguments> arguments(ArgumentsProvider provider,
			ContainerExtensionContext context) {
		try {
			return provider.arguments(context);
		}
		catch (Exception e) {
			// TODO #14 Test
			throw ExceptionUtils.throwAsUncheckedException(e);
		}
	}

	private static TestTemplateInvocationContext toTestTemplateInvocationContext(
			ParameterizedTestNameFormatter formatter, Object[] arguments) {
		return new TestTemplateInvocationContext() {
			@Override
			public String getDisplayName(int invocationIndex) {
				return formatter.format(invocationIndex, arguments);
			}

			@Override
			public List<Extension> getAdditionalExtensions() {
				return singletonList(new ParameterResolver() {

					@Override
					public boolean supports(ParameterContext parameterContext, ExtensionContext extensionContext)
							throws ParameterResolutionException {
						return parameterContext.getIndex() < arguments.length;
					}

					@Override
					public Object resolve(ParameterContext parameterContext, ExtensionContext extensionContext)
							throws ParameterResolutionException {
						Object argument = arguments[parameterContext.getIndex()];
						if (argument instanceof String
								&& parameterContext.getParameter().getType().equals(Integer.TYPE)) {
							return Integer.parseInt((String) argument);
						}
						return argument;
					}
				});
			}
		};
	}
}
