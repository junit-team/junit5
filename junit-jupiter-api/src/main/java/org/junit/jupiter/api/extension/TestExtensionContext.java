/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api.extension;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.util.Optional;

import org.junit.platform.commons.meta.API;

/**
 * {@code TestExtensionContext} encapsulates the <em>context</em> in which
 * the current test is being executed.
 *
 * @since 5.0
 */
@API(Experimental)
public interface TestExtensionContext extends ExtensionContext {

	/**
	 * Get the exception that was thrown during execution of the test associated
	 * with this {@code TestExtensionContext}, if available.
	 *
	 * <p>This method is typically used for logging and tracing purposes. If you
	 * wish to actually <em>handle</em> an exception thrown during test execution,
	 * implement the {@link TestExecutionExceptionHandler} API.
	 *
	 * <p>Unlike the exception passed to a {@code TestExecutionExceptionHandler},
	 * a <em>test exception</em> returned by this method can be any exception
	 * thrown during the invocation of a {@code @Test} method, its surrounding
	 * {@code @BeforeEach} and {@code @AfterEach} methods, or a test-level
	 * {@link Extension}. Note, however, that this method will never return an
	 * exception swallowed by a {@code TestExecutionExceptionHandler}.
	 * Furthermore, if multiple exceptions have been thrown during test execution,
	 * the exception returned by this method will be the first such exception with
	 * all additional exceptions {@linkplain Throwable#addSuppressed(Throwable)
	 * suppressed} in the first one.
	 *
	 * @return an {@code Optional} containing the exception thrown; never
	 * {@code null} but potentially empty if test execution has not (yet)
	 * resulted in an exception
	 */
	Optional<Throwable> getTestException();

}
