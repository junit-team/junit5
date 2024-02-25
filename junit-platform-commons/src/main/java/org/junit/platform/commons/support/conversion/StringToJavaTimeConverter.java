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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.MonthDay;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Period;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

class StringToJavaTimeConverter implements StringToObjectConverter {

	private static final Map<Class<?>, Function<String, ?>> CONVERTERS;
	static {
		Map<Class<?>, Function<String, ?>> converters = new HashMap<>();
		converters.put(Duration.class, Duration::parse);
		converters.put(Instant.class, Instant::parse);
		converters.put(LocalDate.class, LocalDate::parse);
		converters.put(LocalDateTime.class, LocalDateTime::parse);
		converters.put(LocalTime.class, LocalTime::parse);
		converters.put(MonthDay.class, MonthDay::parse);
		converters.put(OffsetDateTime.class, OffsetDateTime::parse);
		converters.put(OffsetTime.class, OffsetTime::parse);
		converters.put(Period.class, Period::parse);
		converters.put(Year.class, Year::parse);
		converters.put(YearMonth.class, YearMonth::parse);
		converters.put(ZonedDateTime.class, ZonedDateTime::parse);
		converters.put(ZoneId.class, ZoneId::of);
		converters.put(ZoneOffset.class, ZoneOffset::of);
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

}
