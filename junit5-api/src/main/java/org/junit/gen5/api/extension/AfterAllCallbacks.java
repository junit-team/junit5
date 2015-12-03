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
 * {@code AfterAllCallbacks} defines the API for {@link TestExtension
 * TestExtensions} that wish to provide additional behavior to tests
 * {@linkplain #preAfterAll before} all {@code @AfterAll} methods have
 * been invoked <em>or</em> {@linkplain #postAfterAll after} all
 * {@code @AfterAll} methods have been invoked.
 *
 * <p>Concrete implementations often implement {@link BeforeAllCallbacks}
 * as well.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @since 5.0
 * @see org.junit.gen5.api.AfterAll
 * @see BeforeAllCallbacks
 * @see BeforeEachCallbacks
 * @see AfterEachCallbacks
 */
public interface AfterAllCallbacks extends TestExtension {

	/**
	 * Callback that is invoked <em>before</em> all {@code @AfterAll}
	 * methods have been invoked.
	 *
	 * @param testExecutionContext the current test execution context
	 */
	default void preAfterAll(TestExecutionContext testExecutionContext) throws Exception {
		/* no-op */
	}

	/**
	 * Callback that is invoked <em>after</em> all {@code @AfterAll}
	 * methods have been invoked.
	 *
	 * @param testExecutionContext the current test execution context
	 */
	default void postAfterAll(TestExecutionContext testExecutionContext) throws Exception {
		/* no-op */
	}

}
