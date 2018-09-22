/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.reflect.Method;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@code DisplayNameGenerator} defines the SPI for generating display
 * names programmatically.
 *
 * @since 5.4
 * @see DisplayName
 * @see DisplayNameGeneration
 */
@API(status = EXPERIMENTAL, since = "5.4")
public interface DisplayNameGenerator {

	/**
	 * Generate a display name for the given top-level or {@code static} nested test class.
	 */
	String generateDisplayNameForClass(Class<?> testClass);

	/**
	 * Generate a display name for the given {@link Nested} inner test class.
	 */
	String generateDisplayNameForNestedClass(Class<?> nestedClass);

	/**
	 * Generate a display name for the given method.
	 *
	 * @implNote The class instance passed as {@code testClass} may differ from
	 * the returned class by {@code testMethod.getDeclaringClass()}: e.g., when
	 * a test method is inherited from a super class.
	 *
	 * @param testClass the class the test method is invoked on
	 * @param testMethod method to generate a display name for
	 */
	String generateDisplayNameForMethod(Class<?> testClass, Method testMethod);

	/**
	 * Compile a string representation from all simple parameter type names.
	 *
	 * @param method the method providing parameter types for the result
	 * @return a string representation of all parameter types of the
	 *         supplied method or {@code "()"} if the method has no parameters
	 */
	static String parameterTypesAsString(Method method) {
		return '(' + ClassUtils.nullSafeToString(Class::getSimpleName, method.getParameterTypes()) + ')';
	}

	/**
	 * Default display name generator.
	 *
	 * <p>The implementation matches the published behaviour when Jupiter 5.0.0
	 * was released.
	 */
	class DefaultGenerator implements DisplayNameGenerator {
		@Override
		public String generateDisplayNameForClass(Class<?> testClass) {
			Preconditions.notNull(testClass, "Test class must not be null");
			String name = testClass.getName();
			int lastDot = name.lastIndexOf('.');
			return name.substring(lastDot + 1);
		}

		@Override
		public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
			Preconditions.notNull(nestedClass, "Nested test class must not be null");
			return nestedClass.getSimpleName();
		}

		@Override
		public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
			Preconditions.notNull(testClass, "Test class must not be null");
			Preconditions.notNull(testMethod, "Test method must not be null");
			return testMethod.getName() + parameterTypesAsString(testMethod);
		}
	}

	/**
	 * Replace all underscore characters with spaces.
	 *
	 * <p>The {@code ReplaceUnderscores} generator replaces all underscore characters
	 * ({@code '_'}) found in class and method names with space characters: {@code ' '}.
	 */
	class ReplaceUnderscores extends DefaultGenerator {

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
			Preconditions.notNull(testClass, "Test class must not be null");
			Preconditions.notNull(testMethod, "Test method must not be null");
			// don't replace underscores in parameter type names
			return replaceUnderscores(testMethod.getName()) + parameterTypesAsString(testMethod);
		}

		private String replaceUnderscores(String name) {
			return name.replace('_', ' ');
		}
	}

}
