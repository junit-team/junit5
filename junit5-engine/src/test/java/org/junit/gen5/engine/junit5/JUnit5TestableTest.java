/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import java.lang.reflect.Method;
import java.math.BigDecimal;

import org.junit.Assert;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.junit5.descriptor.JUnit5Testable;

public class JUnit5TestableTest {

	EngineDescriptor engineDescriptor = new EngineDescriptor(new JUnit5TestEngine());

	@org.junit.Test
	public void fromUniqueIdForTopLevelClass() {

		JUnit5Testable uniqueId = JUnit5Testable.fromUniqueId("junit5:org.junit.gen5.engine.junit5.ATestClass",
			engineDescriptor.getUniqueId());
		Assert.assertEquals("junit5:org.junit.gen5.engine.junit5.ATestClass", uniqueId.getUniqueId());
		Assert.assertSame(ATestClass.class, uniqueId.getJavaElement());
	}

	@org.junit.Test
	public void fromUniqueIdForNestedClass() {

		JUnit5Testable uniqueId = JUnit5Testable.fromUniqueId(
			"junit5:org.junit.gen5.engine.junit5.ATestClass$AnInnerTestClass", engineDescriptor.getUniqueId());
		Assert.assertEquals("junit5:org.junit.gen5.engine.junit5.ATestClass$AnInnerTestClass", uniqueId.getUniqueId());
		Assert.assertSame(ATestClass.AnInnerTestClass.class, uniqueId.getJavaElement());
	}

	@org.junit.Test
	public void fromUniqueIdForDoubleNestedClass() {

		JUnit5Testable uniqueId = JUnit5Testable.fromUniqueId(
			"junit5:org.junit.gen5.engine.junit5.ATestClass$AnInnerTestClass$InnerInnerTestClass",
			engineDescriptor.getUniqueId());
		Assert.assertEquals("junit5:org.junit.gen5.engine.junit5.ATestClass$AnInnerTestClass$InnerInnerTestClass",
			uniqueId.getUniqueId());
		Assert.assertSame(ATestClass.AnInnerTestClass.InnerInnerTestClass.class, uniqueId.getJavaElement());
	}

	@org.junit.Test
	public void fromUniqueIdForMethod() throws NoSuchMethodException {

		JUnit5Testable uniqueId = JUnit5Testable.fromUniqueId("junit5:org.junit.gen5.engine.junit5.ATestClass#test1()",
			engineDescriptor.getUniqueId());
		Assert.assertEquals("junit5:org.junit.gen5.engine.junit5.ATestClass#test1()", uniqueId.getUniqueId());
		Method testMethod = ATestClass.class.getDeclaredMethod("test1", new Class[0]);
		Assert.assertEquals(testMethod, uniqueId.getJavaElement());
	}

	@org.junit.Test
	public void fromUniqueIdForMethodWithParameters() throws NoSuchMethodException {

		JUnit5Testable uniqueId = JUnit5Testable.fromUniqueId(
			"junit5:org.junit.gen5.engine.junit5.BTestClass#test4(java.lang.String,java.math.BigDecimal)",
			engineDescriptor.getUniqueId());
		Assert.assertEquals(
			"junit5:org.junit.gen5.engine.junit5.BTestClass#test4(java.lang.String,java.math.BigDecimal)",
			uniqueId.getUniqueId());
		Method testMethod = BTestClass.class.getDeclaredMethod("test4", new Class[] { String.class, BigDecimal.class });
		Assert.assertEquals(testMethod, uniqueId.getJavaElement());
	}

	@org.junit.Test
	public void fromUniqueIdForMethodInNestedClass() throws NoSuchMethodException {

		JUnit5Testable uniqueId = JUnit5Testable.fromUniqueId(
			"junit5:org.junit.gen5.engine.junit5.ATestClass$AnInnerTestClass#test2()", engineDescriptor.getUniqueId());
		Assert.assertEquals("junit5:org.junit.gen5.engine.junit5.ATestClass$AnInnerTestClass#test2()",
			uniqueId.getUniqueId());
		Method testMethod = ATestClass.AnInnerTestClass.class.getDeclaredMethod("test2", new Class[0]);
		Assert.assertEquals(testMethod, uniqueId.getJavaElement());
	}

	@org.junit.Test
	public void fromClassName() throws NoSuchMethodException {
		JUnit5Testable uniqueId = JUnit5Testable.fromClassName("org.junit.gen5.engine.junit5.ATestClass",
			engineDescriptor.getUniqueId());
		Assert.assertEquals("junit5:org.junit.gen5.engine.junit5.ATestClass", uniqueId.getUniqueId());
		Assert.assertSame(ATestClass.class, uniqueId.getJavaElement());
	}

	@org.junit.Test
	public void nestedClassFromClassName() throws NoSuchMethodException {
		JUnit5Testable uniqueId = JUnit5Testable.fromClassName(
			"org.junit.gen5.engine.junit5.ATestClass$AnInnerTestClass", engineDescriptor.getUniqueId());
		Assert.assertEquals("junit5:org.junit.gen5.engine.junit5.ATestClass$AnInnerTestClass", uniqueId.getUniqueId());
		Assert.assertSame(ATestClass.AnInnerTestClass.class, uniqueId.getJavaElement());
	}

	@org.junit.Test
	public void fromMethod() throws NoSuchMethodException {
		Method testMethod = ATestClass.class.getDeclaredMethod("test1", new Class[0]);
		JUnit5Testable uniqueId = JUnit5Testable.fromMethod(testMethod, ATestClass.class,
			engineDescriptor.getUniqueId());
		Assert.assertEquals("junit5:org.junit.gen5.engine.junit5.ATestClass#test1()", uniqueId.getUniqueId());
		Assert.assertSame(testMethod, uniqueId.getJavaElement());
		Assert.assertSame(ATestClass.class, uniqueId.getJavaContainer());
	}

	@org.junit.Test
	public void fromMethodWithParameters() throws NoSuchMethodException {
		Method testMethod = BTestClass.class.getDeclaredMethod("test4", String.class, BigDecimal.class);
		JUnit5Testable uniqueId = JUnit5Testable.fromMethod(testMethod, BTestClass.class,
			engineDescriptor.getUniqueId());
		Assert.assertEquals(
			"junit5:org.junit.gen5.engine.junit5.BTestClass#test4(java.lang.String,java.math.BigDecimal)",
			uniqueId.getUniqueId());
		Assert.assertSame(testMethod, uniqueId.getJavaElement());
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