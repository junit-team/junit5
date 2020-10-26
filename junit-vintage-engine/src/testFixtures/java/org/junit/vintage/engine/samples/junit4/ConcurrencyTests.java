/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.samples.junit4;

import org.junit.Test;

/**
 * Vintage tests to reproduce various concurrency scenarios.
 *
 * @since 5.8
 */
public class ConcurrencyTests {

	public static class A {
		@Test
		public final void test1() {
		}

		@Test
		public final void test2() {
		}
	}

	public static class B {
		@Test
		public final void test1() {
		}

		@Test
		public final void test2() {
		}
	}

}
