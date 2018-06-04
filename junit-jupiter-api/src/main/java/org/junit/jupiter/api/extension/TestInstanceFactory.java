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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

/**
 * {@code TestInstanceFactory} defines the API for {@link Extension
 * Extensions} that wish to <em>create</em> test instances.
 *
 * <p>Common use cases include creating test instances with special construction
 * requirements or acquiring the test from a dependency injection framework.
 *
 * <p>Only one {@code TestInstanceFactory} is allowed to be registered for each
 * test class in the test class hierarchy; with lower level factories overriding
 * factories registered at higher levels in the hierarchy. Registering multiple
 * factories for any single test class will result in an exception being thrown
 * for all the tests in that test class and any nested test classes below.
 *
 * <p>Extensions that implement {@code TestInstanceFactory} must be registered
 * at the class level.
 *
 * <h3>Constructor Requirements</h3>
 *
 * <p>Consult the documentation in {@link Extension} for details on
 * constructor requirements.
 *
 * @since 5.3
 * @see TestInstanceFactoryContext
 * @see #instantiateTestClass(TestInstanceFactoryContext, ExtensionContext)
 */
@API(status = EXPERIMENTAL, since = "5.3")
public interface TestInstanceFactory extends Extension {

	/**
	 * Callback for producing a test instance for the supplied context.
	 *
	 * <p><strong>Note</strong>: the {@code ExtensionContext} supplied to a
	 * {@code TestInstanceFactory} will always return an empty
	 * {@link java.util.Optional} value from
	 * {@link ExtensionContext#getTestInstance() getTestInstance()}. A
	 * {@code TestInstanceFactory} should therefore only attempt to create the
	 * required test instance.
	 *
	 * @param factoryContext the context for the test class to be instantiated;
	 * never {@code null}
	 * @param extensionContext the current extension context; never {@code null}
	 * @return the required test instance; never {@code null}
	 * @throws TestInstantiationException when an error occurs with the
	 * invocation of a factory
	 */
	Object instantiateTestClass(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext)
			throws TestInstantiationException;

}
