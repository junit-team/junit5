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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class ParameterizedTestNameFormatterTests {

	@Test
	void formatsInvocationIndex() {
		ParameterizedTestNameFormatter formatter = new ParameterizedTestNameFormatter("{index}");

		assertEquals("1", formatter.format(1));
		assertEquals("2", formatter.format(2));
	}

	@Test
	void formatsIndividualArguments() {
		ParameterizedTestNameFormatter formatter = new ParameterizedTestNameFormatter("{0} -> {1}");

		assertEquals("foo -> 42", formatter.format(1, "foo", 42));
	}

	@Test
	void formatsCompleteArgumentsList() {
		ParameterizedTestNameFormatter formatter = new ParameterizedTestNameFormatter("{arguments}");

		assertEquals("foo, 42", formatter.format(1, "foo", 42));
	}
}
