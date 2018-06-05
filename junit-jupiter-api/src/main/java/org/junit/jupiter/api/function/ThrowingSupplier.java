/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.function;

import static org.apiguardian.api.API.Status.STABLE;

import org.apiguardian.api.API;

/**
 * {@code ThrowingSupplier} is a functional interface that can be used to
 * implement any generic block of code that returns an object and
 * potentially throws a {@link Throwable}.
 *
 * <p>The {@code ThrowingSupplier} interface is similar to
 * {@link java.util.function.Supplier}, except that a {@code ThrowingSupplier}
 * can throw any kind of exception, including checked exceptions.
 *
 * <p>As of JUnit Jupiter 5.3, {@code ThrowingSupplier} extends
 * {@link Executable}, providing a <em>default</em> implementation of
 * {@link #execute()} that delegates to {@link #get()} and ignores the return
 * value. This allows the Java compiler to disambiguate between
 * {@code ThrowingSupplier} and {@code Executable} when performing type
 * inference for a lambda expression or method reference supplied to
 * an overloaded method that accepts either a {@code ThrowingSupplier} or an
 * {@code Executable}.
 *
 * <h4>Rationale for throwing {@code Throwable} instead of {@code Exception}</h4>
 *
 * <p>Although Java applications typically throw exceptions that are instances
 * of {@link Exception}, {@link RuntimeException},
 * {@link Error}, or {@link AssertionError} (in testing
 * scenarios), there may be use cases where a {@code ThrowingSupplier} needs to
 * explicitly throw a {@code Throwable}. In order to support such specialized
 * use cases, {@link #get} is declared to throw {@code Throwable}.
 *
 * @since 5.0
 * @param <T> the type of argument supplied
 * @see java.util.function.Supplier
 * @see org.junit.jupiter.api.Assertions#assertTimeout(java.time.Duration, ThrowingSupplier)
 * @see org.junit.jupiter.api.Assertions#assertTimeoutPreemptively(java.time.Duration, ThrowingSupplier)
 * @see Executable
 * @see ThrowingConsumer
 */
@FunctionalInterface
@API(status = STABLE, since = "5.0")
public interface ThrowingSupplier<T> extends Executable {

	/**
	 * Delegates to {@link #get()} and ignores the return value.
	 *
	 * <p>This default method is not intended to be overridden. See
	 * {@linkplain ThrowingSupplier class-level documentation} for further
	 * details.
	 *
	 * @since 5.3
	 * @see #get()
	 */
	@Override
	@API(status = STABLE, since = "5.3")
	default void execute() throws Throwable {
		get();
	}

	/**
	 * Get a result, potentially throwing an exception.
	 *
	 * @return a result
	 */
	T get() throws Throwable;

}
