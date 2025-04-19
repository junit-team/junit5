/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.converter;

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.platform.commons.util.ClassLoaderUtils.getClassLoader;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.util.Currency;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Function;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.support.FieldContext;
import org.junit.platform.commons.support.conversion.ConversionException;
import org.junit.platform.commons.support.conversion.ConversionSupport;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * {@code DefaultArgumentConverter} is the default implementation of the
 * {@link ArgumentConverter} API.
 *
 * <p>The {@code DefaultArgumentConverter} is able to convert from strings to a
 * number of primitive types and their corresponding wrapper types (Byte, Short,
 * Integer, Long, Float, and Double), date and time types from the
 * {@code java.time} package, and some additional common Java types such as
 * {@link File}, {@link BigDecimal}, {@link BigInteger}, {@link Currency},
 * {@link Locale}, {@link URI}, {@link URL}, {@link UUID}, etc.
 *
 * <p>If the source and target types are identical the source object will not
 * be modified.
 *
 * @since 5.0
 * @see org.junit.jupiter.params.converter.ArgumentConverter
 * @see org.junit.platform.commons.support.conversion.ConversionSupport
 */
@API(status = INTERNAL, since = "5.0")
public class DefaultArgumentConverter implements ArgumentConverter {

	static final String DEFAULT_LOCALE_CONVERSION_FORMAT_PROPERTY_NAME = "junit.jupiter.params.arguments.conversion.locale.format";

	private static final Function<String, LocaleConversionFormat> TRANSFORMER = value -> LocaleConversionFormat.valueOf(
		value.trim().toUpperCase(Locale.ROOT));

	private final ExtensionContext context;

	public DefaultArgumentConverter(ExtensionContext context) {
		this.context = context;
	}

	@Override
	public final Object convert(Object source, ParameterContext context) {
		Class<?> targetType = context.getParameter().getType();
		ClassLoader classLoader = getClassLoader(context.getDeclaringExecutable().getDeclaringClass());
		return convert(source, targetType, classLoader);
	}

	@Override
	public final Object convert(Object source, FieldContext context) throws ArgumentConversionException {
		Class<?> targetType = context.getField().getType();
		ClassLoader classLoader = getClassLoader(context.getField().getDeclaringClass());
		return convert(source, targetType, classLoader);
	}

	public final Object convert(Object source, Class<?> targetType, ClassLoader classLoader) {
		if (source == null) {
			if (targetType.isPrimitive()) {
				throw new ArgumentConversionException(
					"Cannot convert null to primitive value of type " + targetType.getTypeName());
			}
			return null;
		}

		if (ReflectionUtils.isAssignableTo(source, targetType)) {
			return source;
		}

		if (source instanceof String) {
			if (targetType == Locale.class && getLocaleConversionFormat() == LocaleConversionFormat.BCP_47) {
				return Locale.forLanguageTag((String) source);
			}

			try {
				return convert((String) source, targetType, classLoader);
			}
			catch (ConversionException ex) {
				throw new ArgumentConversionException(ex.getMessage(), ex);
			}
		}

		throw new ArgumentConversionException(
			String.format("No built-in converter for source type %s and target type %s",
				source.getClass().getTypeName(), targetType.getTypeName()));
	}

	private LocaleConversionFormat getLocaleConversionFormat() {
		return context.getConfigurationParameter(DEFAULT_LOCALE_CONVERSION_FORMAT_PROPERTY_NAME, TRANSFORMER).orElse(
			LocaleConversionFormat.BCP_47);
	}

	Object convert(String source, Class<?> targetType, ClassLoader classLoader) {
		return ConversionSupport.convert(source, targetType, classLoader);
	}

	/**
	 * Enumeration of {@link Locale} conversion formats.
	 *
	 * @since 5.13
	 */
	enum LocaleConversionFormat {

		/**
		 * The IETF BCP 47 language tag format.
		 *
		 * @see Locale#forLanguageTag(String)
		 */
		BCP_47,

		/**
		 * The ISO 639 alpha-2 or alpha-3 language code format.
		 *
		 * @see Locale#Locale(String)
		 */
		ISO_639

	}

}
