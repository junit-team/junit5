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

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.meta.API;

/**
 * Thrown if an error is encountered regarding the use of an
 * {@link ExtensionContext} or {@link Store}.
 *
 * @since 5.0
 */
@API(Experimental)
public class ExtensionContextException extends JUnitException {

	private static final long serialVersionUID = 1L;

	public ExtensionContextException(String message) {
		super(message);
	}

}
