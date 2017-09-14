/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.PreconditionViolationException;

/**
 * @since 5.0
 */
class ValueArgumentsProviderTests {

	@Test
	void providesStrings() {
		Stream<Object[]> arguments = provideArguments(new String[] { "foo", "bar" }, new int[0], new long[0],
			new double[0]);

		assertThat(arguments).containsExactly(new Object[] { "foo" }, new Object[] { "bar" });
	}

	@Test
	void providesInts() {
		Stream<Object[]> arguments = provideArguments(new String[0], new int[] { 23, 42 }, new long[0], new double[0]);

		assertThat(arguments).containsExactly(new Object[] { 23 }, new Object[] { 42 });
	}

	@Test
	void providesLongs() {
		Stream<Object[]> arguments = provideArguments(new String[0], new int[0], new long[] { 23, 42 }, new double[0]);

		assertThat(arguments).containsExactly(new Object[] { 23L }, new Object[] { 42L });
	}

	@Test
	void providesDoubles() {
		Stream<Object[]> arguments = provideArguments(new String[0], new int[0], new long[0],
			new double[] { 23.32, 42.24 });

		assertThat(arguments).containsExactly(new Object[] { 23.32 }, new Object[] { 42.24 });
	}

	@Test
	void multipleInputsAreNotAllowed() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments(new String[1], new int[1], new long[0], new double[0]));
		assertThat(exception).hasMessageContaining(
			"Exactly one type of input must be provided in the @ValueSource annotation but there were 2");
	}

	@Test
	void onlyEmptyInputsAreNotAllowed() {
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> provideArguments(new String[0], new int[0], new long[0], new double[0]));
		assertThat(exception).hasMessageContaining(
			"Exactly one type of input must be provided in the @ValueSource annotation but there were 0");
	}

	private Stream<Object[]> provideArguments(String[] strings, int[] ints, long[] longs, double[] doubles) {
		ValueSource annotation = mock(ValueSource.class);
		when(annotation.strings()).thenReturn(strings);
		when(annotation.ints()).thenReturn(ints);
		when(annotation.longs()).thenReturn(longs);
		when(annotation.doubles()).thenReturn(doubles);

		ValueArgumentsProvider provider = new ValueArgumentsProvider();
		provider.accept(annotation);
		return provider.provideArguments(null).map(Arguments::get);
	}

}
