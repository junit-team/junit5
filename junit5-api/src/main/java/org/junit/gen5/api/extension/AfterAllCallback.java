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
 * {@code AfterAllCallback} defines the API for {@link Extension Extensions}
 * that wish to provide additional behavior to tests after all test methods
 * have been invoked.
 *
 * <p>Concrete implementations often implement {@link BeforeAllCallback} as well.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @since 5.0
 * @see org.junit.gen5.api.AfterAll
 * @see BeforeAllCallback
 * @see BeforeEachCallback
 * @see AfterEachCallback
 */
@FunctionalInterface
@API(Experimental)
public interface AfterAllCallback extends ExtensionPoint {

	/**
	 * Callback that is invoked <em>after</em> all test methods have been invoked.
	 *
	 * @param context the current container extension context
	 */
	void afterAll(ContainerExtensionContext context) throws Exception;

}
