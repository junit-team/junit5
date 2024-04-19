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

    // end::user_guide[]
    @extensions.ExpectToFail
    // tag::user_guide[]
    @Test
    void testExpectedExceptionIsThrown() {
        // The following assertion succeeds because the code under assertion throws
        // IllegalArgumentException which is exactly equal to the expected type
        // The assertion also returns the thrown exception which can be used for
        // further assertions like asserting the exception messages
        IllegalArgumentException exception = assertThrowsExactly(IllegalArgumentException.class, () -> {
            throw new IllegalArgumentException("Expected exception");
        });
        assertEquals(exception.getMessage(), "Expected exception");

        // The following assertion should fail because the assertion expected exactly
        // RuntimeException to be thrown, not subclasses
        assertThrowsExactly(RuntimeException.class, () -> {
            throw new IllegalArgumentException("Expected exception");
        });
    }
}

// end::user_guide[]
// @formatter:on
