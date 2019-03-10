/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toSet;

import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.util.Preconditions;

/**
 * @since 5.0
 */
class EnumArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<EnumSource> {

	private EnumSet<?> constants;

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void accept(EnumSource enumSource) {
		Class enumClass = enumSource.value();
		this.constants = EnumSet.allOf(enumClass);

		EnumSource.Mode mode = enumSource.mode();
		String[] declaredConstantNames = enumSource.names();
		if (declaredConstantNames.length > 0) {
			Set<String> uniqueNames = stream(declaredConstantNames).collect(toSet());
			Preconditions.condition(uniqueNames.size() == declaredConstantNames.length,
				() -> "Duplicate enum constant name(s) found in " + enumSource);
			mode.validate(enumSource, uniqueNames);
			this.constants.removeIf(constant -> !mode.select(constant, uniqueNames));
		}
	}

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		return constants.stream().map(Arguments::of);
	}

}
