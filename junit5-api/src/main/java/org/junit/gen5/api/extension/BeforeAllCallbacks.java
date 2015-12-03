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
 * {@code BeforeAllCallbacks} defines the API for {@link TestExtension
 * TestExtensions} that wish to provide additional behavior to tests
 * {@linkplain #preBeforeAll before} all {@code @BeforeAll} methods have
 * been invoked <em>or</em> {@linkplain #postBeforeAll after} all
 * {@code @BeforeAll} methods have been invoked.
 *
 * <p>Concrete implementations often implement {@link AfterAllCallbacks}
 * as well.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @since 5.0
 * @see org.junit.gen5.api.BeforeAll
 * @see AfterAllCallbacks
 * @see BeforeEachCallbacks
 * @see AfterEachCallbacks
 */
public interface BeforeAllCallbacks extends TestExtension {

	/**
	 * Callback that is invoked <em>before</em> all {@code @BeforeAll}
	 * methods have been invoked.
	 *
	 * @param testExecutionContext the current test execution context
	 */
	default void preBeforeAll(TestExecutionContext testExecutionContext) throws Exception {
		/* no-op */
	}

	/**
	 * Callback that is invoked <em>after</em> all {@code @BeforeAll}
	 * methods have been invoked.
	 *
	 * @param testExecutionContext the current test execution context
	 */
	default void postBeforeAll(TestExecutionContext testExecutionContext) throws Exception {
		/* no-op */
	}

}
