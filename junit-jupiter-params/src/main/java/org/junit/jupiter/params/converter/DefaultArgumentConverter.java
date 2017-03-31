/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.converter;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * {@code DefaultArgumentConverter} is the default implementation of the
 * {@link ArgumentConverter} API.
 *
 * <p>The {@code DefaultArgumentConverter} is able to convert from strings to a
 * number of primitive types and their corresponding wrapper types (Byte, Short,
 * Integer, Long, Float, and Double) as well as date and time types from the
 * {@code java.time} package.
 *
 * <p>If the source and target types are identical the source object will not
 * be modified.
 *
 * @since 5.0
 * @see org.junit.jupiter.params.converter.ArgumentConverter
 */
@API(Internal)
public class DefaultArgumentConverter extends SimpleArgumentConverter {

	public static final DefaultArgumentConverter INSTANCE = new DefaultArgumentConverter();

	private static final List<StringToObjectConverter> stringToObjectConverters = unmodifiableList(
		asList(new StringToPrimitiveConverter(), new StringToEnumConverter(), new StringToJavaTimeConverter()));

	private DefaultArgumentConverter() {
		// nothing to initialize
	}

	@Override
	protected Object convert(Object source, Class<?> targetType) {
		if (source == null) {
			if (targetType.isPrimitive()) {
				throw new ArgumentConversionException(
					"Cannot convert null to primitive value of type " + targetType.getName());
			}
			return null;
		}
		return convertToTargetType(source, toWrapperType(targetType));
	}

	private Class<?> toWrapperType(Class<?> targetType) {
		Class<?> wrapperType = ReflectionUtils.getWrapperType(targetType);
		return wrapperType != null ? wrapperType : targetType;
	}

	private Object convertToTargetType(Object source, Class<?> targetType) {
		if (targetType.isInstance(source)) {
			return source;
		}
		if (source instanceof String) {
			Optional<StringToObjectConverter> converter = stringToObjectConverters.stream().filter(
				candidate -> candidate.canConvert(targetType)).findFirst();
			if (converter.isPresent()) {
				try {
					return converter.get().convert((String) source, targetType);
				}
				catch (Exception ex) {
					throw new ArgumentConversionException(
						"Failed to convert String [" + source + "] to type " + targetType.getName(), ex);
				}
			}
		}
		throw new ArgumentConversionException("No implicit conversion to convert object of type "
				+ source.getClass().getName() + " to type " + targetType.getName());
	}

	interface StringToObjectConverter {

		boolean canConvert(Class<?> targetType);

		Object convert(String source, Class<?> targetType) throws Exception;

	}

	static class StringToPrimitiveConverter implements StringToObjectConverter {

		private static final Map<Class<?>, Function<String, ?>> CONVERTERS;
		static {
			Map<Class<?>, Function<String, ?>> converters = new HashMap<>();
			converters.put(Boolean.class, Boolean::valueOf);
			converters.put(Character.class, source -> {
				Preconditions.condition(source.length() == 1, () -> "String must have length of 1: " + source);
				return source.charAt(0);
			});
			converters.put(Byte.class, Byte::valueOf);
			converters.put(Short.class, Short::valueOf);
			converters.put(Integer.class, Integer::valueOf);
			converters.put(Long.class, Long::valueOf);
			converters.put(Float.class, Float::valueOf);
			converters.put(Double.class, Double::valueOf);
			CONVERTERS = unmodifiableMap(converters);
		}

		@Override
		public boolean canConvert(Class<?> targetType) {
			return CONVERTERS.containsKey(targetType);
		}

		@Override
		public Object convert(String source, Class<?> targetType) {
			return CONVERTERS.get(targetType).apply(source);
		}
	}

	static class StringToEnumConverter implements StringToObjectConverter {

		@Override
		public boolean canConvert(Class<?> targetType) {
			return targetType.isEnum();
		}

		@Override
		public Object convert(String source, Class<?> targetType) throws Exception {
			return valueOf(targetType, source);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		private Object valueOf(Class targetType, String source) {
			return Enum.valueOf(targetType, source);
		}
	}

	static class StringToJavaTimeConverter implements StringToObjectConverter {

		private static final Map<Class<?>, Function<CharSequence, ?>> CONVERTERS;
		static {
			Map<Class<?>, Function<CharSequence, ?>> converters = new HashMap<>();
			converters.put(Instant.class, Instant::parse);
			converters.put(LocalDate.class, LocalDate::parse);
			converters.put(LocalDateTime.class, LocalDateTime::parse);
			converters.put(LocalTime.class, LocalTime::parse);
			converters.put(OffsetDateTime.class, OffsetDateTime::parse);
			converters.put(OffsetTime.class, OffsetTime::parse);
			converters.put(Year.class, Year::parse);
			converters.put(YearMonth.class, YearMonth::parse);
			converters.put(ZonedDateTime.class, ZonedDateTime::parse);
			CONVERTERS = Collections.unmodifiableMap(converters);
		}

		@Override
		public boolean canConvert(Class<?> targetType) {
			return CONVERTERS.containsKey(targetType);
		}

		@Override
		public Object convert(String source, Class<?> targetType) throws Exception {
			return CONVERTERS.get(targetType).apply(source);
		}
	}

}
