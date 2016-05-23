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
 * {@code TestInfo} is used to inject information about the current test or
 * container into to {@code @Test}, {@code @BeforeEach}, {@code @AfterEach},
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
	 * Get the display name of the current test or container.
	 *
	 * <p>The display name is either a default name or a custom name configured
	 * via {@link DisplayName @DisplayName}.
	 *
	 * <h3>Default Display Names</h3>
	 *
	 * <p>If the context in which {@code TestInfo} is used is at the container
	 * level, the default display name is the fully qualified class name for the
	 * test class. If the context in which {@code TestInfo} is used is at the
	 * test level, the default display name is the name of the test method
	 * concatenated with a comma-separated list of parameter types in parentheses.
	 * The names of parameter types are retrieved using {@link Class#getSimpleName()}.
	 * For example, the default display name for the following test method is
	 * {@code testUser(TestInfo, User)}.
	 *
	 * <pre style="code">
	 *   {@literal @}Test
	 *   void testUser(TestInfo testInfo, {@literal @}Mock User user) { ... }
	 * </pre>
	 *
	 * <p>Note that display names are typically used for test reporting in IDEs
	 * and build tools and may contain spaces, special characters, and even emoji.
	 *
	 * @return the display name of the test or container; never {@code null} or empty
	 */
	String getDisplayName();

	/**
	 * Get the set of all tags for the current test or container.
	 *
	 * <p>Tags may be declared directly on the test element or <em>inherited</em>
	 * from an outer context.
	 */
	Set<String> getTags();

	/**
	 * Get the {@link Class} associated with the current test or container, if available.
	 */
	Optional<Class<?>> getTestClass();

	/**
	 * Get the {@link Method} associated with the current test, if available.
	 */
	Optional<Method> getTestMethod();

}
