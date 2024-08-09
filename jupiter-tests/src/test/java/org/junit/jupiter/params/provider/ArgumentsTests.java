/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.jupiter.params.provider.Arguments.of;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Arguments}.
 *
 * @since 5.0
 */
class ArgumentsTests {

	@Test
	void ofSupportsVarargs() {
		var arguments = of(1, "2", 3.0);

		assertArrayEquals(new Object[] { 1, "2", 3.0 }, arguments.get());
	}

	@Test
	void argumentsSupportsVarargs() {
		var arguments = arguments(1, "2", 3.0);

		assertArrayEquals(new Object[] { 1, "2", 3.0 }, arguments.get());
	}

	@Test
	void ofReturnsSameArrayUsedForCreating() {
		Object[] input = { 1, "2", 3.0 };

		var arguments = of(input);

		assertThat(arguments.get()).isSameAs(input);
	}

	@Test
	void argumentsReturnsSameArrayUsedForCreating() {
		Object[] input = { 1, "2", 3.0 };

		var arguments = arguments(input);

		assertThat(arguments.get()).isSameAs(input);
	}

}
