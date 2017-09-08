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

import static org.junit.platform.commons.meta.API.Status.STABLE;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.meta.API;

/**
 * Thrown if an error is encountered in the configuration or execution of a
 * {@link ParameterResolver}.
 *
 * @since 5.0
 * @see ParameterResolver
 */
@API(status = STABLE, since = "5.0")
public class ParameterResolutionException extends JUnitException {

	private static final long serialVersionUID = 1L;

	public ParameterResolutionException(String message) {
		super(message);
	}

	public ParameterResolutionException(String message, Throwable cause) {
		super(message, cause);
	}

}
