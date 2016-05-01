/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import org.junit.gen5.commons.meta.API;

/**
 * {@code AfterTestCallback} defines the API for {@link Extension Extensions}
 * that wish to provide additional behavior to tests after each test method has
 * been invoked.
 *
 * <p>Such callbacks will be invoked before any user defined setup methods (e.g.,
 * {@link org.junit.gen5.api.AfterEach @AfterEach} methods).
 *
 * <p>Concrete implementations often implement {@link BeforeTestMethodCallback} as well.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @since 5.0
 * @see org.junit.gen5.api.Test
 * @see BeforeTestMethodCallback
 * @see BeforeEachCallback
 * @see AfterEachCallback
 * @see BeforeAllCallback
 * @see AfterAllCallback
 */
@FunctionalInterface
@API(Experimental)
public interface AfterTestMethodCallback extends Extension {

	/**
	 * Callback that is invoked <em>after</em> each test method has been invoked.
	 *
	 * @param context the current test extension context
	 */
	void afterTestMethod(TestExtensionContext context) throws Exception;

}
