/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import org.junit.jupiter.api.Test;

class DefaultArgumentConverterTests {

	@Test
	void isAwareOfWrapperTypesForPrimitiveTypes() {
		assertConverts(true, boolean.class, true);
		assertConverts((byte) 1, byte.class, (byte) 1);
		assertConverts('o', char.class, 'o');
		assertConverts((short) 1, short.class, (short) 1);
		assertConverts(1, int.class, 1);
		assertConverts(1L, long.class, 1L);
		assertConverts(1.0f, float.class, 1.0f);
		assertConverts(1.0d, double.class, 1.0d);
	}

	private void assertConverts(Object input, Class<?> targetClass, Object expectedOutput) {
		Object result = DefaultArgumentConverter.INSTANCE.convert(input, targetClass);

		assertThat(result) //
				.describedAs(input + " --(" + targetClass.getName() + ")--> " + expectedOutput) //
				.isEqualTo(expectedOutput);
	}

}
