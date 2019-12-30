/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.reflect.Method;
import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@code DisplayNameGenerator} defines the SPI for generating display names
 * programmatically.
 *
 * <p>Display names are typically used for test reporting in IDEs and build
 * tools and may contain spaces, special characters, and even emoji.
 *
 * <p>Concrete implementations must have a <em>default constructor</em>.
 *
 * @since 5.4
 * @see DisplayName @DisplayName
 * @see DisplayNameGeneration @DisplayNameGeneration
 */
@API(status = EXPERIMENTAL, since = "5.4")
public interface DisplayNameGenerator {

	/**
	 * Generate a display name for the given top-level or {@code static} nested test class.
	 *
	 * @param testClass the class to generate a name for; never {@code null}
	 * @return the display name for the class; never {@code null} or blank
	 */
	String generateDisplayNameForClass(Class<?> testClass);

	/**
	 * Generate a display name for the given {@link Nested @Nested} inner test class.
	 *
	 * @param nestedClass the class to generate a name for; never {@code null}
	 * @return the display name for the nested class; never {@code null} or blank
	 */
	String generateDisplayNameForNestedClass(Class<?> nestedClass);

	/**
	 * Generate a display name for the given method.
	 *
	 * @implNote The class instance supplied as {@code testClass} may differ from
	 * the class returned by {@code testMethod.getDeclaringClass()} &mdash; for
	 * example, when a test method is inherited from a superclass.
	 *
	 * @param testClass the class the test method is invoked on; never {@code null}
	 * @param testMethod method to generate a display name for; never {@code null}
	 * @return the display name for the test; never {@code null} or blank
	 */
	String generateDisplayNameForMethod(Class<?> testClass, Method testMethod);

	/**
	 * Generate a string representation of the formal parameters of the supplied
	 * method, consisting of the {@linkplain Class#getSimpleName() simple names}
	 * of the parameter types, separated by commas, and enclosed in parentheses.
	 *
	 * @param method the method from to extract the parameter types from; never
	 * {@code null}
	 * @return a string representation of all parameter types of the supplied
	 * method or {@code "()"} if the method declares no parameters
	 */
	static String parameterTypesAsString(Method method) {
		Preconditions.notNull(method, "Method must not be null");
		return '(' + ClassUtils.nullSafeToString(Class::getSimpleName, method.getParameterTypes()) + ')';
	}

	/**
	 * Standard {@code DisplayNameGenerator}.
	 *
	 * <p>This implementation matches the standard display name generation
	 * behavior in place since JUnit Jupiter 5.0 was released.
	 */
	class Standard implements DisplayNameGenerator {

		@Override
		public String generateDisplayNameForClass(Class<?> testClass) {
			String name = testClass.getName();
			int lastDot = name.lastIndexOf('.');
			return name.substring(lastDot + 1);
		}

		@Override
		public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
			return nestedClass.getSimpleName();
		}

		@Override
		public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
			return testMethod.getName() + parameterTypesAsString(testMethod);
		}
	}

	/**
	 * {@code DisplayNameGenerator} that replaces underscores with spaces.
	 *
	 * <p>This generator extends the functionality of {@link Standard} by
	 * replacing all underscores ({@code '_'}) found in class and method names
	 * with spaces ({@code ' '}).
	 */
	class ReplaceUnderscores extends Standard {

		@Override
		public String generateDisplayNameForClass(Class<?> testClass) {
			return replaceUnderscores(testClass.getSimpleName());
		}

		@Override
		public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
			return replaceUnderscores(super.generateDisplayNameForNestedClass(nestedClass));
		}

		@Override
		public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
			String displayName = replaceUnderscores(testMethod.getName());
			if (hasParameters(testMethod)) {
				displayName += ' ' + parameterTypesAsString(testMethod);
			}
			return displayName;
		}

		private static String replaceUnderscores(String name) {
			return name.replace('_', ' ');
		}

		private static boolean hasParameters(Method method) {
			return method.getParameterCount() > 0;
		}
	}

	/**
	 * {@code DisplayNameGenerator} that generate complete sentences.
	 *
	 * <p>This extends the functionality of {@link ReplaceUnderscores} by
	 * generating complete sentences display names, divided by a separator
	 * {@link IndicativeSentencesGeneration}.
	 *
	 * @since 5.6
	 */
	@API(status = EXPERIMENTAL, since = "5.6")
	class IndicativeSentences implements DisplayNameGenerator {

		@Override
		public String generateDisplayNameForClass(Class<?> testClass) {
			return getDisplayNameGenerator(testClass).generateDisplayNameForClass(testClass);
		}

		@Override
		public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
			return classReplaceToIndicativeSentence(nestedClass);
		}

		@Override
		public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
			return classReplaceToIndicativeSentence(testClass) + getSentenceSeparator(testClass)
					+ getDisplayNameGenerator(testClass).generateDisplayNameForMethod(testClass, testMethod);
		}

		private String classReplaceToIndicativeSentence(Class<?> testClass) {
			Class<?> enclosingParent = testClass.getEnclosingClass();
			Optional<DisplayName> displayName = AnnotationSupport.findAnnotation(testClass, DisplayName.class);
			Optional<DisplayNameGeneration> displayNameGeneration = AnnotationSupport.findAnnotation(testClass,
				DisplayNameGeneration.class);

			if (enclosingParent == null) {
				return displayName.map(DisplayName::value).orElseGet(() -> generateDisplayNameForClass(testClass));
			}
			else {
				if (displayNameGeneration.isPresent()) {
					return displayName.map(DisplayName::value).orElseGet(() -> generateDisplayNameForClass(testClass));
				}
				else {
					return displayName.map(name -> classReplaceToIndicativeSentence(enclosingParent)
							+ getSentenceSeparator(testClass) + name.value()).orElseGet(
								() -> classReplaceToIndicativeSentence(enclosingParent)
										+ getSentenceSeparator(testClass)
										+ getDisplayNameGenerator(testClass).generateDisplayNameForNestedClass(
											testClass));
				}
			}
		}

		/**
		 * Gets the separator for {@link IndicativeSentencesGeneration} when extracting the
		 * annotation from {@code IndicativeSentencesGeneration}, if it doesn't find it,
		 * then search for the parent classes, if no separator is found use @code{", "} by default.
		 *
		 * @param currentClass the Test Class the separator either custom or default
		 * @return the indicative sentence separator
		 * {@code Class.getName()}.
		 */
		private String getSentenceSeparator(Class<?> currentClass) {
			Optional<IndicativeSentencesGeneration> indicativeSentencesGeneration = AnnotationSupport.findAnnotation(
				currentClass, IndicativeSentencesGeneration.class);

			if (indicativeSentencesGeneration.isPresent())
				if (indicativeSentencesGeneration.get().separator().equals(""))
					return IndicativeSentencesGeneration.DEFAULT_SEPARATOR;
				else
					return indicativeSentencesGeneration.get().separator();

			if (currentClass.getEnclosingClass() != null)
				return getSentenceSeparator(currentClass.getEnclosingClass());

			return IndicativeSentencesGeneration.DEFAULT_SEPARATOR;
		}

		private DisplayNameGenerator getDisplayNameGenerator(Class<?> currentClass) {
			Optional<IndicativeSentencesGeneration> indicativeSentencesGeneration = AnnotationSupport.findAnnotation(
				currentClass, IndicativeSentencesGeneration.class);

			if (indicativeSentencesGeneration.isPresent()) {
				if (indicativeSentencesGeneration.get().value() == ReplaceUnderscores.class)
					return IndicativeSentencesGeneration.replaceUnderscoresGenerator;
				return IndicativeSentencesGeneration.standardGenerator;
			}

			if (currentClass.getEnclosingClass() != null)
				return getDisplayNameGenerator(currentClass.getEnclosingClass());

			return IndicativeSentencesGeneration.standardGenerator;
		}
	}
}
