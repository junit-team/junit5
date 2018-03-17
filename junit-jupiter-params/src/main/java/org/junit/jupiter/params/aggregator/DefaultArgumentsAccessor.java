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

import org.junit.jupiter.params.converter.DefaultArgumentConverter;
import org.junit.platform.commons.util.Preconditions;

public class DefaultArgumentsAccessor implements ArgumentsAccessor {

	private final Object[] arguments;

	public DefaultArgumentsAccessor(Object[] arguments) {
		Preconditions.notNull(arguments, "DefaultArgumentsAccessor initialized with null arguments");
		this.arguments = arguments;
	}

	public Object get(int index) {
		Preconditions.condition(index >= 0 && index < arguments.length,
			String.format("Index must be between 0 and %d", arguments.length));
		return arguments[index];
	}

	public Object[] toArray() {
		return arguments;
	}

	public List<Object> toList() {
		return Arrays.asList(arguments);
	}

	public Character getChar(int index) {
		return (Character) getAs(Character.class, index);
	}

	public Boolean getBoolean(int index) {
		return (Boolean) getAs(Boolean.class, index);
	}

	public Byte getByte(int index) {
		return (Byte) getAs(Byte.class, index);
	}

	public Short getShort(int index) {
		return (Short) getAs(Short.class, index);
	}

	public Integer getInt(int index) {
		return (Integer) getAs(Integer.class, index);
	}

	public Long getLong(int index) {
		return (Long) getAs(Long.class, index);
	}

	public Float getFloat(int index) {
		return (Float) getAs(Float.class, index);
	}

	public Double getDouble(int index) {
		return (Double) getAs(Double.class, index);
	}

	public String getString(int index) {
		return (String) getAs(String.class, index);
	}

	public Object getAs(Class<?> clazz, int index) {
		Preconditions.condition(index >= 0 && index < arguments.length,
			String.format("Index must be between 0 and %d", arguments.length));
		return DefaultArgumentConverter.INSTANCE.convert(arguments[index], clazz);
	}

	public int getSize() {
		return arguments.length;
	}

}
