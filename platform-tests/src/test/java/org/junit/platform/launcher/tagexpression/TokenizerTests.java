/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.tagexpression;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class TokenizerTests {

	@Test
	void nullContainsNoTokens() {
		assertThat(tokenStringsExtractedFrom(null)).isEmpty();
	}

	@Test
	void removeLeadingAndTrailingSpaces() {
		assertThat(tokenStringsExtractedFrom(" tag ")).containsExactly("tag");
	}

	@Test
	void notIsAReservedKeyword() {
		assertThat(tokenStringsExtractedFrom("! tag")).containsExactly("!", "tag");
		assertThat(tokenStringsExtractedFrom("!tag")).containsExactly("!", "tag");
	}

	@Test
	void andIsAReservedKeyword() {
		assertThat(tokenStringsExtractedFrom("one & two")).containsExactly("one", "&", "two");
		assertThat(tokenStringsExtractedFrom("one&two")).containsExactly("one", "&", "two");
	}

	@Test
	void orIsAReservedKeyword() {
		assertThat(tokenStringsExtractedFrom("one | two")).containsExactly("one", "|", "two");
		assertThat(tokenStringsExtractedFrom("one|two")).containsExactly("one", "|", "two");
	}

	@Test
	void anyAndNoneAreReservedKeywords() {
		assertThat(tokenStringsExtractedFrom("!(any())")).containsExactly("!", "(", "any()", ")");
		assertThat(tokenStringsExtractedFrom("!(none())")).containsExactly("!", "(", "none()", ")");
	}

	@Test
	void discoverBrackets() {
		assertThat(tokenStringsExtractedFrom("()")).containsExactly("(", ")");
		assertThat(tokenStringsExtractedFrom("(tag)")).containsExactly("(", "tag", ")");
		assertThat(tokenStringsExtractedFrom("( tag )")).containsExactly("(", "tag", ")");
		assertThat(tokenStringsExtractedFrom("( foo &bar)| (baz& qux )")).containsExactly("(", "foo", "&", "bar", ")",
			"|", "(", "baz", "&", "qux", ")");
	}

	@Test
	void extractRawStringWithSpaceCharactersBeforeTheToken() {
		assertThat(rawStringsExtractedFrom("(")).containsExactly("(");
		assertThat(rawStringsExtractedFrom("  (")).containsExactly("  (");
		assertThat(rawStringsExtractedFrom("  ( foo ")).containsExactly("  (", " foo");
		assertThat(rawStringsExtractedFrom("(( ((   (")).containsExactly("(", "(", " (", "(", "   (");
	}

	@Test
	void extractStartPositionOfRawString() {
		assertThat(startIndicesExtractedFrom("(")).containsExactly(0);
		assertThat(startIndicesExtractedFrom("  (  (")).containsExactly(0, 3);
		assertThat(startIndicesExtractedFrom("foo &!bar")).containsExactly(0, 3, 5, 6);
	}

	private Stream<Integer> startIndicesExtractedFrom(String expression) {
		return tokensExtractedFrom(expression).map(token -> token.startIndex);
	}

	private Stream<String> rawStringsExtractedFrom(String expression) {
		return tokensExtractedFrom(expression).map(token -> token.rawString);
	}

	private List<String> tokenStringsExtractedFrom(String expression) {
		return tokensExtractedFrom(expression).map(Token::string).collect(toList());
	}

	private Stream<Token> tokensExtractedFrom(String expression) {
		return new Tokenizer().tokenize(expression).stream();
	}
}
