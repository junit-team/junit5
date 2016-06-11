/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Optional;

import org.junit.gen5.commons.meta.API;

/**
 * A parameter and its context to support parameter resolution.
 *
 * @since 5.0
 * @see ParameterResolver
 * @see java.lang.reflect.Parameter
 * @see java.lang.reflect.Executable
 * @see java.lang.reflect.Method
 * @see java.lang.reflect.Constructor
 */
@API(Experimental)
public interface ParameterContext {

	/**
	 * Get the parameter of this context.
	 */
	Parameter getParameter();

	/**
	 * Get the {@code Executable} that declares the parameter of this context.
	 *
	 * @see Parameter#getDeclaringExecutable()
	 */
	default Executable getDeclaringExecutable() {
		return getParameter().getDeclaringExecutable();
	}

	/**
	 * Get an {@link Optional} containing the target on which the
	 * {@link Executable} that declares the parameter of this context will be
	 * invoked; never {@code null} but will be <em>empty</em> if the
	 * {@link Executable} is a constructor or {@code static} method.
	 */
	Optional<Object> getTarget();

}
