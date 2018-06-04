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
import org.junit.jupiter.api.Nested;

/**
 * {@code TestInstanceFactory} defines the API for {@link Extension
 * Extensions} that wish to <em>create</em> test instances.
 *
 * <p>Common use cases include creating test instances with special construction
 * requirements or acquiring the test from a dependency injection framework.
 *
 * <p>Extensions that implement {@code TestInstanceFactory} must be
 * registered at the class level.
 *
 * <h3>Constructor Requirements</h3>
 *
 * <p>Consult the documentation in {@link Extension} for details on
 * constructor requirements.
 *
 * @since 5.0
 * @see #instantiateTestClass(Class, ExtensionContext)
 * @see #instantiateNestedTestClass(Class, Object, ExtensionContext)
 */
@API(status = STABLE, since = "5.0")
public interface TestInstanceFactory extends Extension {

	/**
	 * Callback for producing an instance of {@code testClass}. This method is invoked
	 * when the class is a top level test class.
	 *
	 * <p><strong>Note</strong>: the {@code ExtensionContext} supplied to a
	 * {@code TestInstanceFactory} will always return an empty
	 * {@link java.util.Optional} value from {@link ExtensionContext#getTestInstance()
	 * getTestInstance()}. A {@code TestInstanceFactory} should therefore
	 * only attempt to create the required test instance.
	 *
	 * @param testClass the test class to instantiate or otherwise obtain
	 * @param context the current extension context; never {@code null}
	 * @return The required test instance; never {@code null}
	 */
	Object instantiateTestClass(Class<?> testClass, ExtensionContext context) throws Exception;

	/**
	 * Callback for producing an instance of {@code testClass}. This method is only
	 * called when the {@code testClass} is a {@link Nested} test class.
	 *
	 * <p><strong>Note</strong>: the {@code ExtensionContext} supplied to a
	 * {@code TestInstanceFactory} will always return an empty
	 * {@link java.util.Optional} value from {@link ExtensionContext#getTestInstance()
	 * getTestInstance()}. A {@code TestInstanceFactory} should therefore
	 * only attempt to create the required test instance.
	 *
	 * @param testClass the test class to instantiate or otherwise obtain
	 * @param outerInstance instance of outer test class (if any)
	 * @param context the current extension context; never {@code null}
	 * @return The required test instance; never {@code null}
	 */
	Object instantiateNestedTestClass(Class<?> testClass, Object outerInstance, ExtensionContext context)
			throws Exception;

}
