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
 * {@code AfterEachCallbacks} defines the API for {@link TestExtension
 * TestExtensions} that wish to provide additional behavior to tests
 * after each test method has been invoked.
 *
 * <p>Concrete implementations often implement {@link BeforeEachExtensionPoint}
 * as well.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @since 5.0
 * @see org.junit.gen5.api.AfterEach
 * @see BeforeEachExtensionPoint
 * @see BeforeAllExtensionPoint
 * @see AfterAllExtensionPoint
 */
@FunctionalInterface
public interface AfterEachExtensionPoint extends ExtensionPoint {

	/**
	 * Callback that is invoked after each test
	 * method has been invoked.
	 *
	 * @param context the current test extension context
	 */
	void afterEach(TestExtensionContext context) throws Exception;

}
