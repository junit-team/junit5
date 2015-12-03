/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

/**
 * Thrown if an error is encountered in the configuration or execution of a
 * {@link MethodParameterResolver}.
 *
 * @since 5.0
 * @see MethodParameterResolver
 */
public class ParameterResolutionException extends RuntimeException {

	private static final long serialVersionUID = 5137237798019406636L;

	public ParameterResolutionException(String message) {
		super(message);
	}

	public ParameterResolutionException(String message, Throwable cause) {
		super(message, cause);
	}

}
