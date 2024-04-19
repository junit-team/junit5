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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

// @formatter:off
// tag::user_guide[]

class ExceptionAssertionDemo {

    @Test
    void testExpectedExceptionIsThrown() {
        // The following assertion succeeds because the code under assertion throws
        // the expected IllegalArgumentException
        // The assertion also returns the thrown exception which can be used for
        // further assertions like asserting the exception messages
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            throw new IllegalArgumentException("Expected exception");
        });
        assertEquals(exception.getMessage(), "Expected exception");

        // The following assertion should also succeed because the code under assertion throws
        // IllegalArgumentException which is subclass of RuntimeException
        assertThrows(RuntimeException.class, () -> {
            throw new IllegalArgumentException("Expected exception");
        });
    }
}

// end::user_guide[]
// @formatter:on
