/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.util.Preconditions;

/**
 * @since 5.0
 */
class ValueArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<ValueSource> {

	private Object[] arguments;

	@Override
	public void accept(ValueSource source) {
		// @formatter:off
		List<Object> arrays =
				// Declaration of <Object> is necessary due to a bug in Eclipse Photon.
				Stream.<Object> of(
					source.shorts(),
					source.bytes(),
					source.ints(),
					source.longs(),
					source.floats(),
					source.doubles(),
					source.chars(),
					source.booleans(),
					source.strings(),
					source.classes()
				)
				.filter(array -> Array.getLength(array) > 0)
				.collect(toList());
		// @formatter:on

		Preconditions.condition(arrays.size() == 1, () -> "Exactly one type of input must be provided in the @"
				+ ValueSource.class.getSimpleName() + " annotation, but there were " + arrays.size());

		Object originalArray = arrays.get(0);
		arguments = IntStream.range(0, Array.getLength(originalArray)) //
				.mapToObj(index -> Array.get(originalArray, index)) //
				.toArray();
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		return Arrays.stream(arguments).map(Arguments::of);
	}

}
