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

import org.apiguardian.api.API;
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
			return replaceUnderscores(super.generateDisplayNameForClass(testClass));
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
	 * {@code DisplayNameGenerator} that supports {@code ReplaceUnderscores} to generate complete sentences.
	 *
	 * <p>This generator extends the functionality of {@link ReplaceUnderscores} by
	 * generating a human-readable display names that form complete sentences divided each
	 * class, nested class and test by a ({@code ','}).
	 *
	 * @since 5.6
	 */
	@API(status = EXPERIMENTAL, since = "5.6")
	class IndicativeSentencesGenerator extends ReplaceUnderscores {

		@Override
		public String generateDisplayNameForClass(Class<?> testClass) {
			return removeRootCharacter(super.generateDisplayNameForClass(testClass));
		}

		@Override
		public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
			return classReplaceToIndicativeSentence(nestedClass);
		}

		@Override
		public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
			return classReplaceToIndicativeSentence(testClass) + getSentenceSeparator(testClass)
					+ super.generateDisplayNameForMethod(testClass, testMethod);
		}

		private String classReplaceToIndicativeSentence(Class<?> testClass) {
			Class<?> classWithEnclosingParent = testClass.getEnclosingClass();
			DisplayName classWithDisplayName = testClass.getAnnotation(DisplayName.class);
			DisplayNameGeneration classWithAnnotation = testClass.getAnnotation(DisplayNameGeneration.class);

			if (classWithEnclosingParent == null) {
				if (classWithDisplayName != null)
					return classWithDisplayName.value();
				else
					return generateDisplayNameForClass(testClass);
			}
			else {
				if (classWithAnnotation != null && classWithAnnotation.value() == IndicativeSentencesGenerator.class) {
					if (classWithDisplayName != null)
						return classWithDisplayName.value();
					else
						return generateDisplayNameForClass(testClass);
				}
				else {
					if (classWithDisplayName != null)
						return classReplaceToIndicativeSentence(classWithEnclosingParent)
								+ getSentenceSeparator(testClass) + classWithDisplayName.value();
					else
						return classReplaceToIndicativeSentence(classWithEnclosingParent)
								+ getSentenceSeparator(testClass) + super.generateDisplayNameForNestedClass(testClass);

				}
			}
		}

		/**
		 * Gets the separator for {@link IndicativeSentencesSeparator} when extracting the
		 * annotation from {@code IndicativeSentencesSeparator}, if it doesn't find it,
		 * then search for the parent classes, if no separator is found use @code{", "} by default.
		 *
		 * @param currentClass the Test Class the separator either custom or default
		 * @return the indicative sentence separator
		 * {@code Class.getName()}.
		 */
		private String getSentenceSeparator(Class<?> currentClass) {
			IndicativeSentencesSeparator separator = currentClass.getAnnotation(IndicativeSentencesSeparator.class);
			if (separator != null)
				return separator.value();

			Class<?> parentClass = currentClass.getEnclosingClass();
			if (parentClass != null)
				return getSentenceSeparator(parentClass);

			return ", ";
		}

		/**
		 * Generate a string with simply the name of the test name without the
		 * classes related divided by a $: AnotherParent$ParentName$Class.
		 *
		 * @param testName the Test Class from to extract the parameter types from;
		 * never {@code blank}.
		 * @return a string without the $ symbol and the root data that comes from
		 * {@code Class.getName()}.
		 */
		private String removeRootCharacter(String testName) {
			Preconditions.notBlank(testName, "Input Name parameter should not be blank");
			String[] testNameSplit = testName.split("\\$");
			int last = testNameSplit.length - 1;

			return testNameSplit[last];
		}
	}
}
