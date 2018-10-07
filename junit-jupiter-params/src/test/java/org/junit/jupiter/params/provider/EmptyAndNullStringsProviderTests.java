/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class EmptyAndNullStringsProviderTests {

	@Test
	public void testContainNullAndEmpty() {
		testContainNullAndEmptyAndBlankValues();
	}

	@Test
	public void testContainNullAndEmptyAndBlank1() {
		testContainNullAndEmptyAndBlankValues(" ");
	}

	@Test
	public void testContainNullAndEmptyAndBlank2() {
		testContainNullAndEmptyAndBlankValues(" ", "  ");
	}

	private void testContainNullAndEmptyAndBlankValues(String... blankValues) {
		Stream<Object[]> stream = provideArguments(blankValues);
		assertThat(stream).isNotNull();

		List<Object[]> list = stream.collect(toList());

		list.forEach(o -> {
			assertThat(o).isNotNull();
			assertThat(o.length).isEqualTo(1);
		});

		List<Object> strings = list.stream().map(array -> array[0]).collect(toList());

		assertSoftly(softly -> {
			softly.assertThat(strings).contains((Object) null);
			softly.assertThat(strings).contains("");

			for (String blankValue : blankValues) {
				softly.assertThat(strings).contains(blankValue);
			}
		});
	}

	private Stream<Object[]> provideArguments(String... blankValues) {
		EmptyAndNullStrings annotation = mock(EmptyAndNullStrings.class);
		when(annotation.blankValues()).thenReturn(blankValues);

		EmptyAndNullStringsProvider provider = new EmptyAndNullStringsProvider();
		provider.accept(annotation);
		return provider.provideArguments(null).map(Arguments::get);
	}
}
