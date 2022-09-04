/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Spliterator;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;

/**
 * Tests for {@link CombinationsSource}
 */
class CombinationsSourceTest {

	private static final Object NULL = null;
	@SuppressWarnings("StringOperationCanBeSimplified")
	private static final String SUPPLIED_VALUE = new String("supplied");
	private static final Supplier<?> SUPPLIED = () -> SUPPLIED_VALUE;
	private static final Iterator<?> ITERATOR = Arrays.asList(1, 2).iterator();
	private static final Enumeration<?> ENUMERATION = Collections.enumeration(Arrays.asList(1, 2));
	private static final Iterable<?> ITERABLE = Arrays.asList(1, 2);
	private static final Object[] REF_ARRAY = { 1, 2 };
	private static final boolean[] PRIMITIVE_BOOLEAN = { false, true };
	private static final byte[] PRIMITIVE_BYTE = { 1, 2 };
	private static final short[] PRIMITIVE_SHORT = { 1, 2 };
	private static final char[] PRIMITIVE_CHAR = { 1, 2 };
	private static final int[] PRIMITIVE_INT = { 1, 2 };
	private static final long[] PRIMITIVE_LONG = { 1L, 2L };
	private static final float[] PRIMITIVE_FLOAT = { 1.0f, 2.0f };
	private static final double[] PRIMITIVE_DOUBLE = { 1.0, 2.0 };
	private static final Boolean[] BOXED_BOOLEAN = { false, true };
	private static final Byte[] BOXED_BYTE = { 1, 2 };
	private static final Short[] BOXED_SHORT = { 1, 2 };
	private static final Character[] BOXED_CHAR = { 1, 2 };
	private static final Integer[] BOXED_INT = { 1, 2 };
	private static final Long[] BOXED_LONG = { 1L, 2L };
	private static final Float[] BOXED_FLOAT = { 1.0f, 2.0f };
	private static final Double[] BOXED_DOUBLE = { 1.0, 2.0 };

	static Stream<Arguments> stringSourceMethod() {
		return CombinationsSource.fromSources(Collections.singleton("one"), Collections.singleton("two"),
			Collections.singleton("three"));
	}

	@ParameterizedTest
	@MethodSource("stringSourceMethod")
	void valuesCheck(String one, String two, String three) {
		assertThat(one).isEqualTo("one");
		assertThat(two).isEqualTo("two");
		assertThat(three).isEqualTo("three");
	}

	@Test
	void fromOneSource() {
		Stream<Arguments> arguments = CombinationsSource.fromSources(AnEnum.class);
		Arguments[] collected = arguments.toArray(Arguments[]::new);
		assertThat(collected).hasSize(2);
		assertThat(collected[0].get()).containsExactly(AnEnum.FIRST);
		assertThat(collected[1].get()).containsExactly(AnEnum.SECOND);
	}

	@Test
	void fromSingleSource() {
		Stream<Arguments> arguments = CombinationsSource.fromSources(NULL, SUPPLIED, "foo bar");
		Arguments[] collected = arguments.toArray(Arguments[]::new);
		assertThat(collected).hasSize(1);
		assertThat(collected[0].get()).containsExactly(NULL, SUPPLIED_VALUE, "foo bar");
	}

	@Test
	void MultiArgsCheck() {
		// Eight boolean sources will produce 256 combinations which can be compared against their index position.
		Stream<Arguments> arguments = CombinationsSource.fromSources(boolean.class, Boolean.class, boolean.class,
			Boolean.class, boolean.class, Boolean.class, boolean.class, Boolean.class);
		Arguments[] collected = arguments.toArray(Arguments[]::new);
		assertThat(collected).hasSize(1 << 8);
		for (int eachArguments = 0; eachArguments < (1 << 8); eachArguments++) {
			Object[] values = collected[eachArguments].get();
			assertThat(values).hasSize(8);
			for (int argIdx = 0; argIdx < 8; argIdx++) {
				assertThat(values[7 - argIdx]).isExactlyInstanceOf(Boolean.class);
				boolean arg = (Boolean) values[7 - argIdx];
				assertThat(arg).describedAs(
					"arguments " + eachArguments + " values=" + Arrays.toString(values)).isEqualTo(
						((1 << argIdx) & eachArguments) != 0);
			}
		}
	}

	@Test
	void fromPrimitiveArrays() {
		Stream<Arguments> arguments = CombinationsSource.fromSources(PRIMITIVE_BOOLEAN, PRIMITIVE_BYTE, PRIMITIVE_CHAR,
			PRIMITIVE_SHORT, PRIMITIVE_FLOAT, PRIMITIVE_INT, PRIMITIVE_LONG, PRIMITIVE_DOUBLE);

		assertThat(arguments.count()).isEqualTo(1 << 8);
	}

	@Test
	void fromStreams() {
		Stream<Arguments> arguments = CombinationsSource.fromSources(IntStream.of(PRIMITIVE_INT),
			LongStream.of(PRIMITIVE_LONG), DoubleStream.of(PRIMITIVE_DOUBLE), Stream.of(REF_ARRAY));

		assertThat(arguments.count()).isEqualTo(1 << 4);
	}

	@Test
	void fromBoxedArrays() {
		Stream<Arguments> arguments = CombinationsSource.fromSources(BOXED_BOOLEAN, BOXED_BYTE, BOXED_CHAR, BOXED_SHORT,
			BOXED_INT, BOXED_FLOAT, BOXED_LONG, BOXED_DOUBLE);

		assertThat(arguments.count()).isEqualTo(1 << 8);
	}

	@Test
	void fromManySources() {
		Stream<Arguments> arguments = CombinationsSource.fromSources(boolean.class, Boolean.class, AnEnum.class,
			ITERATOR, ENUMERATION, ITERABLE, REF_ARRAY, PRIMITIVE_BOOLEAN, PRIMITIVE_BYTE, PRIMITIVE_CHAR,
			PRIMITIVE_SHORT, PRIMITIVE_FLOAT, PRIMITIVE_INT, PRIMITIVE_LONG, PRIMITIVE_DOUBLE, BOXED_BOOLEAN,
			BOXED_BYTE, BOXED_CHAR, BOXED_SHORT, BOXED_INT, BOXED_FLOAT, BOXED_LONG, BOXED_DOUBLE);

		assertThat(arguments.count()).isEqualTo(1 << 23);
	}

	@Test()
	void emptySource() {
		assertThrowsExactly(IllegalArgumentException.class, () -> {
			Stream<Arguments> arguments = CombinationsSource.fromSources(Collections.emptySet(),
				Collections.emptyList(), Collections.emptyIterator());
			assertThat(arguments.count()).isZero();
		});
	}

	@Test
	void testSplitting() {
		// Eight boolean sources will produce 256 combinations which can be compared against their index position.
		Deque<Spliterator<Object[]>> needsSplit = new ArrayDeque<>();
		needsSplit.push(new CombinationsSource.ArraysSpliterator(true, 3L,
			CombinationsSource.convertSources(true, boolean.class, Boolean.class, boolean.class, Boolean.class,
				boolean.class, Boolean.class, boolean.class, Boolean.class)));
		List<Spliterator<Object[]>> doneSplits = new ArrayList<>();
		do {
			Spliterator<Object[]> didSplit = Objects.requireNonNull(needsSplit.peek()).trySplit();
			if (null != didSplit) {
				needsSplit.push(didSplit);
			}
			else {
				doneSplits.add(needsSplit.pop());
			}
		} while (!needsSplit.isEmpty());
		Object[][] collected = doneSplits.stream().flatMap(aSplit -> StreamSupport.stream(aSplit, false)).toArray(
			Object[][]::new);
		assertThat(collected.length).isEqualTo(1 << 8);
		for (int eachArgument = 0; eachArgument < (1 << 8); eachArgument++) {
			Object[] values = collected[eachArgument];
			assertThat(values).hasSize(8);
			for (int argIdx = 0; argIdx < 8; argIdx++) {
				assertThat(values[7 - argIdx]).isExactlyInstanceOf(Boolean.class);
				boolean arg = (Boolean) values[7 - argIdx];
				assertThat(arg).describedAs(
					"arguments " + eachArgument + " values=" + Arrays.toString(values)).isEqualTo(
						((1 << argIdx) & eachArgument) != 0);
			}
		}
	}

	@Test
	void singleSplit() {
		Deque<Spliterator<Object[]>> needsSplit = new ArrayDeque<>();
		needsSplit.push(new CombinationsSource.ArraysSpliterator(true, 3L, IntStream.range(0, 256).boxed().toArray()));
		List<Spliterator<Object[]>> doneSplits = new ArrayList<>();
		do {
			Spliterator<Object[]> didSplit = Objects.requireNonNull(needsSplit.peek()).trySplit();
			if (null != didSplit) {
				needsSplit.push(didSplit);
			}
			else {
				doneSplits.add(needsSplit.pop());
			}
		} while (!needsSplit.isEmpty());
		int[] collected = doneSplits.stream().flatMap(aSplit -> StreamSupport.stream(aSplit, false)).mapToInt(
			args -> (Integer) args[0]).toArray();
		assertThat(collected).hasSize(1 << 8);
		for (int eachInt = 0; eachInt < (1 << 8); eachInt++) {
			assertThat(collected[eachInt]).isEqualTo(eachInt);
		}
	}

	@Test
	void testNotSplit() {
		Spliterator<Object[]> spliterator = new CombinationsSource.ArraysSpliterator(true, 1L,
			IntStream.range(0, 2).boxed().toArray());
		AtomicInteger gotInt = new AtomicInteger(Integer.MIN_VALUE);
		Consumer<Object[]> acceptInt = obj -> gotInt.set((Integer) obj[0]);
		boolean advanced = spliterator.tryAdvance(acceptInt);
		assertThat(advanced).describedAs("spliterator advanced").isTrue();
		assertThat(gotInt.intValue()).describedAs("got value 0").isZero();
		assertThat(spliterator.trySplit()).describedAs("spliterator split").isNull();
		advanced = spliterator.tryAdvance(acceptInt);
		assertThat(advanced).describedAs("spliterator advanced").isTrue();
		assertThat(gotInt.intValue()).describedAs("got value 0").isOne();
		assertThat(spliterator.trySplit()).describedAs("spliterator split").isNull();
		advanced = spliterator.tryAdvance(acceptInt);
		assertThat(advanced).describedAs("spliterator advanced").isFalse();
		assertThat(spliterator.trySplit()).describedAs("spliterator split").isNull();
	}

	private enum AnEnum {
		FIRST, SECOND
	}
}
