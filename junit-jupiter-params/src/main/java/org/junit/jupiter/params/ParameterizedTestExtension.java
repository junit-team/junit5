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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.AnnotationConsumerInitializer;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.Preconditions;

/**
 * @since 5.0
 */
class ParameterizedTestExtension implements TestTemplateInvocationContextProvider {

	private static final Logger logger = LoggerFactory.getLogger(ParameterizedTestExtension.class);

	private static final String METHOD_CONTEXT_KEY = "context";
	static final String ARGUMENT_MAX_LENGTH_KEY = "junit.jupiter.params.displayname.argument.maxlength";
	static final String DEFAULT_DISPLAY_NAME = "{default_display_name}";
	static final String DISPLAY_NAME_PATTERN_KEY = "junit.jupiter.params.displayname.default";
	static final String ARGUMENT_COUNT_VALIDATION_KEY = "junit.jupiter.params.argumentCountValidation";

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		if (!context.getTestMethod().isPresent()) {
			return false;
		}

		Method testMethod = context.getTestMethod().get();
		if (!isAnnotated(testMethod, ParameterizedTest.class)) {
			return false;
		}

		ParameterizedTestMethodContext methodContext = new ParameterizedTestMethodContext(testMethod, context);

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
				.map(clazz -> ParameterizedTestSpiInstantiator.instantiate(ArgumentsProvider.class, clazz, extensionContext))
				.map(provider -> AnnotationConsumerInitializer.initialize(templateMethod, provider))
				.flatMap(provider -> arguments(provider, extensionContext))
				.map(arguments -> {
					validateArgumentCount(extensionContext, arguments);
					invocationCount.incrementAndGet();
					return createInvocationContext(formatter, methodContext, arguments, invocationCount.intValue());
				})
				.onClose(() ->
						Preconditions.condition(invocationCount.get() > 0,
								"Configuration error: You must configure at least one set of arguments for this @ParameterizedTest"));
		// @formatter:on
	}

	private ExtensionContext.Store getStore(ExtensionContext context) {
		return context.getStore(Namespace.create(ParameterizedTestExtension.class, context.getRequiredTestMethod()));
	}

	private void validateArgumentCount(ExtensionContext extensionContext, Arguments arguments) {
		ArgumentCountValidationMode argumentCountValidationMode = getArgumentCountValidationMode(extensionContext);
		switch (argumentCountValidationMode) {
			case DEFAULT:
			case NONE:
				return;
			case STRICT:
				int testParamCount = extensionContext.getRequiredTestMethod().getParameterCount();
				int argumentsCount = arguments.get().length;
				Preconditions.condition(testParamCount == argumentsCount, () -> String.format(
					"Configuration error: the @ParameterizedTest has %s argument(s) but there were %s argument(s) provided./nNote: the provided arguments are %s",
					testParamCount, argumentsCount, Arrays.toString(arguments.get())));
				break;
			default:
				throw new ExtensionConfigurationException(
					"Unsupported argument count validation mode: " + argumentCountValidationMode);
		}
	}

	private ArgumentCountValidationMode getArgumentCountValidationMode(ExtensionContext extensionContext) {
		ParameterizedTest parameterizedTest = findAnnotation(//
			extensionContext.getRequiredTestMethod(), ParameterizedTest.class//
		).orElseThrow(NoSuchElementException::new);
		if (parameterizedTest.argumentCountValidation() != ArgumentCountValidationMode.DEFAULT) {
			return parameterizedTest.argumentCountValidation();
		}
		else {
			return getArgumentCountValidationModeConfiguration(extensionContext);
		}
	}

	private ArgumentCountValidationMode getArgumentCountValidationModeConfiguration(ExtensionContext extensionContext) {
		String key = ARGUMENT_COUNT_VALIDATION_KEY;
		ArgumentCountValidationMode fallback = ArgumentCountValidationMode.DEFAULT;
		Optional<String> optionalValue = extensionContext.getConfigurationParameter(key);
		if (optionalValue.isPresent()) {
			String value = optionalValue.get();
			return Arrays.stream(ArgumentCountValidationMode.values()).filter(
				mode -> mode.name().equalsIgnoreCase(value)).findFirst().orElseGet(() -> {
					logger.warn(() -> String.format(
						"Ignored invalid configuration '%s' set via the '%s' configuration parameter.", value, key));
					return fallback;
				});
		}
		else {
			return fallback;
		}
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
