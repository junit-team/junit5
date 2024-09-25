/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junit.platform.commons.support.AnnotationSupport.findRepeatableAnnotations;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;
import static org.junit.platform.commons.util.CollectionUtils.getFirstElement;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.AnnotationConsumerInitializer;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.support.ModifierSupport;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * @since 5.0
 */
class ParameterizedTestExtension implements TestTemplateInvocationContextProvider {

	private static final String METHOD_CONTEXT_KEY = "context";
	static final String ARGUMENT_MAX_LENGTH_KEY = "junit.jupiter.params.displayname.argument.maxlength";
	static final String DEFAULT_DISPLAY_NAME = "{default_display_name}";
	static final String DISPLAY_NAME_PATTERN_KEY = "junit.jupiter.params.displayname.default";

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		if (!context.getTestMethod().isPresent()) {
			return false;
		}

		Method testMethod = context.getTestMethod().get();
		if (!isAnnotated(testMethod, ParameterizedTest.class)) {
			return false;
		}

		ParameterizedTestMethodContext methodContext = new ParameterizedTestMethodContext(testMethod);

		Preconditions.condition(methodContext.hasPotentiallyValidSignature(),
			() -> String.format(
				"@ParameterizedTest method [%s] declares formal parameters in an invalid order: "
						+ "argument aggregators must be declared after any indexed arguments "
						+ "and before any arguments resolved by another ParameterResolver.",
				testMethod.toGenericString()));

		getStore(context).put(METHOD_CONTEXT_KEY, methodContext);

		return true;
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(
			ExtensionContext extensionContext) {

		Method templateMethod = extensionContext.getRequiredTestMethod();
		String displayName = extensionContext.getDisplayName();
		ParameterizedTestMethodContext methodContext = getStore(extensionContext)//
				.get(METHOD_CONTEXT_KEY, ParameterizedTestMethodContext.class);
		int argumentMaxLength = extensionContext.getConfigurationParameter(ARGUMENT_MAX_LENGTH_KEY,
			Integer::parseInt).orElse(512);
		ParameterizedTestNameFormatter formatter = createNameFormatter(extensionContext, templateMethod, methodContext,
			displayName, argumentMaxLength);
		AtomicLong invocationCount = new AtomicLong(0);

		// @formatter:off
		return findRepeatableAnnotations(templateMethod, ArgumentsSource.class)
				.stream()
				.map(ArgumentsSource::value)
				.map(clazz -> instantiateArgumentsProvider(clazz, extensionContext))
				.map(provider -> AnnotationConsumerInitializer.initialize(templateMethod, provider))
				.flatMap(provider -> arguments(provider, extensionContext))
				.map(arguments -> {
					invocationCount.incrementAndGet();
					return createInvocationContext(formatter, methodContext, arguments, invocationCount.intValue());
				})
				.onClose(() ->
						Preconditions.condition(invocationCount.get() > 0,
								"Configuration error: You must configure at least one set of arguments for this @ParameterizedTest"));
		// @formatter:on
	}

	private ArgumentsProvider instantiateArgumentsProvider(Class<? extends ArgumentsProvider> clazz,
			ExtensionContext extensionContext) {
		return extensionContext.getExecutableInvoker().invoke(findConstructor(ArgumentsProvider.class, clazz));
	}

	@SuppressWarnings("unchecked")
	private static <T> Constructor<? extends T> findConstructor(Class<T> spiClass, Class<? extends T> clazz) {
		Optional<Constructor<?>> defaultConstructor = getFirstElement(
			ReflectionUtils.findConstructors(clazz, it -> it.getParameterCount() == 0));
		if (defaultConstructor.isPresent()) {
			return (Constructor<? extends T>) defaultConstructor.get();
		}
		if (ModifierSupport.isNotStatic(clazz)) {
			String message = String.format("The %s [%s] must be either a top-level class or a static nested class",
				spiClass.getSimpleName(), clazz.getName());
			throw new JUnitException(message);
		}
		try {
			return ReflectionUtils.getDeclaredConstructor(clazz);
		}
		catch (PreconditionViolationException ex) {
			String message = String.format(
				"Failed to find constructor for %s [%s]. "
						+ "Please ensure that a no-argument or a single constructor exists.",
				spiClass.getSimpleName(), clazz.getName());
			throw new JUnitException(message);
		}
	}

	private ExtensionContext.Store getStore(ExtensionContext context) {
		return context.getStore(Namespace.create(ParameterizedTestExtension.class, context.getRequiredTestMethod()));
	}

	private TestTemplateInvocationContext createInvocationContext(ParameterizedTestNameFormatter formatter,
			ParameterizedTestMethodContext methodContext, Arguments arguments, int invocationIndex) {

		return new ParameterizedTestInvocationContext(formatter, methodContext, arguments, invocationIndex);
	}

	private ParameterizedTestNameFormatter createNameFormatter(ExtensionContext extensionContext, Method templateMethod,
			ParameterizedTestMethodContext methodContext, String displayName, int argumentMaxLength) {

		ParameterizedTest parameterizedTest = findAnnotation(templateMethod, ParameterizedTest.class).get();
		String pattern = parameterizedTest.name().equals(DEFAULT_DISPLAY_NAME)
				? extensionContext.getConfigurationParameter(DISPLAY_NAME_PATTERN_KEY).orElse(
					ParameterizedTest.DEFAULT_DISPLAY_NAME)
				: parameterizedTest.name();
		pattern = Preconditions.notBlank(pattern.trim(),
			() -> String.format(
				"Configuration error: @ParameterizedTest on method [%s] must be declared with a non-empty name.",
				templateMethod));
		return new ParameterizedTestNameFormatter(pattern, displayName, methodContext, argumentMaxLength);
	}

	protected static Stream<? extends Arguments> arguments(ArgumentsProvider provider, ExtensionContext context) {
		try {
			return provider.provideArguments(context);
		}
		catch (Exception e) {
			throw ExceptionUtils.throwAsUncheckedException(e);
		}
	}

}
