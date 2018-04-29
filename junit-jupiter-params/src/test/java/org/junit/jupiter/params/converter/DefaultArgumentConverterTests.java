/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.converter;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Currency;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

/**
 * @since 5.0
 */
class DefaultArgumentConverterTests {

	@Test
	void isAwareOfNull() {
		assertConverts(null, Object.class, null);
		assertConverts(null, String.class, null);
	}

	@Test
	void isAwareOfWrapperTypesForPrimitiveTypes() {
		assertConverts(true, boolean.class, true);
		assertConverts((byte) 1, byte.class, (byte) 1);
		assertConverts('o', char.class, 'o');
		assertConverts((short) 1, short.class, (short) 1);
		assertConverts(1, int.class, 1);
		assertConverts(1L, long.class, 1L);
		assertConverts(1.0f, float.class, 1.0f);
		assertConverts(1.0d, double.class, 1.0d);
	}

	@Test
	void isAwareOfWideningConversions() {
		assertConverts((byte) 1, short.class, (byte) 1);
		assertConverts((short) 1, int.class, (short) 1);
		assertConverts((char) 1, int.class, (char) 1);
		assertConverts((byte) 1, long.class, (byte) 1);
		assertConverts(1, long.class, 1);
		assertConverts((char) 1, float.class, (char) 1);
		assertConverts(1, float.class, 1);
		assertConverts(1L, double.class, 1L);
		assertConverts(1.0f, double.class, 1.0f);
	}

	@Test
	void convertsStringsToPrimitiveTypes() {
		assertConverts("true", boolean.class, true);
		assertConverts("1", byte.class, (byte) 1);
		assertConverts("o", char.class, 'o');
		assertConverts("1", short.class, (short) 1);
		assertConverts("42", int.class, 42);
		assertConverts("42", long.class, 42L);
		assertConverts("42.23", float.class, 42.23f);
		assertConverts("42.23", double.class, 42.23);
	}

	@Test
	void convertsStringsToEnumConstants() {
		assertConverts("DAYS", TimeUnit.class, TimeUnit.DAYS);
	}

	// --- java.io and java.nio ------------------------------------------------

	@Test
	void convertsStringToCharset() {
		assertConverts("ISO-8859-1", Charset.class, Charset.forName("ISO-8859-1"));
		assertConverts("UTF-8", Charset.class, Charset.forName("UTF-8"));
	}

	@Test
	void convertsStringToFile() {
		assertConverts("file", File.class, new File("file"));
		assertConverts("/file", File.class, new File("/file"));
		assertConverts("/some/file", File.class, new File("/some/file"));
	}

	@Test
	void convertsStringToPath() {
		assertConverts("path", Path.class, Paths.get("path"));
		assertConverts("/path", Path.class, Paths.get("/path"));
		assertConverts("/some/path", Path.class, Paths.get("/some/path"));
	}

	// --- java.math -----------------------------------------------------------

	@Test
	void convertsStringToBigDecimal() {
		assertConverts("123.456e789", BigDecimal.class, new BigDecimal("123.456e789"));
	}

	@Test
	void convertsStringToBigInteger() {
		assertConverts("1234567890123456789", BigInteger.class, new BigInteger("1234567890123456789"));
	}

	// --- java.net ------------------------------------------------------------

	@Test
	void convertsStringToURI() {
		assertConverts("http://java.sun.com/j2se/1.3/", URI.class, URI.create("http://java.sun.com/j2se/1.3/"));
	}

	@Test
	void convertsStringToURL() throws Exception {
		assertConverts("http://junit.org/junit5", URL.class, new URL("http://junit.org/junit5"));
	}

	// --- java.time -----------------------------------------------------------

	@Test
	void convertsStringsToJavaTimeInstances() {
		assertConverts("1970-01-01T00:00:00Z", Instant.class, Instant.ofEpochMilli(0));
		assertConverts("2017-03-14", LocalDate.class, LocalDate.of(2017, 3, 14));
		assertConverts("2017-03-14T12:34:56.789", LocalDateTime.class,
			LocalDateTime.of(2017, 3, 14, 12, 34, 56, 789_000_000));
		assertConverts("12:34:56.789", LocalTime.class, LocalTime.of(12, 34, 56, 789_000_000));
		assertConverts("2017-03-14T12:34:56.789Z", OffsetDateTime.class,
			OffsetDateTime.of(2017, 3, 14, 12, 34, 56, 789_000_000, ZoneOffset.UTC));
		assertConverts("12:34:56.789Z", OffsetTime.class, OffsetTime.of(12, 34, 56, 789_000_000, ZoneOffset.UTC));
		assertConverts("2017", Year.class, Year.of(2017));
		assertConverts("2017-03", YearMonth.class, YearMonth.of(2017, 3));
		assertConverts("2017-03-14T12:34:56.789Z", ZonedDateTime.class,
			ZonedDateTime.of(2017, 3, 14, 12, 34, 56, 789_000_000, ZoneOffset.UTC));
	}

	// --- java.util -----------------------------------------------------------

	@Test
	void convertsStringToCurrency() {
		assertConverts("JPY", Currency.class, Currency.getInstance("JPY"));
	}

	@Test
	void convertsStringToLocale() {
		assertConverts("en", Locale.class, Locale.ENGLISH);
		assertConverts("en_us", Locale.class, new Locale(Locale.US.toString()));
	}

	@Test
	void convertsStringToUUID() {
		String uuid = "d043e930-7b3b-48e3-bdbe-5a3ccfb833db";
		assertConverts(uuid, UUID.class, UUID.fromString(uuid));
	}

	// -------------------------------------------------------------------------

	private void assertConverts(Object input, Class<?> targetClass, Object expectedOutput) {
		Object result = DefaultArgumentConverter.INSTANCE.convert(input, targetClass);

		assertThat(result) //
				.describedAs(input + " --(" + targetClass.getName() + ")--> " + expectedOutput) //
				.isEqualTo(expectedOutput);
	}

}
