/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example.exception;

import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import org.junit.jupiter.api.Test;

// @formatter:off
// tag::user_guide[]

public class ExceptionAssertionExactDemo {
    @Test
    void testExpectedExceptionIsThrown() {
        assertThrowsExactly(IllegalArgumentException.class, () -> {
            throw new IllegalArgumentException("Expected exception");
        }, "This will fail because because we expect IllegalArgumentException to be thrown");

        assertThrowsExactly(RuntimeException.class, () -> {
            throw new IllegalArgumentException("Expected exception");
        }, "This will fail because expected exactly RuntimeException to be thrown, not subclasses");
    }
}

// end::user_guide[]
// @formatter:on
