/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package example;

// tag::user_guide[]

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DependsOn;
import org.junit.jupiter.api.MethodOrderer.DependsOnAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.engine.extension.DependsOnTestWatcher;

@ExtendWith(DependsOnTestWatcher.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(DependsOnAnnotation.class)
public class ExampleDependsOnAnnotationTestCase {
	private int counter = 0;

	@AfterEach
	void increaseCounter() {
		counter += 1;
	}

	@Test
	void alpha() {
		assertEquals(1, counter);
	}

	@Test
	@DependsOn("alpha")
	void beta() {
		assertEquals(2, counter);
	}

	@Test
	@DependsOn("beta")
	void gamma() {
		assertEquals(3, counter);
	}

	@Test
	@DependsOn("gamma")
	void delta() {
		assertEquals(4, counter);
	}

	@Test
	void independentTest() {
		assertEquals(0, counter, "Independent tests should run first");
	}
}
// end::user_guide[]
