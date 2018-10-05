/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
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
import org.junit.jupiter.api.DisplayNameGenerator.Standard;
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

	static String determineDisplayNameForMethod(Class<?> testClass, Method testMethod) {
		DisplayNameGenerator generator = getDisplayNameGenerator(testClass);
		return determineDisplayName(testMethod, () -> generator.generateDisplayNameForMethod(testClass, testMethod));

	}

	static Supplier<String> createDisplayNameSupplierForClass(Class<?> testClass) {
		return () -> getDisplayNameGenerator(testClass).generateDisplayNameForClass(testClass);
	}

	static Supplier<String> createDisplayNameSupplierForNestedClass(Class<?> testClass) {
		return () -> getDisplayNameGenerator(testClass).generateDisplayNameForNestedClass(testClass);
	}

	private static DisplayNameGenerator getDisplayNameGenerator(Class<?> testClass) {
		Preconditions.notNull(testClass, "Test class must not be null");
		DisplayNameGeneration generation = getDisplayNameGeneration(testClass).orElse(null);
		// trivial case: no user-defined generation annotation present, return default generator
		if (generation == null) {
			return standardGenerator;
		}
		// check for pre-defined generators and return matching singleton
		Class<? extends DisplayNameGenerator> displayNameGeneratorClass = generation.value();
		if (displayNameGeneratorClass == Standard.class) {
			return standardGenerator;
		}
		if (displayNameGeneratorClass == ReplaceUnderscores.class) {
			return replaceUnderscoresGenerator;
		}
		// else: create an instance of the supplied generator implementation class and return it
		return ReflectionUtils.newInstance(displayNameGeneratorClass);
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
