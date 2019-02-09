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

import org.junit.jupiter.api.DependsOn;
import org.junit.jupiter.api.MethodOrderer.DependsOnAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(DependsOnAnnotation.class)
public class ExampleDependsOnAnnotationTestCase {
	@Test
	void d() {
		System.out.println("Test 1");
	}

	@Test
	@DependsOn(value = "d")
	void c() {
		System.out.println("Test 2");
	}

	@Test
	@DependsOn(value = "c")
	void b() {
		System.out.println("Test 3");
	}

	@Test
	@DependsOn(value = "b")
	void a() {
		System.out.println("Test 4");
	}

	@Test
	void indenepentTest() {
		System.out.println("Independent tests will run first");
	}
}
// end::user_guide[]
