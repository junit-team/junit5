/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.converter;

import java.util.Locale;
import java.util.function.Function;

import org.junit.platform.commons.util.Preconditions;

class StringToLocaleConverter implements StringToObjectConverter {

	private final Function<String, Locale> converter;

	StringToLocaleConverter(LocaleConversionFormat format) {
		Preconditions.notNull(format, "format must not be null");
		this.converter = format == LocaleConversionFormat.ISO_639 ? Locale::new : Locale::forLanguageTag;
	}

	@Override
	public boolean canConvert(Class<?> targetType) {
		return targetType == Locale.class;
	}

	@Override
	public Object convert(String source, Class<?> targetType) {
		return converter.apply(source);
	}

}
