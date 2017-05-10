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

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.util.Preconditions;

/**
 * @since 5.0
 */
class EnumArgumentsProvider<E extends Enum<E>> implements ArgumentsProvider, AnnotationConsumer<EnumSource> {

	private Class<E> enumClass;
	private EnumSource.Mode mode;
	private Set<String> names = Collections.emptySet();

	@Override
	@SuppressWarnings("unchecked")
	public void accept(EnumSource enumSource) {
		this.enumClass = (Class<E>) enumSource.value();
		this.mode = enumSource.mode();
		// only set "names" field if the user provided at least one name
		if (enumSource.names().length > 0) {
			this.names = stream(enumSource.names()).collect(toSet());
			Preconditions.condition(names.size() == enumSource.names().length,
				() -> "Duplicate enum constant name(s) found in: " + enumSource);
			mode.validate(enumSource, names);
		}
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ContainerExtensionContext context) {
		Stream<E> stream = EnumSet.allOf(enumClass).stream();
		if (names != Collections.EMPTY_SET) {
			stream = stream.filter(constant -> mode.select(constant, names));
		}
		return stream.map(ObjectArrayArguments::arguments);
	}

}
