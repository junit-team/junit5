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

import static org.apiguardian.api.API.Status.STABLE;

import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.Optional;

import org.apiguardian.api.API;

/**
 * {@code ParameterContext} encapsulates the <em>context</em> in which an
 * {@link #getDeclaringExecutable Executable} will be invoked for a given
 * {@link #getParameter Parameter}.
 *
 * <p>A {@code ParameterContext} is used to support parameter resolution via
 * a {@link ParameterResolver}.
 *
 * @since 5.0
 * @see ParameterResolver
 * @see java.lang.reflect.Parameter
 * @see java.lang.reflect.Executable
 * @see java.lang.reflect.Method
 * @see java.lang.reflect.Constructor
 */
@API(status = STABLE, since = "5.0")
public interface ParameterContext {

	/**
	 * Get the {@link Parameter} for this context.
	 *
	 * @return the parameter; never {@code null}
	 * @see #getIndex()
	 */
	Parameter getParameter();

	/**
	 * Get the index of the {@link Parameter} for this context within the
	 * parameter list of the {@link #getDeclaringExecutable Executable} that
	 * declares the parameter.
	 *
	 * @return the index of the parameter
	 * @see #getParameter()
	 * @see Executable#getParameters()
	 */
	int getIndex();

	/**
	 * Get the {@link Executable} (i.e., the {@link java.lang.reflect.Method} or
	 * {@link java.lang.reflect.Constructor}) that declares the {@code Parameter}
	 * for this context.
	 *
	 * @return the declaring {@code Executable}; never {@code null}
	 * @see Parameter#getDeclaringExecutable()
	 */
	default Executable getDeclaringExecutable() {
		return getParameter().getDeclaringExecutable();
	}

	/**
	 * Get the target on which the {@link #getDeclaringExecutable Executable}
	 * that declares the {@link #getParameter Parameter} for this context will
	 * be invoked, if available.
	 *
	 * @return an {@link Optional} containing the target on which the
	 * {@code Executable} will be invoked; never {@code null} but will be
	 * <em>empty</em> if the {@code Executable} is a constructor or a
	 * {@code static} method.
	 */
	Optional<Object> getTarget();

}
