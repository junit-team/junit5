/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

/**
 * {@code TestInstancePreDestroyCallback} defines the API for {@link Extension
 * Extensions} that wish to process test instances <em>after</em> they have been
 * used in tests but <em>before</em> they are destroyed.
 *
 * <p>Common use cases include releasing resources that have been created for
 * the test instance, invoking custom clean-up methods on the test instance, etc.
 *
 * <p>Extensions that implement {@code TestInstancePreDestroyCallback} must be
 * registered at the class level if the test class is configured with
 * {@link org.junit.jupiter.api.TestInstance.Lifecycle @TestInstance(Lifecycle.PER_CLASS)}
 * semantics. If the test class is configured with
 * {@link org.junit.jupiter.api.TestInstance.Lifecycle @TestInstance(Lifecycle.PER_METHOD)}
 * semantics, {@code TestInstancePreDestroyCallback} extensions may be registered
 * at the class level or at the method level. In the latter case, the
 * {@code TestInstancePreDestroyCallback} extension will only be applied to the
 * test method for which it is registered.
 *
 * <h3>Constructor Requirements</h3>
 *
 * <p>Consult the documentation in {@link Extension} for details on constructor
 * requirements.
 *
 * @since 5.6
 * @see #preDestroyTestInstance(ExtensionContext)
 * @see TestInstancePostProcessor
 * @see TestInstanceFactory
 * @see ParameterResolver
 */
@FunctionalInterface
@API(status = EXPERIMENTAL, since = "5.6")
public interface TestInstancePreDestroyCallback extends Extension {

	/**
	 * Callback for processing a test instance before it is destroyed.
	 *
	 * @param context the current extension context; never {@code null}
	 * @see ExtensionContext#getTestInstance()
	 * @see ExtensionContext#getRequiredTestInstance()
	 */
	void preDestroyTestInstance(ExtensionContext context) throws Exception;

}
