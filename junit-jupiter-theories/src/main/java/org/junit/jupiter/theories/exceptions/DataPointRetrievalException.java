/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.theories.exceptions;

import static org.apiguardian.api.API.Status.INTERNAL;

import org.apiguardian.api.API;

/**
 * Exception that is thrown if there are any errors while retrieving a data point.
 */
@API(status = INTERNAL, since = "5.3")
public class DataPointRetrievalException extends RuntimeException {

	private static final long serialVersionUID = -2002844107705394342L;

	/**
	 * Constructor.
	 *
	 * @param message the exception message
	 */
	public DataPointRetrievalException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 *
	 * @param message the exception message
	 * @param cause the cause of this exception
	 */
	public DataPointRetrievalException(String message, Throwable cause) {
		super(message, cause);
	}
}
