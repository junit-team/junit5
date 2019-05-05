/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.tagexpression;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.platform.engine.TestTag.create;
import static org.junit.platform.launcher.tagexpression.TagExpressions.and;
import static org.junit.platform.launcher.tagexpression.TagExpressions.not;
import static org.junit.platform.launcher.tagexpression.TagExpressions.or;
import static org.junit.platform.launcher.tagexpression.TagExpressions.tag;

import java.util.Collections;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.engine.TestTag;

class TagExpressionsTests {

	private static final TagExpression True = tags -> true;
	private static final TagExpression False = tags -> false;

	@Test
	void tagIsJustATestTag() {
		assertThat(tag("foo")).hasToString("foo");
	}

	@Test
	void rejectInvalidTestTags() {
		RuntimeException expected = assertThrows(PreconditionViolationException.class,
			() -> tag("tags with spaces are not allowed"));
		assertThat(expected).hasMessageContaining("tags with spaces are not allowed");
	}

	@Test
	void tagEvaluation() {
		TagExpression tagExpression = tag("foo");

		assertThat(tagExpression.evaluate(singleton(create("foo")))).isTrue();
		assertThat(tagExpression.evaluate(singleton(create("not_foo")))).isFalse();
	}

	@Test
	void justConcatenateNot() {
		assertThat(not(tag("foo"))).hasToString("!foo");
		assertThat(not(and(tag("foo"), tag("bar")))).hasToString("!(foo & bar)");
		assertThat(not(or(tag("foo"), tag("bar")))).hasToString("!(foo | bar)");
	}

	@Test
	void notEvaluation() {
		assertThat(not(True).evaluate(anyTestTags())).isFalse();
		assertThat(not(False).evaluate(anyTestTags())).isTrue();
	}

	@Test
	void encloseAndWithParenthesis() {
		assertThat(and(tag("foo"), tag("bar"))).hasToString("(foo & bar)");
	}

	@Test
	void andEvaluation() {
		assertThat(and(True, True).evaluate(anyTestTags())).isTrue();
		assertThat(and(True, False).evaluate(anyTestTags())).isFalse();
		assertThat(and(False, onEvaluateThrow("should not be evaluated")).evaluate(anyTestTags())).isFalse();
	}

	@Test
	void encloseOrWithParenthesis() {
		assertThat(or(tag("foo"), tag("bar"))).hasToString("(foo | bar)");
	}

	@Test
	void orEvaluation() {
		assertThat(or(False, False).evaluate(anyTestTags())).isFalse();
		assertThat(or(True, onEvaluateThrow("should not be evaluated")).evaluate(anyTestTags())).isTrue();
		assertThat(or(False, True).evaluate(anyTestTags())).isTrue();
	}

	private TagExpression onEvaluateThrow(String message) {
		return tags -> {
			throw new RuntimeException(message);
		};
	}

	private static Set<TestTag> anyTestTags() {
		return Collections.emptySet();
	}
}
