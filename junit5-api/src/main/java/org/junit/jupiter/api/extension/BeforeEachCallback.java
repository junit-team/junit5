/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api.extension;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import org.junit.gen5.commons.meta.API;

/**
 * {@code BeforeEachCallback} defines the API for {@link Extension Extensions}
 * that wish to provide additional behavior to tests before each test is invoked.
 *
 * <p>In this context, the term <em>test</em> refers to the actual test method
 * plus any user defined setup methods (e.g.,
 * {@link org.junit.jupiter.api.BeforeEach @BeforeEach} methods).
 *
 * <p>Concrete implementations often implement {@link AfterEachCallback} as well.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @since 5.0
 * @see org.junit.jupiter.api.BeforeEach
 * @see AfterEachCallback
 * @see BeforeTestExecutionCallback
 * @see AfterTestExecutionCallback
 * @see BeforeAllCallback
 * @see AfterAllCallback
 */
@FunctionalInterface
@API(Experimental)
public interface BeforeEachCallback extends Extension {

	/**
	 * Callback that is invoked <em>before</em> each test is invoked.
	 *
	 * @param context the current extension context; never {@code null}
	 */
	void beforeEach(TestExtensionContext context) throws Exception;

}
