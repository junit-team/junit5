/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.format.DateTimeParseException;

import org.junit.jupiter.api.Test;

class TimeoutDurationParserTests {

	private final TimeoutDurationParser parser = new TimeoutDurationParser();

	@Test
	void parsesNumberWithoutUnitIntoSecondsDurations() {
		assertEquals(new TimeoutDuration(42, SECONDS), parser.parse("42"));
	}

	@Test
	void parsesLowerCaseDurations() {
		assertAll(() -> assertEquals(new TimeoutDuration(42, NANOSECONDS), parser.parse("42ns")),
			() -> assertEquals(new TimeoutDuration(42, MICROSECONDS), parser.parse("42μs")),
			() -> assertEquals(new TimeoutDuration(42, MILLISECONDS), parser.parse("42ms")),
			() -> assertEquals(new TimeoutDuration(42, SECONDS), parser.parse("42s")),
			() -> assertEquals(new TimeoutDuration(42, MINUTES), parser.parse("42m")),
			() -> assertEquals(new TimeoutDuration(42, HOURS), parser.parse("42h")),
			() -> assertEquals(new TimeoutDuration(42, DAYS), parser.parse("42d")));
	}

	@Test
	void parsesUpperCaseDurations() {
		assertAll(() -> assertEquals(new TimeoutDuration(42, NANOSECONDS), parser.parse("42NS")),
			() -> assertEquals(new TimeoutDuration(42, MICROSECONDS), parser.parse("42ΜS")),
			() -> assertEquals(new TimeoutDuration(42, MILLISECONDS), parser.parse("42MS")),
			() -> assertEquals(new TimeoutDuration(42, SECONDS), parser.parse("42S")),
			() -> assertEquals(new TimeoutDuration(42, MINUTES), parser.parse("42M")),
			() -> assertEquals(new TimeoutDuration(42, HOURS), parser.parse("42H")),
			() -> assertEquals(new TimeoutDuration(42, DAYS), parser.parse("42D")));
	}

	@Test
	void rejectsNumbersStartingWithZero() {
		assertThrows(DateTimeParseException.class, () -> parser.parse("01"));
	}
}
