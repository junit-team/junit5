/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.jupiter.api.Nested;

/**
 * {@code TestInstanceFactoryContext} encapsulates the <em>context</em> in which
 * a {@link #getTestClass test class} is to be instantiated by a
 * {@link TestInstanceFactory}.
 *
 * @since 5.3
 * @see TestInstanceFactory
 */
@API(status = EXPERIMENTAL, since = "5.3")
public interface TestInstanceFactoryContext {

	/**
	 * Get the test class for this context.
	 *
	 * @return the test class to be instantiated; never {@code null}
	 */
	Class<?> getTestClass();

	/**
	 * Get the outer class instance for {@link Nested nested} test classes. For
	 * top level classes there is not outer instance and as such the value will
	 * be {@link Optional#empty empty}.
	 *
	 * @return the outer test class instance, if test class is nested
	 * @see org.junit.jupiter.api.Nested
	 */
	Optional<Object> getOuterInstance();

}
