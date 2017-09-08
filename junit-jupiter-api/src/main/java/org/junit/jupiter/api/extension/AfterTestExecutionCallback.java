/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api.extension;

import static org.junit.platform.commons.meta.API.Status.STABLE;

import org.junit.platform.commons.meta.API;

/**
 * {@code AfterTestExecutionCallback} defines the API for {@link Extension
 * Extensions} that wish to provide additional behavior to tests immediately
 * after each test has been executed.
 *
 * <p>Such callbacks will be invoked before any user defined teardown methods (e.g.,
 * {@link org.junit.jupiter.api.AfterEach @AfterEach} methods).
 *
 * <p>Concrete implementations often implement {@link BeforeTestExecutionCallback}
 * as well.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @since 5.0
 * @see org.junit.jupiter.api.Test
 * @see BeforeTestExecutionCallback
 * @see BeforeEachCallback
 * @see AfterEachCallback
 * @see BeforeAllCallback
 * @see AfterAllCallback
 */
@FunctionalInterface
@API(status = STABLE, since = "5.0")
public interface AfterTestExecutionCallback extends Extension {

	/**
	 * Callback that is invoked <em>immediately after</em> each test has been executed.
	 *
	 * @param context the current extension context; never {@code null}
	 */
	void afterTestExecution(ExtensionContext context) throws Exception;

}
