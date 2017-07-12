/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.launcher;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class TokenizerTest {

	private static final String illegalCharacter = "|";

	@Test
	void removeLeadingAndTrailingSpaces() {
		assertThat(tokensExtractedFrom(" tag ")).containsExactly("tag");
	}

	@Test
	void upperAndLowerChaseCharactersAreAllowed() {
		assertThat(tokensExtractedFrom("AbyZ")).containsExactly("AbyZ");
	}

	@Test
	void tagsCanHaveSpaceCharacters() {
		assertThat(tokensExtractedFrom("tag with  spaces")).containsExactly("tag with  spaces");
		assertThat(tokensExtractedFrom("(  tag with spaces )  ")).containsExactly("(", "tag with spaces", ")");
	}

	@Test
	void andIsAReservedKeyword() {
		assertThat(tokensExtractedFrom("one and two")).containsExactly("one", "and", "two");
		assertThat(tokensExtractedFrom("andtag")).containsExactly("andtag");
		assertThat(tokensExtractedFrom("oneand two")).containsExactly("oneand two");
		assertThat(tokensExtractedFrom("one andtwo")).containsExactly("one andtwo");
	}

	@Test
	void orIsAReservedKeyword() {
		assertThat(tokensExtractedFrom("one or two")).containsExactly("one", "or", "two");
		assertThat(tokensExtractedFrom("ortag")).containsExactly("ortag");
		assertThat(tokensExtractedFrom("oneor two")).containsExactly("oneor two");
		assertThat(tokensExtractedFrom("one ortwo")).containsExactly("one ortwo");
	}

	@Test
	void notIsAReservedKeyword() {
		assertThat(tokensExtractedFrom("not tag")).containsExactly("not", "tag");
		assertThat(tokensExtractedFrom("nottag")).containsExactly("nottag");
	}

	@Test
	void discoverBrackets() {
		assertThat(tokensExtractedFrom("()")).containsExactly("(", ")");
		assertThat(tokensExtractedFrom("(tag)")).containsExactly("(", "tag", ")");
		assertThat(tokensExtractedFrom("( tag )")).containsExactly("(", "tag", ")");
		assertThat(tokensExtractedFrom("( a and b) or ( c and d )")).containsExactly("(", "a", "and", "b", ")", "or",
			"(", "c", "and", "d", ")");
	}

	@ParameterizedTest
	@MethodSource("data")
	void acceptanceTests(String tagExpression, List<String> tokens) {
		assertThat(tokensExtractedFrom(tagExpression)).containsExactly(tokens.toArray(new String[0]));
	}

	public static Stream<Arguments> data() {
		return Stream.of(Arguments.of("a and b", asList("a", "and", "b")),
			Arguments.of("a or b", asList("a", "or", "b")), Arguments.of("not a", asList("not", "a")),
			Arguments.of("not a or b and not c or not d or e and f",
				asList("not", "a", "or", "b", "and", "not", "c", "or", "not", "d", "or", "e", "and", "f")));
	}

	private List<String> tokensExtractedFrom(String expression) {
		//return new Tokenizer().tokenize(expression);
		return new Tokenizer().tokenizeWithPostProcessing(expression);
	}
}
