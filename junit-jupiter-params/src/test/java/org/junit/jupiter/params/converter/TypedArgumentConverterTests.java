/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * Unit tests for {@link TypedArgumentConverter}.
 *
 * @since 5.7
 */
class TypedArgumentConverterTests {

	@Test
	void preconditions() {
		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(() -> new PassThroughConstructorConverter(null, String.class))//
				.withMessage("sourceType must not be null");

		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(() -> new PassThroughConstructorConverter(String.class, null))//
				.withMessage("targetType must not be null");
	}

	@Test
	void convertsSourceToTarget() {
		assertConverts("abcd", 4);
		assertConverts("", 0);
	}

	private void assertConverts(String input, Integer expectedOutput) {
		var result = new StringLengthArgumentConverter().convert(input);
		assertThat(result).isEqualTo(expectedOutput);
	}

	private static class StringLengthArgumentConverter extends TypedArgumentConverter<String, Integer> {

		protected StringLengthArgumentConverter() {
			super(String.class, Integer.class);
		}

		@Override
		protected Integer convert(String source) throws ArgumentConversionException {
			return source.length();
		}

	}

}
