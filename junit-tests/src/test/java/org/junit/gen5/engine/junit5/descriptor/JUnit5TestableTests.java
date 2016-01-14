/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import static org.junit.gen5.api.Assertions.*;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import org.junit.gen5.api.Test;
import org.junit.gen5.engine.DummyTestEngine;
import org.junit.gen5.engine.EngineDescriptor;

public class JUnit5TestableTests {

	private final EngineDescriptor engineDescriptor = new EngineDescriptor(new DummyTestEngine("ENGINE_ID"));;

	@Test
	public void fromUniqueIdForTopLevelClass() {

		JUnit5Class testable = (JUnit5Class) JUnit5Testable.fromUniqueId(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.ATestClass", engineDescriptor.getUniqueId());
		assertEquals("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.ATestClass", testable.getUniqueId());
		assertSame(ATestClass.class, testable.getJavaClass());
	}

	@Test
	public void fromUniqueIdForNestedClass() {

		JUnit5Class testable = (JUnit5Class) JUnit5Testable.fromUniqueId(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.ATestClass$AnInnerTestClass",
			engineDescriptor.getUniqueId());
		assertEquals("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.ATestClass$AnInnerTestClass",
			testable.getUniqueId());
		assertSame(ATestClass.AnInnerTestClass.class, testable.getJavaClass());
	}

	@Test
	public void fromUniqueIdForDoubleNestedClass() {

		JUnit5Class testable = (JUnit5Class) JUnit5Testable.fromUniqueId(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.ATestClass$AnInnerTestClass$InnerInnerTestClass",
			engineDescriptor.getUniqueId());
		assertEquals(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.ATestClass$AnInnerTestClass$InnerInnerTestClass",
			testable.getUniqueId());
		assertSame(ATestClass.AnInnerTestClass.InnerInnerTestClass.class, testable.getJavaClass());
	}

	@Test
	public void fromUniqueIdForMethod() throws NoSuchMethodException {

		JUnit5Method testable = (JUnit5Method) JUnit5Testable.fromUniqueId(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.ATestClass#test1()", engineDescriptor.getUniqueId());
		assertEquals("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.ATestClass#test1()", testable.getUniqueId());
		Method testMethod = ATestClass.class.getDeclaredMethod("test1");
		assertEquals(testMethod, testable.getJavaMethod());
	}

	@Test
	public void fromUniqueIdForMethodWithParameters() throws NoSuchMethodException {

		JUnit5Method testable = (JUnit5Method) JUnit5Testable.fromUniqueId(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.BTestClass#test4(java.lang.String, java.math.BigDecimal)",
			engineDescriptor.getUniqueId());
		assertEquals(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.BTestClass#test4(java.lang.String, java.math.BigDecimal)",
			testable.getUniqueId());
		Method testMethod = BTestClass.class.getDeclaredMethod("test4", String.class, BigDecimal.class);
		assertEquals(testMethod, testable.getJavaMethod());
	}

	@Test
	public void fromUniqueIdForMethodInNestedClass() throws NoSuchMethodException {

		JUnit5Method testable = (JUnit5Method) JUnit5Testable.fromUniqueId(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.ATestClass$AnInnerTestClass#test2()",
			engineDescriptor.getUniqueId());
		assertEquals("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.ATestClass$AnInnerTestClass#test2()",
			testable.getUniqueId());
		Method testMethod = ATestClass.AnInnerTestClass.class.getDeclaredMethod("test2");
		assertEquals(testMethod, testable.getJavaMethod());
	}

	@Test
	public void fromClass() throws NoSuchMethodException {
		JUnit5Class testable = (JUnit5Class) JUnit5Testable.fromClass(ATestClass.class, engineDescriptor.getUniqueId());
		assertEquals("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.ATestClass", testable.getUniqueId());
		assertSame(ATestClass.class, testable.getJavaClass());
	}

	@Test
	public void nestedClassFromClass() throws NoSuchMethodException {
		JUnit5Class testable = (JUnit5Class) JUnit5Testable.fromClass(ATestClass.AnInnerTestClass.class,
			engineDescriptor.getUniqueId());
		assertEquals("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.ATestClass$AnInnerTestClass",
			testable.getUniqueId());
		assertSame(ATestClass.AnInnerTestClass.class, testable.getJavaClass());
	}

	@Test
	public void fromMethod() throws NoSuchMethodException {
		Method testMethod = ATestClass.class.getDeclaredMethod("test1");
		JUnit5Method testable = (JUnit5Method) JUnit5Testable.fromMethod(testMethod, ATestClass.class,
			engineDescriptor.getUniqueId());
		assertEquals("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.ATestClass#test1()", testable.getUniqueId());
		assertSame(testMethod, testable.getJavaMethod());
		assertSame(ATestClass.class, testable.getContainerClass());
	}

	@Test
	public void fromMethodWithParameters() throws NoSuchMethodException {
		Method testMethod = BTestClass.class.getDeclaredMethod("test4", String.class, BigDecimal.class);
		JUnit5Method testable = (JUnit5Method) JUnit5Testable.fromMethod(testMethod, BTestClass.class,
			engineDescriptor.getUniqueId());
		assertEquals(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.BTestClass#test4(java.lang.String, java.math.BigDecimal)",
			testable.getUniqueId());
		assertSame(testMethod, testable.getJavaMethod());
	}

}

class ATestClass {

	@Test
	void test1() {

	}

	static class AnInnerTestClass {

		@Test
		void test2() {

		}

		static class InnerInnerTestClass {

			@Test
			void test3() {

			}
		}
	}
}

class BTestClass extends ATestClass {

	@Test
	void test4(String aString, BigDecimal aBigDecimal) {

	}

}
