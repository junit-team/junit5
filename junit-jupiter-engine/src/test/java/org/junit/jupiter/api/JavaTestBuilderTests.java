/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.TestBuilder.testOf;
import static org.junit.jupiter.api.TestCaseBuilder.caseOf;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

import org.junit.jupiter.api.function.ThrowingConsumer;

// @formatter:off

/**
 * Java unit tests for {@link TestBuilder}.
 */
class JavaTestBuilderTests {
	private static final ThrowingConsumer<Object> NOOP = it -> {};

	@SuppressWarnings("unchecked")
	private static <T> ThrowingConsumer<T> noop() {
		return (ThrowingConsumer<T>) NOOP;
	}

	private static void check(final TestCases testCases, final String a, final String b) {
		assertAll(
			() -> assertTrue(testCases.hasNext()),
			() -> assertEquals(a, testCases.next().getDisplayName()),
			() -> assertTrue(testCases.hasNext()),
			() -> assertEquals(b, testCases.next().getDisplayName()),
			() -> assertFalse(testCases.hasNext()),
			() -> assertThrows(NoSuchElementException.class, testCases::next)
		);
	}

	private static void check(final TestCases testCases) {
		check(testCases, "[1] a", "[2] b");
	}

	@Test
	void testOfStream() {
		check(testOf(Stream.of('a', 'b'), noop()));
	}

	@Test
	void testOfArray() {
		check(testOf(new String[] { "a", "b" }, noop()));
	}

	@Test
	void testOfBooleanArray() {
		check(testOf(new boolean[] { true, false }, noop()), "[1] true", "[2] false");
	}

	@Test
	void testOfByteArray() {
		check(testOf(new byte[] { 0b0, 0b1 }, noop()), "[1] 0", "[2] 1");
	}

	@Test
	void testOfCharArray() {
		check(testOf(new char[] { 'a', 'b' }, noop()));
	}

	@Test
	void testOfDoubleArray() {
		check(testOf(new double[] { 1.1, 2.2 }, noop()), "[1] 1.1", "[2] 2.2");
	}

	@Test
	void testOfFloatArray() {
		check(testOf(new float[] { 1.1F, 2.2F }, noop()), "[1] 1.1", "[2] 2.2");
	}

	@Test
	void testOfIntArray() {
		check(testOf(new int[] { 1, 2 }, noop()), "[1] 1", "[2] 2");
	}

	@Test
	void testOfLongArray() {
		check(testOf(new long[] { 1L, 2L }, noop()), "[1] 1", "[2] 2");
	}

	@Test
	void testOfShortArray() {
		check(testOf(new short[] { 1, 2 }, noop()), "[1] 1", "[2] 2");
	}

	@Test
	void testOfIterable() {
		check(testOf(Arrays.asList('a', 'b'), noop()));
	}

	@Test
	void testOfIterator() {
		check(testOf(Stream.of('a', 'b').iterator(), noop()));
	}

	enum E {
		a, b
	}

	@Test
	void testOfEnum() {
		check(testOf(E.class, noop()));
	}

	@Test
	void testOf1() {
		check(testOf(noop(), caseOf('a'), caseOf('b')));
	}

	@Test
	void testOf2() {
		check(
			testOf(
				(p1, p2) -> {},
				caseOf('a', 'a'),
				caseOf('b', 'b')
			),
			"[1] a, a",
			"[2] b, b"
		);
	}

	@Test
	void testOf3() {
		check(
			testOf(
				(p1, p2, p3) -> {},
				caseOf('a', 'a', 'a'),
				caseOf('b', 'b', 'b')
			),
			"[1] a, a, a",
			"[2] b, b, b"
		);
	}

	@Test
	void testOf4() {
		check(
			testOf(
				(p1, p2, p3, p4) -> {},
				caseOf('a', 'a', 'a', 'a'),
				caseOf('b', 'b', 'b', 'b')
			),
			"[1] a, a, a, a",
			"[2] b, b, b, b"
		);
	}

	@Test
	void testOf5() {
		check(
			testOf(
				(p1, p2, p3, p4, p5) -> {},
				caseOf('a', 'a', 'a', 'a', 'a'),
				caseOf('b', 'b', 'b', 'b', 'b')
			),
			"[1] a, a, a, a, a",
			"[2] b, b, b, b, b"
		);
	}

	@Test
	void testOf6() {
		check(testOf((p1, p2, p3, p4, p5, p6) -> {
		}, caseOf('a', 'a', 'a', 'a', 'a', 'a'), caseOf('b', 'b', 'b', 'b', 'b', 'b')), "[1] a, a, a, a, a, a",
			"[2] b, b, b, b, b, b");
	}

	@Test
	void testOf7() {
		check(
			testOf(
				(p1, p2, p3, p4, p5, p6, p7) -> {},
				caseOf('a', 'a', 'a', 'a', 'a', 'a', 'a'),
				caseOf('b', 'b', 'b', 'b', 'b', 'b', 'b')
			),
			"[1] a, a, a, a, a, a, a",
			"[2] b, b, b, b, b, b, b"
		);
	}

	@Test
	void testOf8() {
		check(
			testOf(
				(p1, p2, p3, p4, p5, p6, p7, p8) -> {},
				caseOf('a', 'a', 'a', 'a', 'a', 'a', 'a', 'a'),
				caseOf('b', 'b', 'b', 'b', 'b', 'b', 'b', 'b')
			),
			"[1] a, a, a, a, a, a, a, a",
			"[2] b, b, b, b, b, b, b, b"
		);
	}

	@Test
	void testOf9() {
		check(
			testOf(
				(p1, p2, p3, p4, p5, p6, p7, p8, p9) -> {},
				caseOf('a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a'),
				caseOf('b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b')
			),
			"[1] a, a, a, a, a, a, a, a, a",
			"[2] b, b, b, b, b, b, b, b, b"
		);
	}

	@Test
	void testOf10() {
		check(
			testOf(
				(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10) -> {},
				caseOf('a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a'),
				caseOf('b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b')
			),
			"[1] a, a, a, a, a, a, a, a, a, a",
			"[2] b, b, b, b, b, b, b, b, b, b"
		);
	}

	@Test
	void testOf11() {
		check(
			testOf(
				(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11) -> {},
				caseOf('a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a'),
				caseOf('b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b')
			),
			"[1] a, a, a, a, a, a, a, a, a, a, a",
			"[2] b, b, b, b, b, b, b, b, b, b, b"
		);
	}

	@Test
	void testOf12() {
		check(
			testOf(
				(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12) -> {},
				caseOf('a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a'),
				caseOf('b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b')
			),
			"[1] a, a, a, a, a, a, a, a, a, a, a, a",
			"[2] b, b, b, b, b, b, b, b, b, b, b, b"
		);
	}

	@Test
	void testOf13() {
		check(
			testOf(
				(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13) -> {},
				caseOf('a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a'),
				caseOf('b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b')
			),
			"[1] a, a, a, a, a, a, a, a, a, a, a, a, a",
			"[2] b, b, b, b, b, b, b, b, b, b, b, b, b"
		);
	}

	@Test
	void testOf14() {
		check(
			testOf(
				(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14) -> {},
				caseOf('a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a'),
				caseOf('b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b')
			),
			"[1] a, a, a, a, a, a, a, a, a, a, a, a, a, a",
			"[2] b, b, b, b, b, b, b, b, b, b, b, b, b, b"
		);
	}

	@Test
	void testOf15() {
		check(
			testOf(
				(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15) -> {},
				caseOf('a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a'),
				caseOf('b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b')
			),
			"[1] a, a, a, a, a, a, a, a, a, a, a, a, a, a, a",
			"[2] b, b, b, b, b, b, b, b, b, b, b, b, b, b, b"
		);
	}

	@Test
	void testOf16() {
		check(
			testOf(
				(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16) -> {},
				caseOf('a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a'),
				caseOf('b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b')
			),
			"[1] a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a",
			"[2] b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b"
		);
	}

	@Test
	void testOf17() {
		check(
			testOf(
				(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17) -> {},
				caseOf('a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a'),
				caseOf('b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b')
			),
			"[1] a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a",
			"[2] b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b"
		);
	}

	@Test
	void testOf18() {
		check(
			testOf(
				(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18) -> {},
				caseOf('a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a'),
				caseOf('b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b')
			),
			"[1] a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a",
			"[2] b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b"
		);
	}

	@Test
	void testOf19() {
		check(
			testOf(
				(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19) -> {},
				caseOf('a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a'),
				caseOf('b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b')
			),
			"[1] a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a",
			"[2] b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b"
		);
	}

	@Test
	void testOf20() {
		check(testOf((p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20) -> {
		}, caseOf('a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a'),
			caseOf('b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b')),
			"[1] a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a",
			"[2] b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b");
	}

	@Test
	void testOf21() {
		check(
			testOf((p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21) -> {
			}, caseOf('a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a',
				'a', 'a'),
				caseOf('b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b',
					'b', 'b')),
			"[1] a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a",
			"[2] b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b");
	}

	@Test
	void testOf22() {
		check(testOf(
			(p1, p2, p3, p4, p5, p6, p7, p8, p9, p10, p11, p12, p13, p14, p15, p16, p17, p18, p19, p20, p21, p22) -> {
			},
			caseOf('a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a', 'a',
				'a', 'a'),
			caseOf('b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b', 'b',
				'b', 'b')),
			"[1] a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a, a",
			"[2] b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b, b");
	}

	@Test
	void testOfWithCustomCounter() {
		check(testOf(noop(), caseOf('a'), caseOf('b')).counter(i -> "#" + i + " "), "#1 a", "#2 b");
	}

	@Test
	void testOfWithFixedCounter() {
		check(testOf(noop(), caseOf('a'), caseOf('b')).counter("… "), "… a", "… b");
	}

	@Test
	void testOfWithoutCounter() {
		check(testOf(noop(), caseOf('a'), caseOf('b')).uncounted(), "a", "b");
	}

	@Test
	void testOfWithCustomNaming() {
		check(testOf(noop(), caseOf('a'), caseOf('b')).named(it -> Character.toString(Character.toUpperCase(it.p1))),
			"[1] A", "[2] B");
	}

	@Test
	void testOfWithFixedNaming() {
		check(testOf(noop(), caseOf('a'), caseOf('b')).named("…"), "[1] …", "[2] …");
	}

	@Test
	void testOfWithoutName() {
		check(testOf(noop(), caseOf('a'), caseOf('b')).unnamed(), "[1] ", "[2] ");
	}

	@Test
	void testOfWithCustomCounterAndName() {
		check(testOf(noop(), caseOf('a'), caseOf('b')).counter(i -> "#" + i).named(
			it -> Character.toString(Character.toUpperCase(it.p1))), "#1A", "#2B");
	}

	@Test
	void testOfWithCustomCaseNames() {
		check(testOf(noop(), caseOf('a').named("AA"), caseOf('b').named("BB")), "[1] AA", "[2] BB");
	}

	@Test
	void testOfWithCustomNameAndCustomCaseName() {
		check(testOf(noop(), caseOf("").named("EMPTY"), caseOf(" ")).named(it -> Character.getName(it.p1.charAt(0))),
			"[1] EMPTY", "[2] SPACE");
	}
}
