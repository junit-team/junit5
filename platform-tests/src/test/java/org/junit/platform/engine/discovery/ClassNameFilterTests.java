/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.PreconditionViolationException;

/**
 * @since 1.0
 */
class ClassNameFilterTests {

	@Test
	void includeClassNamePatternsChecksPreconditions() {
		assertThatThrownBy(() -> ClassNameFilter.includeClassNamePatterns((String[]) null)) //
				.isInstanceOf(PreconditionViolationException.class) //
				.hasMessage("patterns must not be null or empty");
		assertThatThrownBy(() -> ClassNameFilter.includeClassNamePatterns(new String[0])) //
				.isInstanceOf(PreconditionViolationException.class) //
				.hasMessage("patterns must not be null or empty");
		assertThatThrownBy(() -> ClassNameFilter.includeClassNamePatterns(new String[] { null })) //
				.isInstanceOf(PreconditionViolationException.class) //
				.hasMessage("patterns must not contain null elements");
	}

	@Test
	void includeClassNamePatternsWithSinglePattern() {
		String regex = "^java\\.lang\\..*";

		ClassNameFilter filter = ClassNameFilter.includeClassNamePatterns(regex);

		assertThat(filter).hasToString("Includes class names that match regular expression '" + regex + "'");

		assertTrue(filter.apply("java.lang.String").included());
		assertTrue(filter.toPredicate().test("java.lang.String"));
		assertThat(filter.apply("java.lang.String").getReason()).contains(
			"Class name [java.lang.String] matches included pattern: '" + regex + "'");

		assertFalse(filter.apply("java.time.Instant").included());
		assertFalse(filter.toPredicate().test("java.time.Instant"));
		assertThat(filter.apply("java.time.Instant").getReason()).contains(
			"Class name [java.time.Instant] does not match any included pattern: '" + regex + "'");
	}

	@Test
	void includeClassNamePatternsWithMultiplePatterns() {
		String firstRegex = "^java\\.lang\\..*";
		String secondRegex = "^java\\.util\\..*";

		ClassNameFilter filter = ClassNameFilter.includeClassNamePatterns(firstRegex, secondRegex);

		assertThat(filter).hasToString(
			"Includes class names that match regular expression '" + firstRegex + "' OR '" + secondRegex + "'");

		assertTrue(filter.apply("java.lang.String").included());
		assertTrue(filter.toPredicate().test("java.lang.String"));
		assertThat(filter.apply("java.lang.String").getReason()).contains(
			"Class name [java.lang.String] matches included pattern: '" + firstRegex + "'");

		assertTrue(filter.apply("java.util.Collection").included());
		assertTrue(filter.toPredicate().test("java.util.Collection"));
		assertThat(filter.apply("java.util.Collection").getReason()).contains(
			"Class name [java.util.Collection] matches included pattern: '" + secondRegex + "'");

		assertFalse(filter.apply("java.time.Instant").included());
		assertFalse(filter.toPredicate().test("java.time.Instant"));
		assertThat(filter.apply("java.time.Instant").getReason()).contains(
			"Class name [java.time.Instant] does not match any included pattern: '" + firstRegex + "' OR '"
					+ secondRegex + "'");
	}

}
