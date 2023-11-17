/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.support.conversion;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.junit.platform.commons.util.ReflectionUtils.getWrapperType;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.Currency;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.ClassLoaderUtils;

/**
 * {@code StringConversionSupport} is able to convert from strings to a number
 * of primitive types and their corresponding wrapper types (Byte, Short,
 * Integer, Long, Float, and Double), date and time types from the
 * {@code java.time} package, and some additional common Java types such as
 * {@link File}, {@link BigDecimal}, {@link BigInteger}, {@link Currency},
 * {@link Locale}, {@link URI}, {@link URL}, {@link UUID}, etc.
 *
 * <p>If the target type is {@code String} the source {@code String} will not
 * be modified.
 *
 * @since 1.11
 */
@API(status = EXPERIMENTAL, since = "1.11")
public final class StringConversionSupport {

	private static final List<StringToObjectConverter> stringToObjectConverters = unmodifiableList(asList( //
		new StringToBooleanConverter(), //
		new StringToCharacterConverter(), //
		new StringToNumberConverter(), //
		new StringToClassConverter(), //
		new StringToEnumConverter(), //
		new StringToJavaTimeConverter(), //
		new StringToCommonJavaTypesConverter(), //
		new FallbackStringToObjectConverter() //
	));

	private StringConversionSupport() {
		/* no-op */
	}

	/**
	 * Convert a {@code String} into an object of the supplied type.
	 *
	 * <p>Some underlying converters can require a {@code ClassLoader}.
	 * If none is provided, the default one given by
	 * {@link ClassLoaderUtils#getDefaultClassLoader()} will be used.
	 *
	 * @param source the source {@code String} to convert; may be {@code null}
	 * @param targetType the target type the source should be converted into;
	 * never {@code null}
	 * @param classLoader the {@code ClassLoader} to use; may be {@code null}
	 * @param <T> the type of the target
	 * @return the converted object; may be {@code null} but only if the target
	 * type is a reference type
	 *
	 * @since 1.11
	 */
	@SuppressWarnings("unchecked")
	public static <T> T convert(String source, Class<T> targetType, ClassLoader classLoader) {
		if (source == null) {
			if (targetType.isPrimitive()) {
				throw new ConversionException(
					"Cannot convert null to primitive value of type " + targetType.getTypeName());
			}
			return null;
		}

		if (String.class.equals(targetType)) {
			return (T) source;
		}

		Class<?> targetTypeToUse = toWrapperType(targetType);
		Optional<StringToObjectConverter> converter = stringToObjectConverters.stream().filter(
			candidate -> candidate.canConvert(targetTypeToUse)).findFirst();
		if (converter.isPresent()) {
			try {
				ClassLoader classLoaderToUse = classLoader != null ? classLoader
						: ClassLoaderUtils.getDefaultClassLoader();
				return (T) converter.get().convert(source, targetTypeToUse, classLoaderToUse);
			}
			catch (Exception ex) {
				if (ex instanceof ConversionException) {
					// simply rethrow it
					throw (ConversionException) ex;
				}
				// else
				throw new ConversionException(
					String.format("Failed to convert String \"%s\" to type %s", source, targetType.getTypeName()), ex);
			}
		}

		throw new ConversionException(
			"No built-in converter for source type java.lang.String and target type " + targetType.getTypeName());
	}

	private static Class<?> toWrapperType(Class<?> targetType) {
		Class<?> wrapperType = getWrapperType(targetType);
		return wrapperType != null ? wrapperType : targetType;
	}

}
