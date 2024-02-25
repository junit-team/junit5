/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.support.conversion;

import static java.util.Collections.unmodifiableMap;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

class StringToCommonJavaTypesConverter implements StringToObjectConverter {

	private static final Map<Class<?>, Function<String, ?>> CONVERTERS;

	static {
		Map<Class<?>, Function<String, ?>> converters = new HashMap<>();

		// java.io and java.nio
		converters.put(File.class, File::new);
		converters.put(Charset.class, Charset::forName);
		converters.put(Path.class, Paths::get);
		// java.net
		converters.put(URI.class, URI::create);
		converters.put(URL.class, StringToCommonJavaTypesConverter::toURL);
		// java.util
		converters.put(Currency.class, Currency::getInstance);
		converters.put(Locale.class, Locale::new);
		converters.put(UUID.class, UUID::fromString);

		CONVERTERS = unmodifiableMap(converters);
	}

	@Override
	public boolean canConvertTo(Class<?> targetType) {
		return CONVERTERS.containsKey(targetType);
	}

	@Override
	public Object convert(String source, Class<?> targetType) throws Exception {
		return CONVERTERS.get(targetType).apply(source);
	}

	private static URL toURL(String url) {
		try {
			return URI.create(url).toURL();
		}
		catch (MalformedURLException ex) {
			throw new ConversionException("Failed to convert String \"" + url + "\" to type java.net.URL", ex);
		}
	}

}
