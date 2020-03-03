/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.DisplayNameGenerator.ReplaceUnderscores;
import org.junit.jupiter.api.DisplayNameGenerator.Simple;
import org.junit.jupiter.api.DisplayNameGenerator.Standard;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.StringUtils;

/**
 * Collection of utilities for working with display names.
 *
 * @since 5.4
 * @see DisplayName
 * @see DisplayNameGenerator
 * @see DisplayNameGeneration
 */
final class DisplayNameUtils {

	private static final Logger logger = LoggerFactory.getLogger(DisplayNameUtils.class);

	/**
	 * Pre-defined standard display name generator instance.
	 */
	private static final DisplayNameGenerator standardGenerator = new Standard();

	/**
	 * Pre-defined simple display name generator instance.
	 */
	private static final DisplayNameGenerator simpleGenerator = new Simple();

	/**
	 * Pre-defined display name generator instance replacing underscores.
	 */
	private static final DisplayNameGenerator replaceUnderscoresGenerator = new ReplaceUnderscores();

	static String determineDisplayName(AnnotatedElement element, Supplier<String> displayNameSupplier) {
		Preconditions.notNull(element, "Annotated element must not be null");
		Optional<DisplayName> displayNameAnnotation = findAnnotation(element, DisplayName.class);
		if (displayNameAnnotation.isPresent()) {
			String displayName = displayNameAnnotation.get().value().trim();

			// TODO [#242] Replace logging with precondition check once we have a proper mechanism for
			// handling validation exceptions during the TestEngine discovery phase.
			if (StringUtils.isBlank(displayName)) {
				logger.warn(() -> String.format(
					"Configuration error: @DisplayName on [%s] must be declared with a non-empty value.", element));
			}
			else {
				return displayName;
			}
		}
		// else let a 'DisplayNameGenerator' generate a display name
		return displayNameSupplier.get();
	}

	static String determineDisplayNameForMethod(Class<?> testClass, Method testMethod,
			JupiterConfiguration configuration) {
		DisplayNameGenerator generator = getDisplayNameGenerator(testClass, configuration);
		return determineDisplayName(testMethod, () -> generator.generateDisplayNameForMethod(testClass, testMethod));
	}

	static Supplier<String> createDisplayNameSupplierForClass(Class<?> testClass, JupiterConfiguration configuration) {
		return () -> getDisplayNameGenerator(testClass, configuration).generateDisplayNameForClass(testClass);
	}

	static Supplier<String> createDisplayNameSupplierForNestedClass(Class<?> testClass,
			JupiterConfiguration configuration) {
		return () -> getDisplayNameGenerator(testClass, configuration).generateDisplayNameForNestedClass(testClass);
	}

	private static DisplayNameGenerator getDisplayNameGenerator(Class<?> testClass,
			JupiterConfiguration configuration) {
		Preconditions.notNull(testClass, "Test class must not be null");

		return getDisplayNameGeneration(testClass) //
				.map(DisplayNameGeneration::value) //
				.map(displayNameGeneratorClass -> {
					if (displayNameGeneratorClass == Standard.class) {
						return standardGenerator;
					}
					if (displayNameGeneratorClass == Simple.class) {
						return simpleGenerator;
					}
					if (displayNameGeneratorClass == ReplaceUnderscores.class) {
						return replaceUnderscoresGenerator;
					}
					return ReflectionUtils.newInstance(displayNameGeneratorClass);
				}) //
				.orElseGet(configuration::getDefaultDisplayNameGenerator);
	}

	/**
	 * Find the first {@code DisplayNameGeneration} annotation that is either
	 * <em>directly present</em>, <em>meta-present</em>, <em>indirectly present</em>
	 * on the supplied {@code testClass} or on an enclosing class.
	 */
	private static Optional<DisplayNameGeneration> getDisplayNameGeneration(Class<?> testClass) {
		Class<?> candidate = testClass;
		do {
			Optional<DisplayNameGeneration> generation = findAnnotation(candidate, DisplayNameGeneration.class);
			if (generation.isPresent()) {
				return generation;
			}
			candidate = candidate.getEnclosingClass();
		} while (candidate != null);
		return Optional.empty();
	}
}
