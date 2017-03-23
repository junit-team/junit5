/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.extension;

import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;

import java.lang.reflect.Method;
import java.util.logging.Logger;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;

/**
 * {@code TestTemplateInvocationContextProvider} that supports the
 * {@link RepeatedTest @RepeatedTest} annotation.
 *
 * @since 5.0
 */
class RepeatedTestExtension implements TestTemplateInvocationContextProvider {

	private static final Logger logger = Logger.getLogger(RepeatedTestExtension.class.getName());

	@Override
	public boolean supports(ContainerExtensionContext context) {
		return isAnnotated(context.getTestMethod(), RepeatedTest.class);
	}

	@Override
	public Stream<TestTemplateInvocationContext> provide(ContainerExtensionContext context) {
		Method testMethod = Preconditions.notNull(context.getTestMethod().orElse(null), "test method must not be null");
		String displayName = context.getDisplayName();
		RepeatedTest repeatedTest = AnnotationUtils.findAnnotation(testMethod, RepeatedTest.class).get();
		int totalRepetitions = totalRepetitions(repeatedTest);
		RepeatedTestDisplayNameFormatter formatter = displayNameFormatter(repeatedTest, testMethod, displayName);

		// @formatter:off
		return IntStream
				.rangeClosed(1, totalRepetitions)
				.mapToObj(repetition -> new RepeatedTestInvocationContext(repetition, totalRepetitions, formatter));
		// @formatter:on
	}

	private int totalRepetitions(RepeatedTest repeatedTest) {
		return Math.max(1, repeatedTest.value());
	}

	private RepeatedTestDisplayNameFormatter displayNameFormatter(RepeatedTest repeatedTest, Method method,
			String displayName) {

		String pattern = repeatedTest.name().trim();

		// TODO [#242] Replace logging with precondition check once we have a proper mechanism for
		// handling exceptions during the TestEngine discovery phase.
		//
		// Preconditions.notBlank(name, () -> String.format(
		//    "Configuration error: @RepeatedTest on method [%s] must be declared with a non-empty name.", method));
		//
		if (StringUtils.isBlank(pattern)) {
			logger.warning(String.format(
				"Configuration error: @RepeatedTest on method [%s] must be declared with a non-empty name.", method));
			pattern = AnnotationUtils.getDefaultValue(repeatedTest, "name", String.class).get();
		}

		return new RepeatedTestDisplayNameFormatter(pattern, displayName);
	}

}
