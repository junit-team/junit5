/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api;

/**
 * {@code Executable} is a functional interface that can be used to
 * implement any generic block of code that potentially throws a
 * {@link Throwable}.
 *
 * <p>The {@code Executable} interface is similar to {@link java.lang.Runnable},
 * except that an {@code Executable} can throw any kind of exception.
 *
 * @since 5.0
 * @see Assertions#assertAll(Executable...)
 * @see Assertions#assertAll(String, Executable...)
 * @see Assertions#assertThrows(Class, Executable)
 * @see Assumptions#assumingThat(java.util.function.BooleanSupplier, Executable)
 */
@FunctionalInterface
public interface Executable {

	void execute() throws Throwable;

}
