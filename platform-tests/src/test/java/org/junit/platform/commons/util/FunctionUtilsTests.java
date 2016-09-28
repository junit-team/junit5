/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.commons.util;

import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static java.util.function.Predicate.isEqual;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.expectThrows;

/**
 * Unit tests for {@link FunctionUtils}.
 *
 * @since 1.0
 */
class FunctionUtilsTests {

	// whereWith

	@Test
	void whereWithNullFunction() {
		PreconditionViolationException exception = expectThrows(PreconditionViolationException.class, () -> {
			FunctionUtils.where(null, o -> true);
		});
		assertEquals("function must not be null", exception.getMessage());
	}

	@Test
	void whereWithNullPredicate() {
		PreconditionViolationException exception = expectThrows(PreconditionViolationException.class, () -> {
			FunctionUtils.where(o -> o, null);
		});
		assertEquals("predicate must not be null", exception.getMessage());
	}

	@Test
	void whereWithChecksPredicateAgainstResultOfFunction() {
		Predicate<String> combinedPredicate = FunctionUtils.where(String::length, isEqual(3));
		assertFalse(combinedPredicate.test("fo"));
		assertTrue(combinedPredicate.test("foo"));
		assertFalse(combinedPredicate.test("fooo"));
	}

	// firstPresent

	@Test
	@SuppressWarnings("unchecked")
	void firstPresentWithNullSupplierArray() {
		PreconditionViolationException exception = expectThrows(
				PreconditionViolationException.class,
				() -> FunctionUtils.firstPresent((Supplier[]) null));
		assertEquals("suppliers must not be null", exception.getMessage());
	}

	@Test
	void firstPresentWithNullSupplier() {
		PreconditionViolationException exception = expectThrows(
				PreconditionViolationException.class,
				() -> FunctionUtils.firstPresent(Optional::empty, null));
		assertEquals("suppliers must not be null", exception.getMessage());
	}

	@Test
	void firstPresentWithNullProducingSupplier() {
		PreconditionViolationException exception = expectThrows(
				PreconditionViolationException.class,
				() -> FunctionUtils.firstPresent(() -> null));
		assertEquals("supplied optional must not be null", exception.getMessage());
	}

	@Test
	void firstPresentWithEmptyOptional() {
		Optional<String> firstPresent = FunctionUtils.firstPresent(Optional::empty);
		assertFalse(firstPresent.isPresent());
	}

	@Test
	void firstPresentWithEmptyOptionals() {
		Optional<String> firstPresent = FunctionUtils.firstPresent(Optional::empty, Optional::empty, Optional::empty);
		assertFalse(firstPresent.isPresent());
	}

	@Test
	void firstPresentWithPresentOptional() {
		Optional<String> only = Optional.of("ONLY");
		Optional<String> firstPresent = FunctionUtils.firstPresent(() -> only);

		assertSame(only, firstPresent);
	}

	@Test
	void firstPresentWithPresentOptionals() {
		Optional<String> first = Optional.of("FIRST");
		Optional<String> firstPresent = FunctionUtils.firstPresent(
				() -> first, () -> Optional.of("SECOND"), () -> Optional.of("THIRD"));

		assertSame(first, firstPresent);
	}

	@Test
	void firstPresentWithDifferentOptionals() {
		Optional<String> first = Optional.of("FIRST");
		Optional<String> firstPresent = FunctionUtils.firstPresent(
				Optional::empty, () -> first, () -> Optional.of("SECOND"), Optional::empty, () -> Optional.of("THIRD"));

		assertSame(first, firstPresent);
	}

}
