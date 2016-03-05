/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.discovery;

import static org.junit.gen5.api.Assertions.*;
import static org.junit.gen5.engine.junit5.discovery.JUnit5UniqueIdBuilder.*;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import org.junit.gen5.api.Nested;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.support.descriptor.EngineDescriptor;

public class JUnit5TestableTests {

	@Test
	public void fromUniqueIdForTopLevelClass() {

		JUnit5Class testable = (JUnit5Class) JUnit5Testable.fromUniqueId(uniqueIdForClass(ATestClass.class),
			engineId());
		assertEquals("[engine:junit5]/[class:org.junit.gen5.engine.junit5.discovery.ATestClass]",
			testable.getUniqueId().getUniqueString());
		assertSame(ATestClass.class, testable.getJavaClass());
	}

	@Test
	public void fromUniqueIdForStaticInnerClass() {

		JUnit5Class testable = (JUnit5Class) JUnit5Testable.fromUniqueId(
			uniqueIdForClass(ATestClass.AnInnerStaticClass.class), engineId());
		assertEquals("[engine:junit5]/[class:org.junit.gen5.engine.junit5.discovery.ATestClass$AnInnerStaticClass]",
			testable.getUniqueId().getUniqueString());
		assertSame(ATestClass.AnInnerStaticClass.class, testable.getJavaClass());
	}

	@Test
	public void fromUniqueIdForNestedTestClass() {

		JUnit5Class testable = (JUnit5Class) JUnit5Testable.fromUniqueId(
			uniqueIdForClass(ATestClass.ANestedTestClass.class), engineId());
		assertEquals(
			"[engine:junit5]/[class:org.junit.gen5.engine.junit5.discovery.ATestClass]/[nested-class:ANestedTestClass]",
			testable.getUniqueId().getUniqueString());
		assertSame(ATestClass.ANestedTestClass.class, testable.getJavaClass());
	}

	@Test
	public void fromUniqueIdForMethod() throws NoSuchMethodException {

		JUnit5Method testable = (JUnit5Method) JUnit5Testable.fromUniqueId(
			uniqueIdForMethod(ATestClass.class, "test1()"), engineId());
		assertEquals("[engine:junit5]/[class:org.junit.gen5.engine.junit5.discovery.ATestClass]/[method:test1()]",
			testable.getUniqueId().getUniqueString());
		Method testMethod = ATestClass.class.getDeclaredMethod("test1");
		assertEquals(testMethod, testable.getJavaMethod());
	}

	@Test
	public void fromUniqueIdForMethodWithParameters() throws NoSuchMethodException {

		JUnit5Method testable = (JUnit5Method) JUnit5Testable.fromUniqueId(
			uniqueIdForMethod(BTestClass.class, "test4(java.lang.String, java.math.BigDecimal)"), engineId());
		assertEquals(
			"[engine:junit5]/[class:org.junit.gen5.engine.junit5.discovery.BTestClass]/[method:test4(java.lang.String, java.math.BigDecimal)]",
			testable.getUniqueId().getUniqueString());
		Method testMethod = BTestClass.class.getDeclaredMethod("test4", String.class, BigDecimal.class);
		assertEquals(testMethod, testable.getJavaMethod());
	}

	@Test
	public void fromUniqueIdForMethodInNestedClass() throws NoSuchMethodException {

		JUnit5Method testable = (JUnit5Method) JUnit5Testable.fromUniqueId(
			uniqueIdForMethod(ATestClass.ANestedTestClass.class, "test3()"), engineId());
		assertEquals(
			"[engine:junit5]/[class:org.junit.gen5.engine.junit5.discovery.ATestClass]/[nested-class:ANestedTestClass]/[method:test3()]",
			testable.getUniqueId().getUniqueString());
		Method testMethod = ATestClass.ANestedTestClass.class.getDeclaredMethod("test3");
		assertEquals(testMethod, testable.getJavaMethod());
	}

	@Test
	public void fromClass() throws NoSuchMethodException {
		JUnit5Class testable = (JUnit5Class) JUnit5Testable.fromClass(ATestClass.class, engineId());
		assertEquals(uniqueIdForClass(ATestClass.class), testable.getUniqueId());
		assertSame(ATestClass.class, testable.getJavaClass());
	}

	@Test
	public void innerStaticClassFromClass() throws NoSuchMethodException {
		JUnit5Class testable = (JUnit5Class) JUnit5Testable.fromClass(ATestClass.AnInnerStaticClass.class, engineId());
		assertEquals(uniqueIdForClass(ATestClass.AnInnerStaticClass.class), testable.getUniqueId());
		assertSame(ATestClass.AnInnerStaticClass.class, testable.getJavaClass());
	}

	@Test
	public void nestedClassFromClass() throws NoSuchMethodException {
		JUnit5Class testable = (JUnit5Class) JUnit5Testable.fromClass(ATestClass.ANestedTestClass.class, engineId());
		assertEquals(uniqueIdForClass(ATestClass.ANestedTestClass.class), testable.getUniqueId());
		assertSame(ATestClass.ANestedTestClass.class, testable.getJavaClass());
	}

	@Test
	public void fromMethod() throws NoSuchMethodException {
		Method testMethod = ATestClass.class.getDeclaredMethod("test1");
		JUnit5Method testable = (JUnit5Method) JUnit5Testable.fromMethod(testMethod, ATestClass.class, engineId());
		assertEquals(uniqueIdForMethod(ATestClass.class, "test1()"), testable.getUniqueId());
		assertSame(testMethod, testable.getJavaMethod());
		assertSame(ATestClass.class, testable.getContainerClass());
	}

	@Test
	public void fromMethodWithParameters() throws NoSuchMethodException {
		Method testMethod = BTestClass.class.getDeclaredMethod("test4", String.class, BigDecimal.class);
		JUnit5Method testable = (JUnit5Method) JUnit5Testable.fromMethod(testMethod, BTestClass.class, engineId());
		assertEquals(uniqueIdForMethod(BTestClass.class, "test4(java.lang.String, java.math.BigDecimal)"),
			testable.getUniqueId());
		assertSame(testMethod, testable.getJavaMethod());
	}

}

class ATestClass {

	@Test
	void test1() {

	}

	static class AnInnerStaticClass {

		@Test
		void test2() {

		}

	}

	@Nested
	class ANestedTestClass {

		@Test
		void test3() {

		}

	}
}

class BTestClass extends ATestClass {

	@Test
	void test4(String aString, BigDecimal aBigDecimal) {

	}

}
