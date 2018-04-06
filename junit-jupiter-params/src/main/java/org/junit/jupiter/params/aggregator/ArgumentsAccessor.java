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
 * {@code ArgumentsAccessor} is an interface for accessing and converting arguments
 * passed to a {@link org.junit.jupiter.params.ParameterizedTest @ParameterizedTest}
 * annotated method.
 *
 * <p>An {@code ArgumentsAccessor} implementation is injected into any parameter of
 * an assignable type, and supplied to {@link ArgumentsAggregator} implementations.
 *
 * @since 5.2
 * @see ArgumentsAggregator
 * @see DefaultArgumentsAccessor
 * @see org.junit.jupiter.params.ParameterizedTest
 */
@API(status = EXPERIMENTAL, since = "5.2")
public interface ArgumentsAccessor {

	Object[] toArray();

	List<Object> toList();

	int size();

	Object get(int index);

	<T> T get(int index, Class<T> requiredType);

	Character getCharacter(int index);

	Boolean getBoolean(int index);

	Byte getByte(int index);

	Short getShort(int index);

	Integer getInteger(int index);

	Long getLong(int index);

	Float getFloat(int index);

	Double getDouble(int index);

	String getString(int index);
}
