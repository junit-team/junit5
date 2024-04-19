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

import org.junit.jupiter.api.Test;

// @formatter:off
// tag::user_guide[]

class UncaughtExceptionHandlingDemo {

    // end::user_guide[]
    @extensions.ExpectToFail
    // tag::user_guide[]
    @Test
    void failsDueToUncaughtException() {
        // the following thrown exception will cause a test failure
        throw new RuntimeException();
    }
}

// end::user_guide[]
// @formatter:on
