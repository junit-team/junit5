/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

/**
 * {@code BeforeEachCallbacks} defines the API for {@link TestExtension
 * TestExtensions} that wish to provide additional behavior to tests
 * {@linkplain #preBeforeEach before} all {@code @BeforeEach} methods have
 * been invoked <em>or</em> {@linkplain #postBeforeEach after} all
 * {@code @BeforeEach} methods have been invoked.
 *
 * <p>Concrete implementations often implement {@link AfterEachCallbacks}
 * as well.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @author Sam Brannen
 * @since 5.0
 * @see org.junit.gen5.api.BeforeEach
 * @see AfterEachCallbacks
 * @see BeforeAllCallbacks
 * @see AfterAllCallbacks
 */
public interface BeforeEachCallbacks extends TestExtension {

	/**
	 * Callback that is invoked <em>before</em> all {@code @BeforeEach}
	 * methods have been invoked.
	 *
	 * @param methodExecutionContext the current test execution context
	 * @param testInstance the instance under test
	 */
	default void preBeforeEach(TestExecutionContext methodExecutionContext, Object testInstance) throws Exception {
		/* no-op */
	}

	/**
	 * Callback that is invoked <em>after</em> all {@code @BeforeEach}
	 * methods have been invoked but <em>before</em> the actual test method
	 * is invoked.
	 *
	 * @param methodExecutionContext the current test execution context
	 * @param testInstance the instance under test
	 */
	default void postBeforeEach(TestExecutionContext methodExecutionContext, Object testInstance) throws Exception {
		/* no-op */
	}

}
