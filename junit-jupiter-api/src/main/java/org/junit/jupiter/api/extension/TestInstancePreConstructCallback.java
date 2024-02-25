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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;
import org.junit.jupiter.api.TestInstance.Lifecycle;

/**
 * {@code TestInstancePreConstructCallback} defines the API for {@link Extension
 * Extensions} that wish to be invoked <em>prior</em> to creation of test instances.
 *
 * <p>This extension is a symmetric counterpart to {@link TestInstancePreDestroyCallback}.
 * The use cases for this extension may include preparing context-sensitive arguments
 * that are injected into the instance's constructor.
 *
 * <p>Extensions that implement {@code TestInstancePreConstructCallback} must be
 * registered at the class level if the test class is configured with
 * {@link Lifecycle @TestInstance(Lifecycle.PER_CLASS)} semantics. If the test
 * class is configured with
 * {@link Lifecycle @TestInstance(Lifecycle.PER_METHOD)} semantics,
 * {@code TestInstancePreConstructCallback} extensions may be registered at the
 * class level or at the method level. In the latter case, the extension will
 * only be applied to the test method for which it is registered.
 *
 * <h2>Constructor Requirements</h2>
 *
 * <p>Consult the documentation in {@link Extension} for details on constructor
 * requirements.
 *
 * @since 5.9
 * @see TestInstancePreDestroyCallback
 * @see TestInstanceFactory
 * @see ParameterResolver
 */
@FunctionalInterface
@API(status = EXPERIMENTAL, since = "5.9")
public interface TestInstancePreConstructCallback extends Extension {

	/**
	 * Callback invoked prior to test instances being constructed.
	 *
	 * @param factoryContext the context for the test instance about to be instantiated;
	 * never {@code null}
	 * @param context the current extension context; never {@code null}
	 */
	void preConstructTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext context) throws Exception;

}
