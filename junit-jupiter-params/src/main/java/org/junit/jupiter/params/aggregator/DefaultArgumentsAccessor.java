/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.aggregator;

import static java.lang.String.format;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.converter.DefaultArgumentConverter;
import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.commons.util.Preconditions;

/**
 * Default implementation of the {@link ArgumentsAccessor} API.
 *
 * <p>Delegates conversion to {@link DefaultArgumentConverter}.
 *
 * @since 5.2
 * @see ArgumentsAccessor
 * @see DefaultArgumentConverter
 * @see org.junit.jupiter.params.ParameterizedTest
 */
@API(status = INTERNAL, since = "5.2")
public class DefaultArgumentsAccessor implements ArgumentsAccessor {

	private final ParameterContext parameterContext;
	private final int invocationIndex;
	private final Object[] arguments;

	public DefaultArgumentsAccessor(ParameterContext parameterContext, int invocationIndex, Object... arguments) {
		Preconditions.notNull(parameterContext, "ParameterContext must not be null");
		Preconditions.condition(invocationIndex >= 1, () -> "invocation index must be >= 1");
		Preconditions.notNull(arguments, "Arguments array must not be null");
		this.parameterContext = parameterContext;
		this.invocationIndex = invocationIndex;
		this.arguments = arguments;
	}

	@Override
	public Object get(int index) {
		Preconditions.condition(index >= 0 && index < this.arguments.length,
			() -> format("index must be >= 0 and < %d", this.arguments.length));
		return this.arguments[index];
	}

	@Override
	public <T> T get(int index, Class<T> requiredType) {
		Preconditions.notNull(requiredType, "requiredType must not be null");
		Object value = get(index);
		try {
			Object convertedValue = DefaultArgumentConverter.INSTANCE.convert(value, requiredType,
				this.parameterContext);
			return requiredType.cast(convertedValue);
		}
		catch (Exception ex) {
			String message = format(
				"Argument at index [%d] with value [%s] and type [%s] could not be converted or cast to type [%s].",
				index, value, ClassUtils.nullSafeToString(value == null ? null : value.getClass()),
				requiredType.getName());
			throw new ArgumentAccessException(message, ex);
		}
	}

	@Override
	public Character getCharacter(int index) {
		return get(index, Character.class);
	}

	@Override
	public Boolean getBoolean(int index) {
		return get(index, Boolean.class);
	}

	@Override
	public Byte getByte(int index) {
		return get(index, Byte.class);
	}

	@Override
	public Short getShort(int index) {
		return get(index, Short.class);
	}

	@Override
	public Integer getInteger(int index) {
		return get(index, Integer.class);
	}

	@Override
	public Long getLong(int index) {
		return get(index, Long.class);
	}

	@Override
	public Float getFloat(int index) {
		return get(index, Float.class);
	}

	@Override
	public Double getDouble(int index) {
		return get(index, Double.class);
	}

	@Override
	public String getString(int index) {
		return get(index, String.class);
	}

	@Override
	public int size() {
		return this.arguments.length;
	}

	@Override
	public Object[] toArray() {
		return Arrays.copyOf(this.arguments, this.arguments.length);
	}

	@Override
	public List<Object> toList() {
		return Collections.unmodifiableList(Arrays.asList(this.arguments));
	}

	@Override
	public int getInvocationIndex() {
		return this.invocationIndex;
	}

}
