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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@code @DisplayNameGeneration} is used to declare...
 *
 * <p>Display names are typically used for test reporting in IDEs and build
 * tools and may contain spaces, special characters, and even emoji.
 *
 * @since 5.4
 * @see DisplayName
 * @see DisplayNameGenerator
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@API(status = EXPERIMENTAL, since = "5.4")
public @interface DisplayNameGeneration {

	/**
	 * @return custom display name generator implementation or {@link DisplayNameGenerator}
	 *         to use the {@code Style} provided by the {@link #value()} property
	 */
	Class<? extends DisplayNameGenerator> generator() default DisplayNameGenerator.class;

	/**
	 * @return the style to use, can be overridden by a custom display name generator implementation
	 */
	Style value() default Style.DEFAULT;

	/**
	 * TODO Javadoc
	 */
	enum Style implements DisplayNameGenerator {
		/**
		 * Default display name generator.
		 */
		DEFAULT {
			/**
			 * TODO Javadoc
			 */
			@Override
			public String generateDisplayNameForClass(Class<?> testClass) {
				Preconditions.notNull(testClass, "Test class must not be null");
				String name = testClass.getName();
				int lastDot = name.lastIndexOf('.');
				return name.substring(lastDot + 1);
			}

			/**
			 * TODO Javadoc
			 */
			@Override
			public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
				Preconditions.notNull(nestedClass, "Nested test class must not be null");
				return nestedClass.getSimpleName();
			}

			/**
			 * TODO Javadoc
			 */
			@Override
			public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
				Preconditions.notNull(testClass, "Test class must not be null");
				Preconditions.notNull(testMethod, "Test method must not be null");
				return testMethod.getName() + parameterTypesAsString(testMethod);
			}
		},

		/**
		 * TODO Javadoc
		 */
		UNDERSCORE {
			/**
			 * TODO Javadoc
			 */
			@Override
			public String generateDisplayNameForClass(Class<?> testClass) {
				return replaceUnderscore(DEFAULT.generateDisplayNameForClass(testClass));
			}

			/**
			 * TODO Javadoc
			 */
			@Override
			public String generateDisplayNameForNestedClass(Class<?> nestedClass) {
				return replaceUnderscore(DEFAULT.generateDisplayNameForNestedClass(nestedClass));
			}

			/**
			 * TODO Javadoc
			 */
			@Override
			public String generateDisplayNameForMethod(Class<?> testClass, Method testMethod) {
				Preconditions.notNull(testClass, "Test class must not be null");
				Preconditions.notNull(testMethod, "Test method must not be null");
				return replaceUnderscore(testMethod.getName()) + parameterTypesAsString(testMethod);
			}

			private String replaceUnderscore(String name) {
				return name.replace('_', ' ');
			}
		};

		/**
		 * @return a string representation of all parameter types of the
		 *         passed method or {@code "()"} if the method has no parameters
		 */
		private static String parameterTypesAsString(Method testMethod) {
			return '(' + ClassUtils.nullSafeToString(Class::getSimpleName, testMethod.getParameterTypes()) + ')';
		}

	}

}
