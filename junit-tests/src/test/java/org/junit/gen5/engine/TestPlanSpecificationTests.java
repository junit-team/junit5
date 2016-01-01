/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static java.util.stream.Collectors.toList;
import static org.junit.gen5.api.Assertions.*;
import static org.junit.gen5.engine.TestPlanSpecification.*;

import java.util.Arrays;
import java.util.List;

import org.junit.gen5.api.Test;

/**
 * Unit tests for {@link TestPlanSpecification}.
 *
 * @since 5.0
 */
public class TestPlanSpecificationTests {

	@Test
	public void forUniqueIdForMethod() {
		TestPlanSpecificationElement element = forUniqueId("junit5:org.example.UserTests#fullname()");
		assertEquals(UniqueIdSpecification.class, element.getClass());
	}

	@Test
	public void forNameWithClass() {
		TestPlanSpecificationElement element = forName(MyTestClass.class.getName());
		assertEquals(ClassSpecification.class, element.getClass());
	}

	@Test
	public void forNameWithMethod() throws Exception {
		TestPlanSpecificationElement element = forName(fullyQualifiedMethodName());
		assertEquals(MethodSpecification.class, element.getClass());
	}

	@Test
	public void forNameWithPackage() {
		TestPlanSpecificationElement element = forName("org.junit.gen5");
		assertEquals(PackageSpecification.class, element.getClass());
	}

	@Test
	public void buildSpecification() throws Exception {
		// @formatter:off
		TestPlanSpecification spec = build(
			forUniqueId("junit5:org.example.UserTests#fullname()"),
			forName(MyTestClass.class.getName()),
			forName("org.junit.gen5"),
			forName(fullyQualifiedMethodName())
		);
		// @formatter:on

		assertNotNull(spec);
		List<Class<? extends TestPlanSpecificationElement>> expected = Arrays.asList(UniqueIdSpecification.class,
			ClassSpecification.class, PackageSpecification.class, MethodSpecification.class);
		assertEquals(expected, spec.getElements().stream().map(Object::getClass).collect(toList()));
	}

	private String fullyQualifiedMethodName() throws Exception {
		return MyTestClass.class.getName() + "#" + MyTestClass.class.getDeclaredMethod("myTest").getName();
	}

	static class MyTestClass {

		void myTest() {
		}
	}

}
