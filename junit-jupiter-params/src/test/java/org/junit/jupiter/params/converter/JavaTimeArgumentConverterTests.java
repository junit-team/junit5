/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.converter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class JavaTimeArgumentConverterTests {

	@Test
	void convertsLocalDate() {
		JavaTimeArgumentConverter converter = new JavaTimeArgumentConverter();
		JavaTimeConversionPattern annotation = mock(JavaTimeConversionPattern.class);
		when(annotation.value()).thenReturn("dd.MM.yyyy");
		converter.initialize(annotation);

		Object result = converter.convert("01.02.2017", LocalDate.class);

		assertThat(result).isEqualTo(LocalDate.of(2017, 2, 1));
	}

}
