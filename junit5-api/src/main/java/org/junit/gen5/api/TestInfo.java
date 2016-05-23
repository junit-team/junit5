/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.commons.meta.API;

/**
 * {@code TestInfo} is used to inject information about the current test
 * into to {@code @Test}, {@code @BeforeEach}, {@code @AfterEach},
 * {@code @BeforeAll}, and {@code @AfterAll} methods.
 *
 * <p>If a method parameter is of type {@link TestInfo}, JUnit will supply
 * an instance of {@code TestInfo} corresponding to the current test as the
 * value for the parameter.
 *
 * @since 5.0
 * @see Test
 * @see DisplayName
 */
@API(Experimental)
public interface TestInfo {

	/**
	 * Get the display name of the current test.
	 *
	 * <p>The display name is either the canonical name of the test or a
	 * custom name configured via {@link DisplayName @DisplayName}.
	 *
	 * @return the display name of the test; never {@code null}
	 */
	String getDisplayName();

	/**
	 * Get the set of all tags. Might be declared directly on this element
	 * or "inherited" from an outer context.
	 */
	Set<String> getTags();

	/**
	 * Get the {@link Class} associated with the current test, if available.
	 */
	Optional<Class<?>> getTestClass();

	/**
	 * Get the {@link Method} associated with the current test, if available.
	 */
	Optional<Method> getTestMethod();

}
