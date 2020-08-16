/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.params.provider.MockCsvAnnotationBuilder.csvSource;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * @since 5.0
 */
class CsvArgumentsProviderTests {

	@Test
	void throwsExceptionOnInvalidCsv() {
		CsvSource annotation = csvSource("foo", "bar", "");

		assertThatExceptionOfType(JUnitException.class)//
				.isThrownBy(() -> provideArguments(annotation).toArray())//
				.withMessage("Line at index 2 contains invalid CSV: \"\"");
	}

	@Test
	void providesSingleArgument() {
		CsvSource annotation = csvSource("foo");

		Stream<Object[]> arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("foo"));
	}

	@Test
	void providesMultipleArguments() {
		CsvSource annotation = csvSource("foo", "bar");

		Stream<Object[]> arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("foo"), array("bar"));
	}

	@Test
	void splitsAndTrimsArguments() {
		CsvSource annotation = csvSource(" foo , bar ");

		Stream<Object[]> arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("foo", "bar"));
	}

	@Test
	void trimsLeadingSpaces() {
		CsvSource annotation = csvSource("'', 1", " '', 2", "'' , 3", " '' , 4");

		Stream<Object[]> arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(new Object[][] { { "", "1" }, { "", "2" }, { "", "3" }, { "", "4" } });
	}

	@Test
	void trimsTrailingSpaces() {
		CsvSource annotation = csvSource("1,''", "2, ''", "3,'' ", "4, '' ");

		Stream<Object[]> arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(new Object[][] { { "1", "" }, { "2", "" }, { "3", "" }, { "4", "" } });
	}

	@Test
	void understandsQuotes() {
		CsvSource annotation = csvSource("'foo, bar'");

		Stream<Object[]> arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("foo, bar"));
	}

	@Test
	void understandsEscapeCharacters() {
		CsvSource annotation = csvSource("'foo or ''bar''', baz");

		Stream<Object[]> arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("foo or 'bar'", "baz"));
	}

	@Test
	void doesNotTrimSpacesInsideQuotes() {
		CsvSource annotation = csvSource("''", "'   '", "'blank '", "' not blank   '");

		Stream<Object[]> arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array(""), array("   "), array("blank "), array(" not blank   "));
	}

	@Test
	void providesArgumentsWithCharacterDelimiter() {
		CsvSource annotation = csvSource().delimiter('|').lines("foo|bar", "bar|foo").build();

		Stream<Object[]> arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("foo", "bar"), array("bar", "foo"));
	}

	@Test
	void providesArgumentsWithStringDelimiter() {
		CsvSource annotation = csvSource().delimiterString("~~~").lines("foo~~~ bar", "bar~~~ foo").build();

		Stream<Object[]> arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("foo", "bar"), array("bar", "foo"));
	}

	@Test
	void throwsExceptionIfBothDelimitersAreSimultaneouslySet() {
		CsvSource annotation = csvSource().delimiter('|').delimiterString("~~~").build();

		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(() -> provideArguments(annotation))//
				.withMessageStartingWith("The delimiter and delimiterString attributes cannot be set simultaneously in")//
				.withMessageContaining("CsvSource");
	}

	@Test
	void defaultEmptyValueAndDefaultNullValue() {
		CsvSource annotation = csvSource("'', null, , apple");

		Stream<Object[]> arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("", "null", null, "apple"));
	}

	@Test
	void customEmptyValueAndDefaultNullValue() {
		CsvSource annotation = csvSource().emptyValue("EMPTY").lines("'', null, , apple").build();

		Stream<Object[]> arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("EMPTY", "null", null, "apple"));
	}

	@Test
	void customNullValues() {
		CsvSource annotation = csvSource().nullValues("N/A", "NIL").lines("apple, , NIL, '', N/A, banana").build();

		Stream<Object[]> arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("apple", null, null, "", null, "banana"));
	}

	@Test
	void convertsEmptyValuesToNullInLinesAfterFirstLine() {
		CsvSource annotation = csvSource("'', ''", " , ");

		Stream<Object[]> arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(new Object[][] { { "", "" }, { null, null } });
	}

	@Test
	void throwsExceptionIfSourceExceedsMaxCharsPerColumnConfig() {
		CsvSource annotation = csvSource().lines("413").maxCharsPerColumn(2).build();

		Stream<Object[]> arguments = provideArguments(annotation);

		assertThatExceptionOfType(CsvParsingException.class)//
				.isThrownBy(() -> arguments.toArray())//
				.withMessageStartingWith("Failed to parse CSV input configured via Mock for CsvSource")//
				.withRootCauseInstanceOf(ArrayIndexOutOfBoundsException.class);
	}

	@Test
	void providesArgumentWithDefaultMaxCharsPerColumnConfig() {
		CsvSource annotation = csvSource().lines("0".repeat(4096)).delimiter(';').build();

		Stream<Object[]> arguments = provideArguments(annotation);

		assertThat(arguments.toArray()).hasSize(1);
	}

	@Test
	void throwsExceptionWhenSourceExceedsDefaultMaxCharsPerColumnConfig() {
		CsvSource annotation = csvSource().lines("0".repeat(4097)).delimiter(';').build();

		Stream<Object[]> arguments = provideArguments(annotation);

		assertThatExceptionOfType(CsvParsingException.class)//
				.isThrownBy(() -> arguments.toArray())//
				.withMessageStartingWith("Failed to parse CSV input configured via Mock for CsvSource")//
				.withRootCauseInstanceOf(ArrayIndexOutOfBoundsException.class);
	}

	@Test
	void providesArgumentsForExceedsSourceWithCustomMaxCharsPerColumnConfig() {
		CsvSource annotation = csvSource().lines("0".repeat(4097)).delimiter(';').maxCharsPerColumn(4097).build();

		Stream<Object[]> arguments = provideArguments(annotation);

		assertThat(arguments.toArray()).hasSize(1);
	}

	@Test
	void throwsExceptionWhenMaxCharsPerColumnIsNotPositiveNumber() {
		CsvSource annotation = csvSource().lines("41").delimiter(';').maxCharsPerColumn(-1).build();

		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(() -> provideArguments(annotation))//
				.withMessageStartingWith("maxCharsPerColumn must be a positive number: -1");
	}

	private Stream<Object[]> provideArguments(CsvSource annotation) {
		CsvArgumentsProvider provider = new CsvArgumentsProvider();
		provider.accept(annotation);
		return provider.provideArguments(null).map(Arguments::get);
	}

	@SuppressWarnings("unchecked")
	private static <T> T[] array(T... elements) {
		return elements;
	}

}
