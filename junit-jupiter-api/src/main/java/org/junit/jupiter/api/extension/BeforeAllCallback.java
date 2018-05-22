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
 * {@code BeforeAllCallback} defines the API for {@link Extension Extensions}
 * that wish to provide additional behavior to test containers before all tests
 * are invoked.
 *
 * <p>Concrete implementations often implement {@link AfterAllCallback} as well.
 *
 * <p>Extensions that implement {@code BeforeAllCallback} must be registered at
 * the class level.
 *
 * <h3>Constructor Requirements</h3>
 *
 * <p>Consult the documentation in {@link Extension} for details on
 * constructor requirements.
 *
 * @since 5.0
 * @see org.junit.jupiter.api.BeforeAll
 * @see AfterAllCallback
 * @see BeforeEachCallback
 * @see AfterEachCallback
 * @see BeforeTestExecutionCallback
 * @see AfterTestExecutionCallback
 */
@FunctionalInterface
@API(status = STABLE, since = "5.0")
public interface BeforeAllCallback extends Extension {

	/**
	 * Callback that is invoked once <em>before</em> all tests in the current
	 * container.
	 *
	 * @param context the current extension context; never {@code null}
	 */
	void beforeAll(ExtensionContext context) throws Exception;

}
