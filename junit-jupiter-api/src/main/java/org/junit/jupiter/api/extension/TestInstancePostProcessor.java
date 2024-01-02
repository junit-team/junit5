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
 * {@code TestInstancePostProcessor} defines the API for {@link Extension
 * Extensions} that wish to <em>post-process</em> test instances.
 *
 * <p>Common use cases include injecting dependencies into the test
 * instance, invoking custom initialization methods on the test instance,
 * etc.
 *
 * <p>Extensions that implement {@code TestInstancePostProcessor} must be
 * registered at the class level.
 *
 * <h2>Constructor Requirements</h2>
 *
 * <p>Consult the documentation in {@link Extension} for details on
 * constructor requirements.
 *
 * @since 5.0
 * @see #postProcessTestInstance(Object, ExtensionContext)
 * @see TestInstancePreDestroyCallback
 * @see TestInstanceFactory
 * @see ParameterResolver
 */
@FunctionalInterface
@API(status = STABLE, since = "5.0")
public interface TestInstancePostProcessor extends Extension {

	/**
	 * Callback for post-processing the supplied test instance.
	 *
	 * <p><strong>Note</strong>: the {@code ExtensionContext} supplied to a
	 * {@code TestInstancePostProcessor} will always return an empty
	 * {@link java.util.Optional} value from {@link ExtensionContext#getTestInstance()
	 * getTestInstance()}. A {@code TestInstancePostProcessor} should therefore
	 * only attempt to process the supplied {@code testInstance}.
	 *
	 * @param testInstance the instance to post-process; never {@code null}
	 * @param context the current extension context; never {@code null}
	 */
	void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception;

}
