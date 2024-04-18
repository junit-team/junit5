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

import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

// @formatter:off
// tag::user_guide[]

class ExceptionAssertionDemoTestCase {
    @Test
    void testExpectedExceptionIsThrown() {
        assertThrows(IllegalArgumentException.class, () -> {
            throw new IllegalArgumentException("Expected exception");
        }, "Expected IllegalArgumentException to be thrown");

        assertThrows(RuntimeException.class, () -> {
            throw new IllegalArgumentException("Expected exception");
        }, "should also pass because IllegalArgumentException is subclass of RuntimeException");
    }
}

// end::user_guide[]
// @formatter:on
