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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.EnumArgumentsProviderTests.EnumWithTwoConstants.BAR;
import static org.junit.jupiter.params.provider.EnumArgumentsProviderTests.EnumWithTwoConstants.FOO;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.Arguments;

class EnumArgumentsProviderTests {

	@Test
	void providesAllEnumConstants() {
		Stream<Object[]> arguments = provideArguments(EnumWithTwoConstants.class);

		assertThat(arguments).containsExactly(new Object[] { FOO }, new Object[] { BAR });
	}

	@Test
	void provideSingleEnumConstant() {
		Stream<Object[]> arguments = provideArguments(EnumWithTwoConstants.class, "FOO");

		assertThat(arguments).containsExactly(new Object[] { FOO });
	}

	@Test
	void provideAllEnumConstantsWithNamingAll() {
		Stream<Object[]> arguments = provideArguments(EnumWithTwoConstants.class, "FOO", "BAR");

		assertThat(arguments).containsExactly(new Object[] { FOO }, new Object[] { BAR });
	}

	@Test
	void duplicateConstantNameIsDetected() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
			() -> provideArguments(EnumWithTwoConstants.class, "FOO", "BAR", "FOO"));
		assertThat(exception).hasMessageContaining("Duplicate constant name(s) found");
	}

	@Test
	void invalidConstantNameIsDetected() {
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
			() -> provideArguments(EnumWithTwoConstants.class, "F00", "B4R"));
		assertThat(exception).hasMessageContaining("Invalid constant name(s) found");
	}

	enum EnumWithTwoConstants {
		FOO, BAR
	}

	private Stream<Object[]> provideArguments(Class<? extends Enum<?>> enumClass, String... names) {
		EnumSource annotation = mock(EnumSource.class);
		when(annotation.value()).thenAnswer(invocation -> enumClass);
		when(annotation.names()).thenAnswer(invocation -> names);

		EnumArgumentsProvider provider = new EnumArgumentsProvider();
		provider.initialize(annotation);
		return provider.arguments(null).map(Arguments::get);
	}

}
