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
 * {@link org.junit.jupiter.api.Test @Test} methods and
 * {@link org.junit.jupiter.api.TestTemplate @TestTemplate} methods (e.g.,
 * {@code @RepeatedTest} and {@code @ParameterizedTest}). Moreover, if there is a
 * failure at the class level &mdash; for example, an exception thrown by a
 * {@code @BeforeAll} method &mdash; no test results will be reported.
 *
 * <p>Extensions implementing this API can be registered at any level.
 *
 * <h3>Exception Handling</h3>
 *
 * <p>In contrast to other {@link Extension} APIs, a {@code TestWatcher} is not
 * permitted to adversely influence the execution of tests. Consequently, any
 * exception thrown by a method in the {@code TestWatcher} API will be logged at
 * {@code WARNING} level and will not be allowed to propagate or fail test
 * execution.
 *
 * @since 5.4
 */
@API(status = EXPERIMENTAL, since = "5.4")
public interface TestWatcher extends Extension {

	/**
	 * Invoked after a disabled test has been skipped.
	 *
	 * <p>The default implementation does nothing. Concrete implementations can
	 * override this method as appropriate.
	 *
	 * @param context the current extension context; never {@code null}
	 * @param reason the reason the test is disabled; never {@code null} but
	 * potentially <em>empty</em>
	 */
	default void testDisabled(ExtensionContext context, Optional<String> reason) {
		/* no-op */
	}

	/**
	 * Invoked after a test has completed successfully.
	 *
	 * <p>The default implementation does nothing. Concrete implementations can
	 * override this method as appropriate.
	 *
	 * @param context the current extension context; never {@code null}
	 */
	default void testSuccessful(ExtensionContext context) {
		/* no-op */
	}

	/**
	 * Invoked after a test has been aborted.
	 *
	 * <p>The default implementation does nothing. Concrete implementations can
	 * override this method as appropriate.
	 *
	 * @param context the current extension context; never {@code null}
	 * @param cause the throwable responsible for the test being aborted; may be {@code null}
	 */
	default void testAborted(ExtensionContext context, Throwable cause) {
		/* no-op */
	}

	/**
	 * Invoked after a test has failed.
	 *
	 * <p>The default implementation does nothing. Concrete implementations can
	 * override this method as appropriate.
	 *
	 * @param context the current extension context; never {@code null}
	 * @param cause the throwable that caused test failure; may be {@code null}
	 */
	default void testFailed(ExtensionContext context, Throwable cause) {
		/* no-op */
	}

}
