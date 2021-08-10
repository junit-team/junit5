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
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * Tests for {@link TypedArgumentConverter}.
 *
 * @since 5.7
 */
class TypedArgumentConverterTests {

	/**
	 * @since 5.8
	 */
	@Test
	void preconditions() {
		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(() -> new StringLengthArgumentConverter(null, Integer.class))//
				.withMessage("sourceType must not be null");

		assertThatExceptionOfType(PreconditionViolationException.class)//
				.isThrownBy(() -> new StringLengthArgumentConverter(String.class, null))//
				.withMessage("targetType must not be null");
	}

	@Test
	void convertsSourceToTarget() {
		assertAll(//
			() -> assertConverts("abcd", 4), //
			() -> assertConverts("", 0) //
		);
	}

	private void assertConverts(String input, int expected) {
		int length = new StringLengthArgumentConverter().convert(input);
		assertThat(length).isEqualTo(expected);
	}

	private static class StringLengthArgumentConverter extends TypedArgumentConverter<String, Integer> {

		StringLengthArgumentConverter() {
			this(String.class, Integer.class);
		}

		StringLengthArgumentConverter(Class<String> sourceType, Class<Integer> targetType) {
			super(sourceType, targetType);
		}

		@Override
		protected Integer convert(String source) throws ArgumentConversionException {
			return source.length();
		}
	}

}
