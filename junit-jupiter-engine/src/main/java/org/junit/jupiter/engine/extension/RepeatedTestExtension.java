/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;

import java.lang.reflect.Method;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@code TestTemplateInvocationContextProvider} that supports the
 * {@link RepeatedTest @RepeatedTest} annotation.
 *
 * @since 5.0
 */
class RepeatedTestExtension implements TestTemplateInvocationContextProvider {

	@Override
	public boolean supportsTestTemplate(ExtensionContext context) {
		return isAnnotated(context.getTestMethod(), RepeatedTest.class);
	}

	@Override
	public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
		Method testMethod = context.getRequiredTestMethod();
		String displayName = context.getDisplayName();
		RepeatedTest repeatedTest = AnnotationUtils.findAnnotation(testMethod, RepeatedTest.class).get();
		int totalRepetitions = totalRepetitions(repeatedTest, testMethod);
		RepeatedTestDisplayNameFormatter formatter = displayNameFormatter(repeatedTest, testMethod, displayName);

		// @formatter:off
		return IntStream
				.rangeClosed(1, totalRepetitions)
				.mapToObj(repetition -> new RepeatedTestInvocationContext(repetition, totalRepetitions, formatter));
		// @formatter:on
	}

	private int totalRepetitions(RepeatedTest repeatedTest, Method method) {
		int repetitions = repeatedTest.value();
		Preconditions.condition(repetitions > 0, () -> String.format(
			"Configuration error: @RepeatedTest on method [%s] must be declared with a positive 'value'.", method));
		return repetitions;
	}

	private RepeatedTestDisplayNameFormatter displayNameFormatter(RepeatedTest repeatedTest, Method method,
			String displayName) {
		String pattern = Preconditions.notBlank(repeatedTest.name().trim(), () -> String.format(
			"Configuration error: @RepeatedTest on method [%s] must be declared with a non-empty name.", method));
		return new RepeatedTestDisplayNameFormatter(pattern, displayName);
	}

}
