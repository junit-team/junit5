/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import static org.apiguardian.api.API.Status.STABLE;

import org.apiguardian.api.API;

/**
 * {@code BeforeParameterizedTestExecutionCallback} defines the API for {@link Extension
 * Extensions} that wish to provide additional behavior to tests immediately
 * before each test is executed.
 *
 * <p>Such callbacks will be invoked after any user defined setup methods (e.g.,
 * {@link org.junit.jupiter.api.BeforeEach @BeforeEach} methods).
 *
 * <p>Concrete implementations often implement {@link AfterTestExecutionCallback}
 * as well.
 *
 * <h3>Constructor Requirements</h3>
 *
 * <p>Consult the documentation in {@link Extension} for details on
 * constructor requirements.
 *
 * @since 5.0
 * @see org.junit.jupiter.api.Test
 * @see AfterTestExecutionCallback
 * @see BeforeEachCallback
 * @see AfterEachCallback
 * @see BeforeAllCallback
 * @see AfterAllCallback
 */
@FunctionalInterface
@API(status = STABLE, since = "5.0")
public interface BeforeParameterizedTestExecutionCallback extends Extension {
	/**
	 * Callback that is invoked <em>immediately before</em> each test is executed.
	 * This callback is invoked <em>after</em> the {@link BeforeTestExecutionCallback}
	 *
	 * @param context the current extension context; never {@code null}
	 * @param arguments a copy of the arguments with which this test method invocation will use; never {@code null}
	 */
	void beforeParameterizedTestExecution(ExtensionContext context, Object[] arguments);
}
