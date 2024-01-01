/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.support.conversion;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;
import org.junit.platform.commons.JUnitException;

/**
 * {@code ConversionException} is an exception that can occur when an
 * object is converted to another object.
 *
 * @since 1.11
 */
@API(status = EXPERIMENTAL, since = "1.11")
public class ConversionException extends JUnitException {

	private static final long serialVersionUID = 1L;

	public ConversionException(String message) {
		super(message);
	}

	public ConversionException(String message, Throwable cause) {
		super(message, cause);
	}

}
