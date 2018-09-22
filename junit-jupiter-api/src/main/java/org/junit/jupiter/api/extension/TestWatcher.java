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
 * The {@code TestWatcher} interface defines an extension API for processing test results.
 *
 * <p> Interface methods are called after a test has been processed. Currently,
 * only method based tests will be reported.
 *
 * <p>Extensions implementing this API can be registered at any level, reporting subsequent
 * tests down the chain.
 *
 * <p>Any {@code ClosableResource} objects stored in the injected {@link ExtensionContext}
 * have already been <strong>closed</strong> at invocation time.
 *
 * @since 5.4
 */
@API(status = EXPERIMENTAL, since = "5.4")
public interface TestWatcher extends Extension {

	/**
	 * Invoked after a test has completed successfully.
	 *
	 * @param context the current extension context; never {@code null}
	 */
	void testSuccessful(ExtensionContext context);

	/**
	 * Invoked after a test was aborted.
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

	/**
	 * Invoked after skipping a disabled test.
	 *
	 * @param context the current extension context; never {@code null}
	 * @param reason the reason for skipping the test; never {@code null}
	 */
	void testDisabled(ExtensionContext context, Optional<String> reason);
}
