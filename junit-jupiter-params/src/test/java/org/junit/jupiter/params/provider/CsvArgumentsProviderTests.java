/*
 * Copyright 2015-2024 the original author or authors.
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
import static org.mockito.Mockito.mock;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * @since 5.0
 */
class CsvArgumentsProviderTests {

	@Test
	void throwsExceptionForInvalidCsv() {
		var annotation = csvSource("foo", "bar", "");

		assertThatExceptionOfType(JUnitException.class)//
				.isThrownBy(() -> provideArguments(annotation).toArray())//
				.withMessage("Record at index 3 contains invalid CSV: \"\"");
	}

	@Test
	void throwsExceptionIfNeitherValueNorTextBlockIsDeclared() {
		var annotation = csvSource().build();

		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(() -> provideArguments(annotation))//
				.withMessage("@CsvSource must be declared with either `value` or `textBlock` but not both");
	}

	@Test
	void throwsExceptionIfValueAndTextBlockAreDeclared() {
		var annotation = csvSource().lines("foo").textBlock("""
				bar
				baz
				""").build();

		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(() -> provideArguments(annotation))//
				.withMessage("@CsvSource must be declared with either `value` or `textBlock` but not both");
	}

	@Test
	void providesSingleArgument() {
		var annotation = csvSource("foo");

		var arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("foo"));
	}

	@Test
	void providesSingleArgumentFromTextBlock() {
		var annotation = csvSource().textBlock("foo").build();

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
	void providesMultipleArgumentsFromTextBlock() {
		var annotation = csvSource().textBlock("""
				foo
				bar
				""").build();

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
	void ignoresLeadingAndTrailingSpaces() {
		var annotation = csvSource().lines("1,a", "2, b", "3,c ", "4, d ") //
				.ignoreLeadingAndTrailingWhitespace(false).build();

		var arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(
			new Object[][] { { "1", "a" }, { "2", " b" }, { "3", "c " }, { "4", " d " } });
	}

	@Test
	void understandsQuotes() {
		var annotation = csvSource("'foo, bar'");

		var arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("foo, bar"));
	}

	@Test
	void understandsQuotesInTextBlock() {
		var annotation = csvSource().textBlock("""
				'foo, bar'
				""").build();

		var arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("foo, bar"));
	}

	@Test
	void understandsCustomQuotes() {
		var annotation = csvSource().quoteCharacter('~').lines("~foo, bar~").build();

		var arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("foo, bar"));
	}

	@Test
	void understandsCustomQuotesInTextBlock() {
		var annotation = csvSource().quoteCharacter('"').textBlock("""
					"foo, bar"
				""").build();

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
	void understandsEscapeCharactersWithCutomQuoteCharacter() {
		var annotation = csvSource().quoteCharacter('~').lines("~foo or ~~bar~~~, baz").build();

		var arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("foo or ~bar~", "baz"));
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
		var annotation = csvSource().nullValues("N/A", "NIL", "null")//
				.lines("apple, , NIL, '', N/A, banana, null").build();

		var arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("apple", null, null, "", null, "banana", null));
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

		assertThatExceptionOfType(CsvParsingException.class)//
				.isThrownBy(() -> provideArguments(annotation))//
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

		assertThatExceptionOfType(CsvParsingException.class)//
				.isThrownBy(() -> provideArguments(annotation))//
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

	@Test
	void ignoresCommentCharacterWhenUsingValueAttribute() {
		var annotation = csvSource("#foo", "#bar,baz", "baz,#quux");

		var arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("#foo"), array("#bar", "baz"), array("baz", "#quux"));
	}

	@Test
	void honorsCommentCharacterWhenUsingTextBlockAttribute() {
		var annotation = csvSource().textBlock("""
				#foo
				bar, #baz
				'#bar', baz
				""").build();

		var arguments = provideArguments(annotation);

		assertThat(arguments).containsExactly(array("bar", "#baz"), array("#bar", "baz"));
	}

	@Test
	void supportsCsvHeadersWhenUsingTextBlockAttribute() {
		supportsCsvHeaders(csvSource().useHeadersInDisplayName(true).textBlock("""
				FRUIT, RANK
				apple, 1
				banana, 2
				""").build());
	}

	@Test
	void supportsCsvHeadersWhenUsingValueAttribute() {
		supportsCsvHeaders(csvSource().useHeadersInDisplayName(true)//
				.lines("FRUIT, RANK", "apple, 1", "banana, 2").build());
	}

	private void supportsCsvHeaders(CsvSource csvSource) {
		var arguments = provideArguments(csvSource);
		Stream<String[]> argumentsAsStrings = arguments.map(array -> {
			String[] strings = new String[array.length];
			for (int i = 0; i < array.length; i++) {
				strings[i] = String.valueOf(array[i]);
			}
			return strings;
		});

		assertThat(argumentsAsStrings).containsExactly(array("FRUIT = apple", "RANK = 1"),
			array("FRUIT = banana", "RANK = 2"));
	}

	@Test
	void throwsExceptionIfColumnCountExceedsHeaderCount() {
		var annotation = csvSource().useHeadersInDisplayName(true).textBlock("""
				FRUIT, RANK
				apple, 1
				banana, 2, BOOM!
				""").build();

		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(() -> provideArguments(annotation))//
				.withMessage(
					"The number of columns (3) exceeds the number of supplied headers (2) in CSV record: [banana, 2, BOOM!]");
	}

	private Stream<Object[]> provideArguments(CsvSource annotation) {
		var provider = new CsvArgumentsProvider();
		provider.accept(annotation);
		return provider.provideArguments(mock()).map(Arguments::get);
	}

	@SuppressWarnings("unchecked")
	private static <T> T[] array(T... elements) {
		return elements;
	}

}
