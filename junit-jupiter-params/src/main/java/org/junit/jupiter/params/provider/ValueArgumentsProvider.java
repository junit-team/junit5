/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.provider;

import static java.util.stream.Collectors.toList;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.util.Preconditions;

/**
 * @since 5.0
 */
class ValueArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<ValueSource> {

	private Object[] arguments;

	@Override
	public void accept(ValueSource source) {
		List<Object> arrays = Stream.of(source.strings(), source.ints(), source.longs(), source.doubles()) //
				.filter(array -> Array.getLength(array) > 0) //
				.collect(toList());
		Preconditions.condition(arrays.size() == 1, () -> "Exactly one type of input must be provided in the @"
				+ ValueSource.class.getSimpleName() + " annotation but there were " + arrays.size());
		Object originalArray = arrays.get(0);
		arguments = IntStream.range(0, Array.getLength(originalArray)) //
				.mapToObj(index -> Array.get(originalArray, index)) //
				.toArray();
	}

	@Override
	public Stream<? extends Arguments> arguments(ContainerExtensionContext context) {
		return Arrays.stream(arguments).map(ObjectArrayArguments::arguments);
	}
}
