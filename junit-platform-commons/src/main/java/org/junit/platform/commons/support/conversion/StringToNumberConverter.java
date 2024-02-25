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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

class StringToNumberConverter implements StringToObjectConverter {

	private static final Map<Class<?>, Function<String, ?>> CONVERTERS;
	static {
		Map<Class<?>, Function<String, ?>> converters = new HashMap<>();
		converters.put(Byte.class, Byte::decode);
		converters.put(Short.class, Short::decode);
		converters.put(Integer.class, Integer::decode);
		converters.put(Long.class, Long::decode);
		converters.put(Float.class, Float::valueOf);
		converters.put(Double.class, Double::valueOf);
		// Technically, BigInteger and BigDecimal constructors are covered by
		// FallbackStringToObjectConverter, but we have explicit conversion
		// configured for them anyway.
		converters.put(BigInteger.class, BigInteger::new);
		converters.put(BigDecimal.class, BigDecimal::new);
		CONVERTERS = unmodifiableMap(converters);
	}

	@Override
	public boolean canConvertTo(Class<?> targetType) {
		return CONVERTERS.containsKey(targetType);
	}

	@Override
	public Object convert(String source, Class<?> targetType) {
		return CONVERTERS.get(targetType).apply(source.replace("_", ""));
	}

}
