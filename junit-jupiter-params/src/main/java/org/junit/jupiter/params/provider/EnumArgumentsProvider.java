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

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;

class EnumArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<EnumSource> {

	private Class<? extends Enum<?>> enumClass;
	private Set<String> names = Collections.emptySet();

	@Override
	public void accept(EnumSource annotation) {
		enumClass = annotation.value();
		if (annotation.names().length > 0) {
			names = Stream.of(annotation.names()).collect(Collectors.toSet());
			if (names.size() != annotation.names().length) {
				throw new IllegalArgumentException(
					"Duplicate constant name(s) found in: " + Arrays.asList(annotation.names()));
			}
			@SuppressWarnings("rawtypes")
			Set<String> allSet = Arrays.stream(enumClass.getEnumConstants()).map(Enum::name).collect(
				Collectors.toSet());
			if (!allSet.containsAll(names)) {
				throw new IllegalArgumentException(
					"Invalid constant name(s) found in: " + names + ". Valid names are: " + allSet);
			}
		}
	}

	private boolean select(Enum<?> constant) {
		return names == Collections.EMPTY_SET || names.contains(constant.name());
	}

	@Override
	public Stream<? extends Arguments> arguments(ContainerExtensionContext context) {
		return Arrays.stream(enumClass.getEnumConstants()).filter(this::select).map(ObjectArrayArguments::create);
	}

}
