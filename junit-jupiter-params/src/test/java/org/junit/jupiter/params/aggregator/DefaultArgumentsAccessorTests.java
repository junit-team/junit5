/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.aggregator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * Unit tests for {@link DefaultArgumentsAccessor}.
 *
 * @since 5.2
 */
class DefaultArgumentsAccessorTests {

	@Test
	void argumentsMustNotBeNull() {
		assertThrows(PreconditionViolationException.class, () -> new DefaultArgumentsAccessor((Object[]) null));
	}

	@Test
	void indexMustNotBeNegative() {
		ArgumentsAccessor arguments = new DefaultArgumentsAccessor(1, 2);
		Exception exception = assertThrows(PreconditionViolationException.class, () -> arguments.get(-1));
		assertThat(exception.getMessage()).containsSubsequence("index must be", ">= 0");
	}

	@Test
	void indexMustBeSmallerThanLength() {
		ArgumentsAccessor arguments = new DefaultArgumentsAccessor(1, 2);
		Exception exception = assertThrows(PreconditionViolationException.class, () -> arguments.get(2));
		assertThat(exception.getMessage()).containsSubsequence("index must be", "< 2");
	}

	@Test
	void getNull() {
		assertNull(new DefaultArgumentsAccessor(new Object[] { null }).get(0));
	}

	@Test
	void getWithNullCastToWrapperType() {
		assertNull(new DefaultArgumentsAccessor((Object[]) new Integer[] { null }).get(0, Integer.class));
	}

	@Test
	void get() {
		assertEquals(1, new DefaultArgumentsAccessor(1).get(0));
	}

	@Test
	void getWithCast() {
		assertEquals(Integer.valueOf(1), new DefaultArgumentsAccessor(1).get(0, Integer.class));
		assertEquals(Character.valueOf('A'), new DefaultArgumentsAccessor('A').get(0, Character.class));
	}

	@Test
	void getWithCastToPrimitiveType() {
		Exception exception = assertThrows(ArgumentAccessException.class,
			() -> new DefaultArgumentsAccessor(1).get(0, int.class));
		assertThat(exception.getMessage()).isEqualTo(
			"Argument at index [0] with value [1] and type [java.lang.Integer] could not be converted or cast to type [int].");

		exception = assertThrows(ArgumentAccessException.class,
			() -> new DefaultArgumentsAccessor(new Object[] { null }).get(0, int.class));
		assertThat(exception.getMessage()).isEqualTo(
			"Argument at index [0] with value [null] and type [null] could not be converted or cast to type [int].");
	}

	@Test
	void getWithCastToIncompatibleType() {
		Exception exception = assertThrows(ArgumentAccessException.class,
			() -> new DefaultArgumentsAccessor(1).get(0, Character.class));
		assertThat(exception.getMessage()).isEqualTo(
			"Argument at index [0] with value [1] and type [java.lang.Integer] could not be converted or cast to type [java.lang.Character].");
	}

	@Test
	void getCharacter() {
		assertEquals(Character.valueOf('A'), new DefaultArgumentsAccessor('A', 'B').getCharacter(0));
	}

	@Test
	void getBoolean() {
		assertEquals(Boolean.TRUE, new DefaultArgumentsAccessor(true, false).getBoolean(0));
	}

	@Test
	void getByte() {
		assertEquals(Byte.valueOf((byte) 42), new DefaultArgumentsAccessor((byte) 42).getByte(0));
	}

	@Test
	void getShort() {
		assertEquals(Short.valueOf((short) 42), new DefaultArgumentsAccessor((short) 42).getShort(0));
	}

	@Test
	void getInteger() {
		assertEquals(Integer.valueOf(42), new DefaultArgumentsAccessor(42).getInteger(0));
	}

	@Test
	void getLong() {
		assertEquals(Long.valueOf(42L), new DefaultArgumentsAccessor(42L).getLong(0));
	}

	@Test
	void getFloat() {
		assertEquals(Float.valueOf(42.0f), new DefaultArgumentsAccessor(42.0f).getFloat(0));
	}

	@Test
	void getDouble() {
		assertEquals(Double.valueOf(42.0), new DefaultArgumentsAccessor(42.0).getDouble(0));
	}

	@Test
	void getString() {
		assertEquals("foo", new DefaultArgumentsAccessor("foo", "bar").getString(0));
	}

	@Test
	void toArray() {
		DefaultArgumentsAccessor arguments = new DefaultArgumentsAccessor("foo", "bar");
		Object[] copy = arguments.toArray();
		assertArrayEquals(new String[] { "foo", "bar" }, copy);

		// Modify local copy:
		copy[0] = "Boom!";
		assertEquals("foo", arguments.toArray()[0]);
	}

	@Test
	void toList() {
		DefaultArgumentsAccessor arguments = new DefaultArgumentsAccessor("foo", "bar");
		List<Object> copy = arguments.toList();
		assertIterableEquals(Arrays.asList("foo", "bar"), copy);

		// Modify local copy:
		assertThrows(UnsupportedOperationException.class, () -> copy.set(0, "Boom!"));
	}

	@Test
	void size() {
		assertEquals(0, new DefaultArgumentsAccessor().size());
		assertEquals(1, new DefaultArgumentsAccessor(42).size());
		assertEquals(5, new DefaultArgumentsAccessor('a', 'b', 'c', 'd', 'e').size());
	}

}
