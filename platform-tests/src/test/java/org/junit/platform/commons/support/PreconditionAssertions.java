/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.function.Executable;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * @since 1.4
 */
class PreconditionAssertions {

	static void assertPreconditionViolationException(String name, Executable executable) {
		var exception = assertThrows(PreconditionViolationException.class, executable);
		assertEquals(name + " must not be null", exception.getMessage());
	}

	static void assertPreconditionViolationExceptionForString(String name, Executable executable) {
		var exception = assertThrows(PreconditionViolationException.class, executable);
		assertEquals(name + " must not be null or blank", exception.getMessage());
	}

}
