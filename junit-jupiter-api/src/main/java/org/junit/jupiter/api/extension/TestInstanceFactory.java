/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import static org.apiguardian.api.API.Status.STABLE;

import org.apiguardian.api.API;

/**
 * {@code TestInstanceFactory} defines the API for {@link Extension
 * Extensions} that wish to {@linkplain #createTestInstance create} test instances.
 *
 * <p>Common use cases include creating test instances with special construction
 * requirements or acquiring the test instance from a dependency injection
 * framework.
 *
 * <p>Extensions that implement {@code TestInstanceFactory} must be registered
 * at the class level.
 *
 * <h2>Warning</h2>
 *
 * <p>Only one {@code TestInstanceFactory} is allowed to be registered for any
 * given test class. Registering multiple factories for any single test class
 * will result in an exception being thrown for all tests in that class, in any
 * subclass, and in any nested class. Note that any {@code TestInstanceFactory}
 * registered in a {@linkplain Class#getSuperclass() superclass} or
 * {@linkplain Class#getEnclosingClass() enclosing} class (i.e., in the case of
 * a {@code @Nested} test class) is <em>inherited</em>. It is therefore the
 * user's responsibility to ensure that only a single {@code TestInstanceFactory}
 * is registered for any specific test class.
 *
 * <h2>Constructor Requirements</h2>
 *
 * <p>Consult the documentation in {@link Extension} for details on
 * constructor requirements.
 *
 * @since 5.3
 * @see #createTestInstance(TestInstanceFactoryContext, ExtensionContext)
 * @see TestInstanceFactoryContext
 * @see TestInstancePostProcessor
 * @see TestInstancePreDestroyCallback
 * @see ParameterResolver
 */
@FunctionalInterface
@API(status = STABLE, since = "5.7")
public interface TestInstanceFactory extends Extension {

	/**
	 * Callback for creating a test instance for the supplied context.
	 *
	 * <p><strong>Note</strong>: the {@code ExtensionContext} supplied to a
	 * {@code TestInstanceFactory} will always return an empty
	 * {@link java.util.Optional} value from
	 * {@link ExtensionContext#getTestInstance() getTestInstance()} since the
	 * test instance cannot exist before it has been created by a
	 * {@code TestInstanceFactory} or the framework itself.
	 *
	 * @param factoryContext the context for the test class to be instantiated;
	 * never {@code null}
	 * @param extensionContext the current extension context; never {@code null}
	 * @return the test instance; never {@code null}
	 * @throws TestInstantiationException if an error occurs while creating the
	 * test instance
	 */
	Object createTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext extensionContext)
			throws TestInstantiationException;

}
