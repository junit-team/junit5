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

import java.util.Optional;

import org.apiguardian.api.API;

/**
 * {@code TestWatcher} defines the API for {@link Extension Extensions} that
 * wish to process test results.
 *
 * <p>The methods in this API are called after a test has been skipped or
 * executed. Any {@link ExtensionContext.Store.CloseableResource CloseableResource}
 * objects stored in the {@link ExtensionContext.Store Store} of the supplied
 * {@link ExtensionContext} will have already been <strong>closed</strong> before
 * methods in this API are invoked.
 *
 * <p>Please note that this API is currently only used to report the results of
 * method-based tests.
 *
 * <p>Extensions implementing this API can be registered at any level.
 *
 * <h3>Exception Handling</h3>
 *
 * <p>In contrast to other {@link Extension} APIs, a {@code TestWatcher} is not
 * permitted to adversely influence the execution of tests. Consequently, any
 * exception thrown by a {@code TestWatcher} will be logged at {@code WARNING}
 * level and will not allowed to propagate or fail test execution.
 *
 * @since 5.4
 */
@API(status = EXPERIMENTAL, since = "5.4")
public interface TestWatcher extends Extension {

	/**
	 * Invoked after a disabled test has been skipped.
	 *
	 * @param context the current extension context; never {@code null}
	 * @param reason the reason the test is disabled; never {@code null}
	 */
	void testDisabled(ExtensionContext context, Optional<String> reason);

	/**
	 * Invoked after a test has completed successfully.
	 *
	 * @param context the current extension context; never {@code null}
	 */
	void testSuccessful(ExtensionContext context);

	/**
	 * Invoked after a test has been aborted.
	 *
	 * @param context the current extension context; never {@code null}
	 * @param cause the throwable responsible for the test being aborted; may be {@code null}
	 */
	void testAborted(ExtensionContext context, Throwable cause);

	/**
	 * Invoked after a test has failed.
	 *
	 * @param context the current extension context; never {@code null}
	 * @param cause the throwable that caused test failure; may be {@code null}
	 */
	void testFailed(ExtensionContext context, Throwable cause);

}
