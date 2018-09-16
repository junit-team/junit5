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
	 *
	 * TODO Javadoc
	 */
	String generateDisplayNameForClass(Class<?> testClass);

	/**
	 * Generate a display name for the given {@link Nested} inner test class.
	 *
	 * TODO Javadoc
	 */
	String generateDisplayNameForNestedClass(Class<?> nestedClass);

	/**
	 * Generate a display name for the given method.
	 *
	 * TODO Javadoc
	 *
	 * @implNote The class instance passed as {@code testClass} may differ from
	 * the returned class by {@code testMethod.getDeclaringClass()}: e.g., when
	 * a test method is inherited from a super class.
	 *
	 * @param testClass the class the test method is invoked on
	 * @param testMethod method to generate a display name for
	 */
	String generateDisplayNameForMethod(Class<?> testClass, Method testMethod);
}
