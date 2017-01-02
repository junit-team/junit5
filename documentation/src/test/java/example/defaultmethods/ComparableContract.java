/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package example.defaultmethods;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

// tag::user_guide[]
public interface ComparableContract<T extends Comparable<T>> extends Testable<T> {

	T createSmallerValue();

	@Test
	default void returnsZeroWhenComparedToItself() {
		T value = createValue();
		assertEquals(0, value.compareTo(value));
	}

	@Test
	default void returnsPositiveNumberComparedToSmallerValue() {
		T value = createValue();
		T smallerValue = createSmallerValue();
		assertTrue(value.compareTo(smallerValue) > 0);
	}

	@Test
	default void returnsNegativeNumberComparedToSmallerValue() {
		T value = createValue();
		T smallerValue = createSmallerValue();
		assertTrue(smallerValue.compareTo(value) < 0);
	}

}
// end::user_guide[]
