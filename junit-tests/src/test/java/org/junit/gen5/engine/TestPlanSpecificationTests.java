/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Unit tests for {@link TestPlanSpecification}.
 *
 * @since 5.0
 */
public class TestPlanSpecificationTests {

	@Test
	public void testPlanBuilderDemo() {
		TestPlanSpecification specification = TestPlanSpecification.build(
			TestPlanSpecification.forUniqueId("junit5:org.example.UserTests#fullname()"));

		assertNotNull(specification);
	}

	@Test
	public void testForNameWithClass() {
		TestPlanSpecificationElement specification = TestPlanSpecification.forName(MyTestClass.class.getName());
		assertEquals(ClassSpecification.class, specification.getClass());
	}

	@Test
	public void testForNameWithMethod() throws NoSuchMethodException {
		String methodName = MyTestClass.class.getName() + "#" + MyTestClass.class.getDeclaredMethod("myTest").getName();
		TestPlanSpecificationElement specification = TestPlanSpecification.forName(methodName);
		assertEquals(MethodSpecification.class, specification.getClass());
	}

	@Test
	public void testForNameWithPackage() throws NoSuchMethodException {
		String packageName = "org.junit.gen5";
		TestPlanSpecificationElement specification = TestPlanSpecification.forName(packageName);
		assertEquals(PackageSpecification.class, specification.getClass());
	}
}

class MyTestClass {

	@Test
	void myTest() {

	}
}