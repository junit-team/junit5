/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.resolver.testpackage;

import static org.assertj.core.api.Assertions.assertThat;

public class NestingTestClass {
	public static class NestedStaticClass {
		void nestedStaticClassTest() {
			assertThat(1 + 1).isEqualTo(2);
		}
	}

	public class NestedInnerClass {
		void nestedInnerClassTest() {
			assertThat(1 + 1).isEqualTo(2);
		}
	}
}
