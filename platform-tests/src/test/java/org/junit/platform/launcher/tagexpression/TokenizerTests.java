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

import java.util.List;

import org.junit.jupiter.api.Test;

class TokenizerTests {

	@Test
	void nullContainsNoTokens() {
		assertThat(tokensExtractedFrom(null)).isEmpty();
	}

	@Test
	void removeLeadingAndTrailingSpaces() {
		assertThat(tokensExtractedFrom(" tag ")).containsExactly("tag");
	}

	@Test
	void notIsAReservedKeyword() {
		assertThat(tokensExtractedFrom("! tag")).containsExactly("!", "tag");
		assertThat(tokensExtractedFrom("!tag")).containsExactly("!", "tag");
	}

	@Test
	void andIsAReservedKeyword() {
		assertThat(tokensExtractedFrom("one & two")).containsExactly("one", "&", "two");
		assertThat(tokensExtractedFrom("one&two")).containsExactly("one", "&", "two");
	}

	@Test
	void orIsAReservedKeyword() {
		assertThat(tokensExtractedFrom("one | two")).containsExactly("one", "|", "two");
		assertThat(tokensExtractedFrom("one|two")).containsExactly("one", "|", "two");
	}

	@Test
	void discoverBrackets() {
		assertThat(tokensExtractedFrom("()")).containsExactly("(", ")");
		assertThat(tokensExtractedFrom("(tag)")).containsExactly("(", "tag", ")");
		assertThat(tokensExtractedFrom("( tag )")).containsExactly("(", "tag", ")");
		assertThat(tokensExtractedFrom("( foo &bar)| (baz& qux )")).containsExactly("(", "foo", "&", "bar", ")", "|",
			"(", "baz", "&", "qux", ")");
	}

	private List<String> tokensExtractedFrom(String expression) {
		return new Tokenizer().tokenize(expression);
	}
}
