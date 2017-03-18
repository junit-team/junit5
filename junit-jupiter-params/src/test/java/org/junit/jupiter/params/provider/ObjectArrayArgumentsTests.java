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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

class ObjectArrayArgumentsTests {

	@Test
	void supportsVarargs() {
		ObjectArrayArguments arguments = ObjectArrayArguments.create(1, "2", 3.0);

		assertArrayEquals(new Object[] { 1, "2", 3.0 }, arguments.get());
	}

	@Test
	void returnsSameArrayUsedForCreating() {
		Object[] input = { 1, "2", 3.0 };

		ObjectArrayArguments arguments = ObjectArrayArguments.create(input);

		assertThat(arguments.get()).isSameAs(input);
	}

}
