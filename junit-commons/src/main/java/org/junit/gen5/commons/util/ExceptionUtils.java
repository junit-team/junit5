/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.commons.util;

/**
 * Collection of utilities for working with exceptions.
 *
 * @since 5.0
 */
public final class ExceptionUtils {

	private ExceptionUtils() {
		/* no-op */
	}

	/**
	 * Throw the supplied {@link Throwable}, <em>masked</em> as a
	 * {@link RuntimeException}.
	 *
	 * <p>The supplied {@code Throwable} will not be wrapped. Rather, it
	 * will be thrown as-is using a hack based on generics and type erasure
	 * that tricks the Java compiler into believing that the thrown exception
	 * is an unchecked exception.
	 *
	 * @param t the Throwable to throw as a {@code RuntimeException}
	 * @return this method always throws an exception and therefore never
	 * returns anything; the return type is merely present to allow this
	 * method to be supplied as the operand in a {@code throw} statement
	 */
	public static RuntimeException throwAsRuntimeException(Throwable t) {
		ExceptionUtils.<RuntimeException> throwAs(t);

		// Appeasing the compiler: the following line will never be executed.
		return null;
	}

	@SuppressWarnings("unchecked")
	private static <T extends Throwable> void throwAs(Throwable t) throws T {
		throw (T) t;
	}

}
