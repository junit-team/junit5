/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.execution;

import org.junit.platform.commons.JUnitException;

public class ExtensionValuesStoreException extends JUnitException {

	private static final long serialVersionUID = 1L;

	public ExtensionValuesStoreException(String message) {
		super(message);
	}

}
