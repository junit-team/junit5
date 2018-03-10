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

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.converter.DefaultArgumentConverter;
import org.junit.platform.commons.util.Preconditions;

/**
 * The default implementation of {@link ArgumentsAccessor}.
 *
 * Delegates conversion to {@link DefaultArgumentConverter}.
 *
 * @since 5.2
 * @see ArgumentsAccessor
 * @see ParameterizedTest
 * @see DefaultArgumentConverter
 */
public class DefaultArgumentsAccessor implements ArgumentsAccessor {

	private final Object[] arguments;

	public DefaultArgumentsAccessor(Object[] arguments) {
		Preconditions.notNull(arguments, "Arguments array must not be null");
		this.arguments = arguments;
	}

	public Object get(int index) {
		Preconditions.condition(index >= 0 && index < arguments.length,
			String.format("Index must be between 0 and %d", arguments.length));
		return arguments[index];
	}

	public Object[] toArray() {
		return Arrays.copyOf(arguments, arguments.length);
	}

	public List<Object> toList() {
		return Arrays.asList(arguments);
	}

	public Character getCharacter(int index) {
		return get(index, Character.class);
	}

	public Boolean getBoolean(int index) {
		return get(index, Boolean.class);
	}

	public Byte getByte(int index) {
		return get(index, Byte.class);
	}

	public Short getShort(int index) {
		return get(index, Short.class);
	}

	public Integer getInteger(int index) {
		return get(index, Integer.class);
	}

	public Long getLong(int index) {
		return get(index, Long.class);
	}

	public Float getFloat(int index) {
		return get(index, Float.class);
	}

	public Double getDouble(int index) {
		return get(index, Double.class);
	}

	public String getString(int index) {
		return get(index, String.class);
	}

	public <T> T get(int index, Class<T> requiredType) {
		try {
			return requiredType.cast(DefaultArgumentConverter.INSTANCE.convert(get(index), requiredType));
		}
		catch (Exception ex) {
			throw new ArgumentsAccessorException(
				String.format("Argument in index %d of class %s could not be converted to %s", index,
					get(index).getClass().getCanonicalName(), requiredType.getCanonicalName()),
				ex);
		}
	}

	public int size() {
		return arguments.length;
	}

}
