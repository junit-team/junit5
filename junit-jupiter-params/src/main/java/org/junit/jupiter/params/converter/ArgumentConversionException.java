/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.converter;

import org.junit.platform.commons.JUnitException;

/**
 * @since 5.0
 */
public class ArgumentConversionException extends JUnitException {

	private static final long serialVersionUID = 1L;

	public ArgumentConversionException(String message) {
		super(message);
	}

	public ArgumentConversionException(String message, Throwable cause) {
		super(message, cause);
	}

}
