/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.commons.util;

import static java.util.function.Predicate.isEqual;
import static org.junit.gen5.api.Assertions.*;

import java.util.function.Predicate;

import org.junit.gen5.api.Test;

class FunctionUtilsTests {

	@Test
	void whereWithNullFunction() {
		NullPointerException exception = expectThrows(NullPointerException.class, () -> {
			FunctionUtils.where(null, o -> true);
		});
		assertEquals("function must not be null", exception.getMessage());
	}

	@Test
	void whereWithNullPredicate() {
		NullPointerException exception = expectThrows(NullPointerException.class, () -> {
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
}
