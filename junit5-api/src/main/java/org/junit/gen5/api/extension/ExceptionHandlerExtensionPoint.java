/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

/**
 * {@code ExceptionHandlerExtensionPoint} defines the API for {@link Extension Extensions}
 * that wish to <em>react to thrown exceptions</em> in tests.
 *
 * <p>Common use cases include swallowing an exception if it's anticipated
 * or rolling back a transaction in certain error scenarios.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @since 5.0
 */
@FunctionalInterface
public interface ExceptionHandlerExtensionPoint extends ExtensionPoint {

	/**
	 * React to a {@link Throwable throwable} which has been thrown by a test method.
	 *
	 * <p>Implementors have to decide if they
	 * <ul>
	 *     <li>Rethrow the incoming {@code throwable}</li>
	 *     <li>Throw a newly constructed {@link Exception} or {@link Throwable}</li>
	 *     <li>Swallow the incoming {@link Throwable throwable}</li>
	 * </ul>
	 *
	 * <p>If the incoming {@code throwable} is swallowed, other registered
	 * {@link ExceptionHandlerExtensionPoint exception handlers}
	 * with later {@link org.junit.gen5.api.extension.ExtensionPointRegistry.Position Position}
	 *  will not be called. Otherwise, the next {@link ExceptionHandlerExtensionPoint exception handler}
	 *  will be called with the freshly thrown {@link Throwable}.
	 *
	 */
	void handleException(TestExtensionContext context, Throwable throwable) throws Throwable;
}
