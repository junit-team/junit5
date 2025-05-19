/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.aggregator;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtensionContext;
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

	private final int invocationIndex;
	private final Object[] arguments;
	private final BiFunction<Object, Class<?>, Object> converter;

	public static DefaultArgumentsAccessor create(ExtensionContext context, int invocationIndex,
			ClassLoader classLoader, Object[] arguments) {
		Preconditions.notNull(classLoader, "ClassLoader must not be null");

		BiFunction<Object, Class<?>, Object> converter = (source, targetType) -> new DefaultArgumentConverter(context) //
				.convert(source, targetType, classLoader);
		return new DefaultArgumentsAccessor(converter, invocationIndex, arguments);
	}

	private DefaultArgumentsAccessor(BiFunction<Object, Class<?>, Object> converter, int invocationIndex,
			Object... arguments) {
		Preconditions.condition(invocationIndex >= 1, () -> "Invocation index must be >= 1");
		this.converter = Preconditions.notNull(converter, "Converter must not be null");
		this.invocationIndex = invocationIndex;
		this.arguments = Preconditions.notNull(arguments, "Arguments array must not be null");
	}

	@Override
	public Object get(int index) {
		Preconditions.condition(index >= 0 && index < this.arguments.length,
			() -> "index must be >= 0 and < %d".formatted(this.arguments.length));
		return this.arguments[index];
	}

	@Override
	public <T> T get(int index, Class<T> requiredType) {
		Preconditions.notNull(requiredType, "requiredType must not be null");
		Object value = get(index);
		try {
			Object convertedValue = converter.apply(value, requiredType);
			return requiredType.cast(convertedValue);
		}
		catch (Exception ex) {
			String message = "Argument at index [%d] with value [%s] and type [%s] could not be converted or cast to type [%s].".formatted(
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
