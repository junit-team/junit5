/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.sources;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.sources.EnumArgumentsProviderTests.EnumWithTwoConstants.BAR;
import static org.junit.jupiter.params.sources.EnumArgumentsProviderTests.EnumWithTwoConstants.FOO;
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

	enum EnumWithTwoConstants {
		FOO, BAR
	}

	private Stream<Object[]> provideArguments(Class<? extends Enum<?>> enumClass) {
		EnumSource annotation = mock(EnumSource.class);
		when(annotation.value()).thenAnswer(invocation -> enumClass);

		EnumArgumentsProvider provider = new EnumArgumentsProvider();
		provider.initialize(annotation);
		return provider.arguments(null).map(Arguments::get);
	}

}
