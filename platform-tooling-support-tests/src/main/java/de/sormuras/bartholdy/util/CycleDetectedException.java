/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package de.sormuras.bartholdy.util;

public class CycleDetectedException extends IllegalArgumentException {

	@java.io.Serial
	private static final long serialVersionUID = -6238089467068688420L;

	CycleDetectedException(String message) {
		super("Cycle detected: " + message);
	}
}
