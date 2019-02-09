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

import org.junit.jupiter.api.Depend;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.MethodOrderer.DependAnnotation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(DependAnnotation.class)
public class ExampleDependAnnotationTestCase {
	@Test
	@Disabled
	void d() {
		System.out.println("Test 1");
	}

	@Test
	@Depend(methods = "d")
	void c() {
		System.out.println("Test 2");
	}

	@Test
	@Depend(methods = "d")
	void b() {
		System.out.println("Test 3");
	}

	@Test
	@Depend(methods = "b")
	void a() {
		System.out.println("Test 4");
	}
}
// end::user_guide[]
