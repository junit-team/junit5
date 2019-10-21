/*
 * Copyright 2015-2019 the original author or authors.
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
 * {@code TestInstancePreDestroyCallback} defines the API for {@link Extension
 * Extensions} that wish to process test instances <em>after</em> they have been
 * used in tests and <em>before</em> they are destroyed.
 *
 * <p>Common use cases include cleaning dependencies that have been injected into the
 * test instance, invoking custom de-initialization methods on the test instance, etc.
 *
 * <p>Extensions that implement {@code TestInstancePreDestroyCallback} must be
 * registered at the class level.
 *
 * <h3>Constructor Requirements</h3>
 *
 * <p>Consult the documentation in {@link Extension} for details on
 * constructor requirements.
 *
 * @since 5.6
 * @see #preDestroyTestInstance(Object, ExtensionContext)
 * @see TestInstanceFactory
 * @see ParameterResolver
 */
@FunctionalInterface
@API(status = STABLE, since = "5.6")
public interface TestInstancePreDestroyCallback extends Extension {

	/**
	 * Callback for processing the supplied test instance before it is destroyed.
	 *
	 * @param testInstance the instance to process; never {@code null}
	 * @param context the current extension context; never {@code null}
	 */
	void preDestroyTestInstance(Object testInstance, ExtensionContext context) throws Exception;
}
