/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.converter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalQuery;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.params.support.AnnotationConsumer;

/**
 * @since 5.0
 */
class JavaTimeArgumentConverter extends SimpleArgumentConverter
		implements AnnotationConsumer<JavaTimeConversionPattern> {

	private static final Map<Class<?>, TemporalQuery<?>> TEMPORAL_QUERIES;
	static {
		Map<Class<?>, TemporalQuery<?>> queries = new LinkedHashMap<>();
		queries.put(ChronoLocalDate.class, ChronoLocalDate::from);
		queries.put(ChronoLocalDateTime.class, ChronoLocalDateTime::from);
		queries.put(ChronoZonedDateTime.class, ChronoZonedDateTime::from);
		queries.put(LocalDate.class, LocalDate::from);
		queries.put(LocalDateTime.class, LocalDateTime::from);
		queries.put(LocalTime.class, LocalTime::from);
		queries.put(OffsetDateTime.class, OffsetDateTime::from);
		queries.put(OffsetTime.class, OffsetTime::from);
		queries.put(Year.class, Year::from);
		queries.put(YearMonth.class, YearMonth::from);
		queries.put(ZonedDateTime.class, ZonedDateTime::from);
		TEMPORAL_QUERIES = Collections.unmodifiableMap(queries);
	}

	private String pattern;

	@Override
	public void accept(JavaTimeConversionPattern annotation) {
		pattern = annotation.value();
	}

	@Override
	public Object convert(Object input, Class<?> targetClass) throws ArgumentConversionException {
		if (!TEMPORAL_QUERIES.containsKey(targetClass)) {
			throw new ArgumentConversionException("Cannot convert to " + targetClass.getName() + ": " + input);
		}
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		TemporalQuery<?> temporalQuery = TEMPORAL_QUERIES.get(targetClass);
		return formatter.parse(input.toString(), temporalQuery);
	}

}
