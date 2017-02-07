/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api.function;

import static org.junit.platform.commons.meta.API.Usage.Stable;

import java.time.Duration;

import org.junit.platform.commons.meta.API;

/**
 * {@code ThrowingSupplier} is a functional interface that can be used to
 * implement any generic block of code that returns an object and
 * potentially throws a {@link Throwable}.
 *
 * <p>The {@code ThrowingSupplier} interface is similar to
 * {@link java.util.function.Supplier}, except that a {@code ThrowingSupplier}
 * can throw any kind of exception, including checked exceptions.
 *
 * <p><h4>Rationale for throwing {@code Throwable} instead of {@code Exception}</h4>
 *
 * <p>Although Java applications typically throw exceptions that are instances
 * of {@link Exception}, {@link RuntimeException},
 * {@link Error}, or {@link AssertionError} (in testing
 * scenarios), there may be use cases where a {@code ThrowingSupplier} needs to
 * explicitly throw a {@code Throwable}. In order to support such specialized
 * use cases, {@link #get} is declared to throw {@code Throwable}.
 *
 * @see java.util.function.Supplier
 * @see org.junit.jupiter.api.DynamicTest#stream
 * @see org.junit.jupiter.api.Assertions#assertTimeout(Duration, ThrowingSupplier)
 * @see Executable
 * @since 5.0
 */
@FunctionalInterface
@API(Stable)
public interface ThrowingSupplier<T> {

	/**
	 * Gets a result.
	 *
	 * @return a result
	 */
	T get() throws Throwable;

}
