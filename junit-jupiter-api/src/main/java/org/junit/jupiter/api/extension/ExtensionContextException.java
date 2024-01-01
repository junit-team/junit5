/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.STABLE;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.platform.commons.JUnitException;

/**
 * Thrown if an error is encountered regarding the use of an
 * {@link ExtensionContext} or {@link Store}.
 *
 * @since 5.0
 */
@API(status = STABLE, since = "5.0")
public class ExtensionContextException extends JUnitException {

	private static final long serialVersionUID = 1L;

	@SuppressWarnings("unused")
	public ExtensionContextException(String message) {
		super(message);
	}

	@API(status = EXPERIMENTAL, since = "5.10")
	public ExtensionContextException(String message, Throwable cause) {
		super(message, cause);
	}

}
