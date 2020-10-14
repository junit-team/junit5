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
		var annotation = csvSource("foo", "bar", "");

		assertThatExceptionOfType(JUnitException.class)//
				.isThrownBy(() -> provideArguments(annotation).toArray())//
				.withMessage("Line at index 2 contains invalid CSV: \"\"");
	}

	@Test
	void providesSingleArgument() {
		var annotation = csvSource("foo");

		var arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("foo"));
	}

	@Test
	void providesMultipleArguments() {
		var annotation = csvSource("foo", "bar");

		var arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("foo"), array("bar"));
	}

	@Test
	void splitsAndTrimsArguments() {
		var annotation = csvSource(" foo , bar ");

		var arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("foo", "bar"));
	}

	@Test
	void trimsLeadingSpaces() {
		var annotation = csvSource("'', 1", " '', 2", "'' , 3", " '' , 4");

		var arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(new Object[][] { { "", "1" }, { "", "2" }, { "", "3" }, { "", "4" } });
	}

	@Test
	void trimsTrailingSpaces() {
		var annotation = csvSource("1,''", "2, ''", "3,'' ", "4, '' ");

		var arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(new Object[][] { { "1", "" }, { "2", "" }, { "3", "" }, { "4", "" } });
	}

	@Test
	void understandsQuotes() {
		var annotation = csvSource("'foo, bar'");

		var arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("foo, bar"));
	}

	@Test
	void understandsEscapeCharacters() {
		var annotation = csvSource("'foo or ''bar''', baz");

		var arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("foo or 'bar'", "baz"));
	}

	@Test
	void doesNotTrimSpacesInsideQuotes() {
		var annotation = csvSource("''", "'   '", "'blank '", "' not blank   '");

		var arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array(""), array("   "), array("blank "), array(" not blank   "));
	}

	@Test
	void providesArgumentsWithCharacterDelimiter() {
		var annotation = csvSource().delimiter('|').lines("foo|bar", "bar|foo").build();

		var arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("foo", "bar"), array("bar", "foo"));
	}

	@Test
	void providesArgumentsWithStringDelimiter() {
		var annotation = csvSource().delimiterString("~~~").lines("foo~~~ bar", "bar~~~ foo").build();

		var arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("foo", "bar"), array("bar", "foo"));
	}

	@Test
	void throwsExceptionIfBothDelimitersAreSimultaneouslySet() {
		var annotation = csvSource().delimiter('|').delimiterString("~~~").build();

		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(() -> provideArguments(annotation))//
				.withMessageStartingWith("The delimiter and delimiterString attributes cannot be set simultaneously in")//
				.withMessageContaining("CsvSource");
	}

	@Test
	void defaultEmptyValueAndDefaultNullValue() {
		var annotation = csvSource("'', null, , apple");

		var arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("", "null", null, "apple"));
	}

	@Test
	void customEmptyValueAndDefaultNullValue() {
		var annotation = csvSource().emptyValue("EMPTY").lines("'', null, , apple").build();

		var arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("EMPTY", "null", null, "apple"));
	}

	@Test
	void customNullValues() {
		var annotation = csvSource().nullValues("N/A", "NIL").lines("apple, , NIL, '', N/A, banana").build();

		var arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("apple", null, null, "", null, "banana"));
	}

	@Test
	void convertsEmptyValuesToNullInLinesAfterFirstLine() {
		var annotation = csvSource("'', ''", " , ");

		var arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(new Object[][] { { "", "" }, { null, null } });
	}

	@Test
	void throwsExceptionIfSourceExceedsMaxCharsPerColumnConfig() {
		var annotation = csvSource().lines("413").maxCharsPerColumn(2).build();

		var arguments = provideArguments(annotation);

		assertThatExceptionOfType(CsvParsingException.class)//
				.isThrownBy(arguments::toArray)//
				.withMessageStartingWith("Failed to parse CSV input configured via Mock for CsvSource")//
				.withRootCauseInstanceOf(ArrayIndexOutOfBoundsException.class);
	}

	@Test
	void providesArgumentWithDefaultMaxCharsPerColumnConfig() {
		var annotation = csvSource().lines("0".repeat(4096)).delimiter(';').build();

		var arguments = provideArguments(annotation);

		assertThat(arguments.toArray()).hasSize(1);
	}

	@Test
	void throwsExceptionWhenSourceExceedsDefaultMaxCharsPerColumnConfig() {
		var annotation = csvSource().lines("0".repeat(4097)).delimiter(';').build();

		var arguments = provideArguments(annotation);

		assertThatExceptionOfType(CsvParsingException.class)//
				.isThrownBy(arguments::toArray)//
				.withMessageStartingWith("Failed to parse CSV input configured via Mock for CsvSource")//
				.withRootCauseInstanceOf(ArrayIndexOutOfBoundsException.class);
	}

	@Test
	void providesArgumentsForExceedsSourceWithCustomMaxCharsPerColumnConfig() {
		var annotation = csvSource().lines("0".repeat(4097)).delimiter(';').maxCharsPerColumn(4097).build();

		var arguments = provideArguments(annotation);

		assertThat(arguments.toArray()).hasSize(1);
	}

	@Test
	void throwsExceptionWhenMaxCharsPerColumnIsNotPositiveNumber() {
		var annotation = csvSource().lines("41").delimiter(';').maxCharsPerColumn(-1).build();

		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(() -> provideArguments(annotation))//
				.withMessageStartingWith("maxCharsPerColumn must be a positive number: -1");
	}

	private Stream<Object[]> provideArguments(CsvSource annotation) {
		var provider = new CsvArgumentsProvider();
		provider.accept(annotation);
		return provider.provideArguments(null).map(Arguments::get);
	}

	@SuppressWarnings("unchecked")
	private static <T> T[] array(T... elements) {
		return elements;
	}

}
