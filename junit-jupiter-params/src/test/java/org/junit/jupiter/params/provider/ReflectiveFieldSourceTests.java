/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

/**
 * @since 5.0
 */
class ReflectiveFieldSourceTests {

	public class TestReflectiveFieldSource extends ReflectiveFieldArguments {

		private final int i;
		private final String s;
		private final double v;

		public TestReflectiveFieldSource(int i, String s, double v) {
			this.i = i;
			this.s = s;
			this.v = v;
		}
	}

	@Test
	void providesOrder() {
		Arguments arguments = new TestReflectiveFieldSource(1, "2", 3.0);

		assertArrayEquals(new Object[] { 1, "2", 3.0 }, arguments.get());
	}

}
