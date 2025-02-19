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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.Preconditions;

class ArgumentCountValidator implements InvocationInterceptor {
	private static final Logger logger = LoggerFactory.getLogger(ArgumentCountValidator.class);

	static final String ARGUMENT_COUNT_VALIDATION_KEY = "junit.jupiter.params.argumentCountValidation";
	private static final ExtensionContext.Namespace NAMESPACE = ExtensionContext.Namespace.create(
		ArgumentCountValidator.class);

	private final ParameterizedTestMethodContext methodContext;
	private final EvaluatedArgumentSet arguments;

	ArgumentCountValidator(ParameterizedTestMethodContext methodContext, EvaluatedArgumentSet arguments) {
		this.methodContext = methodContext;
		this.arguments = arguments;
	}

	@Override
	public void interceptTestTemplateMethod(InvocationInterceptor.Invocation<Void> invocation,
			ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext) throws Throwable {
		validateArgumentCount(extensionContext);
		invocation.proceed();
	}

	private ExtensionContext.Store getStore(ExtensionContext context) {
		return context.getRoot().getStore(NAMESPACE);
	}

	private void validateArgumentCount(ExtensionContext extensionContext) {
		ArgumentCountValidationMode argumentCountValidationMode = getArgumentCountValidationMode(extensionContext);
		switch (argumentCountValidationMode) {
			case DEFAULT:
			case NONE:
				return;
			case STRICT:
				int testParamCount = extensionContext.getRequiredTestMethod().getParameterCount();
				int argumentsCount = arguments.getTotalLength();
				Preconditions.condition(testParamCount == argumentsCount, () -> String.format(
					"Configuration error: the @ParameterizedTest has %s argument(s) but there were %s argument(s) provided.%nNote: the provided arguments are %s",
					testParamCount, argumentsCount, Arrays.toString(arguments.getAllPayloads())));
				break;
			default:
				throw new ExtensionConfigurationException(
					"Unsupported argument count validation mode: " + argumentCountValidationMode);
		}
	}

	private ArgumentCountValidationMode getArgumentCountValidationMode(ExtensionContext extensionContext) {
		ParameterizedTest parameterizedTest = methodContext.annotation;
		if (parameterizedTest.argumentCountValidation() != ArgumentCountValidationMode.DEFAULT) {
			return parameterizedTest.argumentCountValidation();
		}
		else {
			return getArgumentCountValidationModeConfiguration(extensionContext);
		}
	}

	private ArgumentCountValidationMode getArgumentCountValidationModeConfiguration(ExtensionContext extensionContext) {
		String key = ARGUMENT_COUNT_VALIDATION_KEY;
		ArgumentCountValidationMode fallback = ArgumentCountValidationMode.NONE;
		ExtensionContext.Store store = getStore(extensionContext);
		return store.getOrComputeIfAbsent(key, __ -> {
			Optional<String> optionalConfigValue = extensionContext.getConfigurationParameter(key);
			if (optionalConfigValue.isPresent()) {
				String configValue = optionalConfigValue.get();
				Optional<ArgumentCountValidationMode> enumValue = Arrays.stream(
					ArgumentCountValidationMode.values()).filter(
						mode -> mode.name().equalsIgnoreCase(configValue)).findFirst();
				if (enumValue.isPresent()) {
					logger.config(() -> String.format(
						"Using ArgumentCountValidationMode '%s' set via the '%s' configuration parameter.",
						enumValue.get().name(), key));
					return enumValue.get();
				}
				else {
					logger.warn(() -> String.format(
						"Invalid ArgumentCountValidationMode '%s' set via the '%s' configuration parameter. "
								+ "Falling back to the %s default value.",
						configValue, key, fallback.name()));
					return fallback;
				}
			}
			else {
				return fallback;
			}
		}, ArgumentCountValidationMode.class);
	}
}
