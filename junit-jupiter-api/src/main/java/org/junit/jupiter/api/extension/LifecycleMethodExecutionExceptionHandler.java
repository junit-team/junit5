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
 * {@code LifecycleMethodExecutionExceptionHandler} defines the API for
 * {@link Extension Extensions} that wish to handle exceptions thrown during
 * execution of lifecycle methods (annotated with {@code @BeforeAll},
 * {@code @BeforeEach}, {@code @AfterEach} and {@code @AfterAll}.
 *
 * <p>Common use cases include swallowing an exception if it's anticipated,
 * logging or rolling back a transaction in certain error scenarios.
 *
 * <p>This extension needs to be declared on a class level if class level methods
 * ({@code @BeforeAll}, {@code @AfterAll}) are to be covered. If declared on Test
 * level, only handlers for {@code @BeforeEach} and {@code @AfterEach} will execute
 *
 * <h3>Constructor Requirements</h3>
 *
 * <p>Consult the documentation in {@link Extension} for details on constructor
 * requirements.
 *
 * @see TestExecutionExceptionHandler
 *
 * @since 5.5
 */
@API(status = STABLE, since = "5.5")
public interface LifecycleMethodExecutionExceptionHandler extends Extension {

	/**
	 * Handle the supplied {@link Throwable throwable}.
	 *
	 * <p>Implementors must perform one of the following.
	 * <ol>
	 * <li>Rethrow the supplied {@code throwable} <em>as is</em> which is the default implementation</li>
	 * <li>Swallow the supplied {@code throwable}, thereby preventing propagation.</li>
	 * <li>Throw a new exception, potentially wrapping the supplied {@code throwable}.</li>
	 * </ol>
	 *
	 * <p>If the supplied {@code throwable} is swallowed, subsequent
	 * {@code LifecycleMethodExecutionExceptionHandler} will not be invoked;
	 * otherwise, the next registered {@code LifecycleMethodExecutionExceptionHandler}
	 * (if there is one) will be invoked with any {@link Throwable} thrown by
	 * this handler.
	 *
	 * @param context the current extension context; never {@code null}
	 * @param throwable the {@code Throwable} to handle; never {@code null}
	 */
	default void handleBeforeAllMethodExecutionException(ExtensionContext context, Throwable throwable)
			throws Throwable {
		throw throwable;
	}

	/**
	 * Handle the supplied {@link Throwable throwable}.
	 *
	 * <p>Implementors must perform one of the following.
	 * <ol>
	 * <li>Rethrow the supplied {@code throwable} <em>as is</em> which is the default implementation</li>
	 * <li>Swallow the supplied {@code throwable}, thereby preventing propagation.</li>
	 * <li>Throw a new exception, potentially wrapping the supplied {@code throwable}.</li>
	 * </ol>
	 *
	 * <p>If the supplied {@code throwable} is swallowed, subsequent
	 * {@code LifecycleMethodExecutionExceptionHandler}
	 * will not be invoked; otherwise, the next registered
	 * {@code LifecycleMethodExecutionExceptionHandler} (if there is one)
	 * will be invoked with any {@link Throwable} thrown by this handler.
	 *
	 * @param context the current extension context; never {@code null}
	 * @param throwable the {@code Throwable} to handle; never {@code null}
	 */
	default void handleBeforeEachMethodExecutionException(ExtensionContext context, Throwable throwable)
			throws Throwable {
		throw throwable;
	}

	/**
	 * Handle the supplied {@link Throwable throwable}.
	 *
	 * <p>Implementors must perform one of the following.
	 * <ol>
	 * <li>Rethrow the supplied {@code throwable} <em>as is</em> which is the default implementation</li>
	 * <li>Swallow the supplied {@code throwable}, thereby preventing propagation.</li>
	 * <li>Throw a new exception, potentially wrapping the supplied {@code throwable}.</li>
	 * </ol>
	 *
	 * <p>If the supplied {@code throwable} is swallowed, subsequent
	 * {@code LifecycleMethodExecutionExceptionHandler} will not be invoked;
	 * otherwise, the next registered {@code LifecycleMethodExecutionExceptionHandler}
	 * (if there is one) will be invoked with any {@link Throwable} thrown by
	 * this handler.
	 *
	 * @param context the current extension context; never {@code null}
	 * @param throwable the {@code Throwable} to handle; never {@code null}
	 */
	default void handleAfterEachMethodExecutionException(ExtensionContext context, Throwable throwable)
			throws Throwable {
		throw throwable;
	}

	/**
	 * Handle the supplied {@link Throwable throwable}.
	 *
	 * <p>Implementors must perform one of the following.
	 * <ol>
	 * <li>Rethrow the supplied {@code throwable} <em>as is</em> which is the default implementation</li>
	 * <li>Swallow the supplied {@code throwable}, thereby preventing propagation.</li>
	 * <li>Throw a new exception, potentially wrapping the supplied {@code throwable}.</li>
	 * </ol>
	 *
	 * <p>If the supplied {@code throwable} is swallowed, subsequent
	 * {@code LifecycleMethodExecutionExceptionHandler} will not be invoked; otherwise,
	 * the next registered {@code LifecycleMethodExecutionExceptionHandler} (if there
	 * is one) will be invoked with any {@link Throwable} thrown by this handler.
	 *
	 * @param context the current extension context; never {@code null}
	 * @param throwable the {@code Throwable} to handle; never {@code null}
	 */
	default void handleAfterAllMethodExecutionException(ExtensionContext context, Throwable throwable)
			throws Throwable {
		throw throwable;
	}
}
