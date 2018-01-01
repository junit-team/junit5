/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.tagexpression;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ParserErrorTests {

	private final Parser parser = new Parser();

	@Test
	void cantParseExpressionFromNull() {
		assertThat(parseErrorFromParsing(null)).contains("empty tag expression");
	}

	@Test
	void emptyExpression() {
		assertThat(parseErrorFromParsing("")).contains("empty tag expression");
	}

	@Test
	void missingClosingParenthesis() {
		assertThat(parseErrorFromParsing("(")).contains("( at <0> missing closing parenthesis");
		assertThat(parseErrorFromParsing("( foo & bar")).contains("( at <0> missing closing parenthesis");
	}

	@Test
	void missingOpeningParenthesis() {
		assertThat(parseErrorFromParsing(")")).contains(") at <0> missing opening parenthesis");
		assertThat(parseErrorFromParsing(" foo | bar)")).contains(") at <3> missing opening parenthesis");
	}

	@Test
	void partialUnaryOperator() {
		assertThat(parseErrorFromParsing("!")).contains("! at <0> missing rhs operand");
	}

	@Test
	void partialBinaryOperator() {
		assertThat(parseErrorFromParsing("& foo")).contains("& at <0> missing lhs operand");
		assertThat(parseErrorFromParsing("foo |")).contains("| at <1> missing rhs operand");
	}

	@ParameterizedTest
	@MethodSource("data")
	void acceptanceTests(String tagExpression, String parseError) {
		assertThat(parseErrorFromParsing(tagExpression)).contains(parseError);
	}

	private static Stream<Arguments> data() {
		// @formatter:off
		return Stream.of(
				Arguments.of("&", "& at <0> missing lhs and rhs operand"),
				Arguments.of("|", "| at <0> missing lhs and rhs operand"),
				Arguments.of("| |", "| at <0> missing lhs and rhs operand"),
				Arguments.of("!", "! at <0> missing rhs operand"),
				Arguments.of("foo bar", "missing operator"),
				Arguments.of("foo bar |", "| at <2> missing rhs operand"),
				Arguments.of("foo bar &", "& at <2> missing rhs operand"),
				Arguments.of("foo & (bar !)", "! at <4> missing rhs operand"),
				Arguments.of("( foo & bar ) )", ") at <5> missing opening parenthesis"),
				Arguments.of("( ( foo & bar )", "( at <0> missing closing parenthesis"),

				Arguments.of("foo & (bar baz) |", "missing operator between bar <3> and baz <4>"),

				Arguments.of("foo & (bar baz) &", "missing operator between bar <3> and baz <4>"),
				Arguments.of("foo & (bar |baz) &", "& at <7> missing rhs operand"),

				Arguments.of("foo | (bar baz) &", "& at <6> missing rhs operand"),
				Arguments.of("foo | (bar baz) &quux", "missing operator between bar <3> and (baz & quux) <6>"),

				Arguments.of("foo & |", "& at <1> missing rhs operand"),
				Arguments.of("foo !& bar", "! at <1> missing rhs operand"),
				Arguments.of("foo !| bar", "! at <1> missing rhs operand")
		);
		// @formatter:on
	}

	private String parseErrorFromParsing(String tagExpression) {
		try {
			ParseResult parseResult = parser.parse(tagExpression);
			parseResult.expressionOrThrow(RuntimeException::new);
			return null;
		}
		catch (RuntimeException ex) {
			return ex.getMessage();
		}
	}
}
