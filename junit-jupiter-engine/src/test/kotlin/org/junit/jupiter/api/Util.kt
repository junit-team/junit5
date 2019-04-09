/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */
package org.junit.jupiter.api

/**
 * Purely serves as a provider of fail assertions that have a suspend modifier.
 *
 * Is used to verify assertThrows() can handle suspending functions.
 *
 * @see KotlinFailAssertionsTests
 * @see KotlinAssertionsTests
 */
object Util {
    suspend fun failSuspend(message: (() -> String)?): Nothing = Assertions.fail<Nothing>(message)
    suspend fun failSuspend(message: String?, throwable: Throwable? = null): Nothing = Assertions.fail<Nothing>(message, throwable)
    suspend fun failSuspend(throwable: Throwable?): Nothing = Assertions.fail<Nothing>(throwable)
}
