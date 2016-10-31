/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.launcher;

public class Failure {

    private final TestIdentifier testIdentifier;
    private final Throwable exception;

    public Failure(TestIdentifier testIdentifier, Throwable exception) {
        this.testIdentifier = testIdentifier;
        this.exception = exception;
    }

    public TestIdentifier getTestIdentifier() {
        return testIdentifier;
    }

    public Throwable getException() {
        return exception;
    }
}
