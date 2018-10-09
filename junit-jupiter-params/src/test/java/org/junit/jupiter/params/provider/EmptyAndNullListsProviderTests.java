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

import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class EmptyAndNullListsProviderTests {

	@Test
	public void testContainNullEmpty() {
		Stream<Object[]> stream = provideArguments();
		assertThat(stream).isNotNull();

		List<Object[]> argumentObjectList = stream.collect(toList());

		argumentObjectList.forEach(argumentObject -> {
			assertThat(argumentObject).isNotNull();
			assertThat(argumentObject.length).isEqualTo(1);
		});

		List<Object> arguments = argumentObjectList.stream().map(array -> array[0]).collect(toList());

		assertSoftly(softly -> {
			softly.assertThat(arguments).contains((Object) null);
			softly.assertThat(arguments).anySatisfy(object -> {
				assertThat(object).isInstanceOf(List.class);
				List<?> list = (List<?>) object;
				assertThat(list).isEmpty();
			});
		});
	}

	private Stream<Object[]> provideArguments() {
		EmptyAndNullListsProvider provider = new EmptyAndNullListsProvider();
		return provider.provideArguments(null).map(Arguments::get);
	}
}
