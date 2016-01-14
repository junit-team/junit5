/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.commons;

/**
 * Base class for all {@link RuntimeException RuntimeExceptions} thrown
 * by JUnit.
 *
 * @since 5.0
 */
public class JUnitException extends RuntimeException {

	private static final long serialVersionUID = -1751494513161886551L;

	public JUnitException(String message) {
		super(message);
	}

	public JUnitException(String message, Throwable cause) {
		super(message, cause);
	}

}
