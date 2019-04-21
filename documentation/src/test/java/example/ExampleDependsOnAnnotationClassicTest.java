/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example;

// tag::user_guide[]

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DependsOn;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.DependsOnAnnotation.class)
class ExampleDependsOnAnnotationClassicTest {
	@Test
	void independentTest() {
		assertEquals(0, 0);
	}

	@Test
	void gamma() {
		assertEquals(1, 1);
	}

	@Test
	@DependsOn("gamma")
	void delta() {
		assertEquals(2, 2);
	}

	@Test
	@DependsOn("delta")
	void beta() {
		assertEquals(3, 3);
	}

	@Test
	@DependsOn("beta")
	void alpha() {
		assertEquals(4, 4);
	}
}
// end::user_guide[]
