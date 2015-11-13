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
 * @since 5.0
 */
public class ArgumentResolutionException extends RuntimeException {

	private static final long serialVersionUID = 5137237798019406636L;

	public ArgumentResolutionException(String message) {
		super(message);
	}

	public ArgumentResolutionException(String message, Throwable cause) {
		super(message, cause);
	}

	public ArgumentResolutionException(Throwable cause) {
		super(cause);
	}

}
