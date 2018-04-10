/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.aggregator;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.List;

import org.apiguardian.api.API;

/**
 * {@code ArgumentsAccessor} defines the public API for accessing arguments passed
 * to a {@link org.junit.jupiter.params.ParameterizedTest @ParameterizedTest} method.
 *
 * <p>Specifically, an {@code ArgumentsAccessor} <em>aggregates</em> a set of
 * arguments for a given invocation of a parameterized test and provides convenience
 * methods for accessing those arguments in a type-safe manner with support for
 * automatic type conversion.
 *
 * <p>An instance of {@code ArgumentsAccessor} will be automatically supplied
 * for any parameter of type {@code ArgumentsAccessor} in a parameterized test.
 * In addition, {@link ArgumentsAggregator} implementations are given access to
 * an {@code ArgumentsAccessor}.
 *
 * @since 5.2
 * @see ArgumentsAggregator
 * @see org.junit.jupiter.params.ParameterizedTest
 */
@API(status = EXPERIMENTAL, since = "5.2")
public interface ArgumentsAccessor {

	/**
	 * Get the value of the argument at the given index as an {@link Object}.
	 *
	 * @param index the index of the argument to get; must be greater than or
	 * equal to zero and less than {@link #size}
	 * @return the value at the given index, potentially {@code null}
	 */
	Object get(int index) throws ArgumentsAccessorException;

	/**
	 * Get the value of the argument at the given index as an instance of the
	 * required type.
	 *
	 * @param index the index of the argument to get; must be greater than or
	 * equal to zero and less than {@link #size}
	 * @param requiredType the required type of the value; never {@code null}
	 * @return the value at the given index, potentially {@code null}
	 */
	<T> T get(int index, Class<T> requiredType) throws ArgumentsAccessorException;

	/**
	 * Get the value of the argument at the given index as a {@link Character},
	 * performing automatic type conversion as necessary.
	 *
	 * @param index the index of the argument to get; must be greater than or
	 * equal to zero and less than {@link #size}
	 * @return the value at the given index, potentially {@code null}
	 * @throws ArgumentsAccessorException if the value cannot be accessed
	 * or converted to the desired type
	 */
	Character getCharacter(int index) throws ArgumentsAccessorException;

	/**
	 * Get the value of the argument at the given index as a {@link Boolean},
	 * performing automatic type conversion as necessary.
	 *
	 * @param index the index of the argument to get; must be greater than or
	 * equal to zero and less than {@link #size}
	 * @return the value at the given index, potentially {@code null}
	 * @throws ArgumentsAccessorException if the value cannot be accessed
	 * or converted to the desired type
	 */
	Boolean getBoolean(int index) throws ArgumentsAccessorException;

	/**
	 * Get the value of the argument at the given index as a {@link Byte},
	 * performing automatic type conversion as necessary.
	 *
	 * @param index the index of the argument to get; must be greater than or
	 * equal to zero and less than {@link #size}
	 * @return the value at the given index, potentially {@code null}
	 * @throws ArgumentsAccessorException if the value cannot be accessed
	 * or converted to the desired type
	 */
	Byte getByte(int index) throws ArgumentsAccessorException;

	/**
	 * Get the value of the argument at the given index as a {@link Short},
	 * performing automatic type conversion as necessary.
	 *
	 * @param index the index of the argument to get; must be greater than or
	 * equal to zero and less than {@link #size}
	 * @return the value at the given index, potentially {@code null}
	 * @throws ArgumentsAccessorException if the value cannot be accessed
	 * or converted to the desired type
	 */
	Short getShort(int index) throws ArgumentsAccessorException;

	/**
	 * Get the value of the argument at the given index as a {@link Integer},
	 * performing automatic type conversion as necessary.
	 *
	 * @param index the index of the argument to get; must be greater than or
	 * equal to zero and less than {@link #size}
	 * @return the value at the given index, potentially {@code null}
	 * @throws ArgumentsAccessorException if the value cannot be accessed
	 * or converted to the desired type
	 */
	Integer getInteger(int index) throws ArgumentsAccessorException;

	/**
	 * Get the value of the argument at the given index as a {@link Long},
	 * performing automatic type conversion as necessary.
	 *
	 * @param index the index of the argument to get; must be greater than or
	 * equal to zero and less than {@link #size}
	 * @return the value at the given index, potentially {@code null}
	 * @throws ArgumentsAccessorException if the value cannot be accessed
	 * or converted to the desired type
	 */
	Long getLong(int index) throws ArgumentsAccessorException;

	/**
	 * Get the value of the argument at the given index as a {@link Float},
	 * performing automatic type conversion as necessary.
	 *
	 * @param index the index of the argument to get; must be greater than or
	 * equal to zero and less than {@link #size}
	 * @return the value at the given index, potentially {@code null}
	 * @throws ArgumentsAccessorException if the value cannot be accessed
	 * or converted to the desired type
	 */
	Float getFloat(int index) throws ArgumentsAccessorException;

	/**
	 * Get the value of the argument at the given index as a {@link Double},
	 * performing automatic type conversion as necessary.
	 *
	 * @param index the index of the argument to get; must be greater than or
	 * equal to zero and less than {@link #size}
	 * @return the value at the given index, potentially {@code null}
	 * @throws ArgumentsAccessorException if the value cannot be accessed
	 * or converted to the desired type
	 */
	Double getDouble(int index) throws ArgumentsAccessorException;

	/**
	 * Get the value of the argument at the given index as a {@link String},
	 * performing automatic type conversion as necessary.
	 *
	 * @param index the index of the argument to get; must be greater than or
	 * equal to zero and less than {@link #size}
	 * @return the value at the given index, potentially {@code null}
	 * @throws ArgumentsAccessorException if the value cannot be accessed
	 * or converted to the desired type
	 */
	String getString(int index) throws ArgumentsAccessorException;

	/**
	 * Get the number of arguments in this accessor.
	 */
	int size();

	/**
	 * Get all arguments in this accessor as an array.
	 */
	Object[] toArray();

	/**
	 * Get all arguments in this accessor as a list.
	 */
	List<Object> toList();

}
