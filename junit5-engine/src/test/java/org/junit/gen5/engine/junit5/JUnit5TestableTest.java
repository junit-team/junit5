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
import org.junit.gen5.engine.junit5.descriptor.JUnit5Class;
import org.junit.gen5.engine.junit5.descriptor.JUnit5Method;

public class JUnit5TestableTest {
	private final JUnit5TestEngine testEngine = new JUnit5TestEngine();

	@org.junit.Test
	public void fromUniqueIdForTopLevelClass() {

		JUnit5Class testable = (JUnit5Class) JUnit5TestEngine.fromUniqueId(
				"junit5:org.junit.gen5.engine.junit5.ATestClass");
		Assert.assertEquals("junit5:org.junit.gen5.engine.junit5.ATestClass", testable.getUniqueId());
		Assert.assertSame(ATestClass.class, testable.getJavaClass());
	}

	private String getEngineId() {
		return testEngine.getId();
	}

	@org.junit.Test
	public void fromUniqueIdForNestedClass() {

		JUnit5Class testable = (JUnit5Class) JUnit5TestEngine.fromUniqueId(
				"junit5:org.junit.gen5.engine.junit5.ATestClass$AnInnerTestClass");
		Assert.assertEquals("junit5:org.junit.gen5.engine.junit5.ATestClass$AnInnerTestClass", testable.getUniqueId());
		Assert.assertSame(ATestClass.AnInnerTestClass.class, testable.getJavaClass());
	}

	@org.junit.Test
	public void fromUniqueIdForDoubleNestedClass() {

		JUnit5Class testable = (JUnit5Class) JUnit5TestEngine.fromUniqueId(
				"junit5:org.junit.gen5.engine.junit5.ATestClass$AnInnerTestClass$InnerInnerTestClass"
		);
		Assert.assertEquals("junit5:org.junit.gen5.engine.junit5.ATestClass$AnInnerTestClass$InnerInnerTestClass",
			testable.getUniqueId());
		Assert.assertSame(ATestClass.AnInnerTestClass.InnerInnerTestClass.class, testable.getJavaClass());
	}

	@org.junit.Test
	public void fromUniqueIdForMethod() throws NoSuchMethodException {

		JUnit5Method testable = (JUnit5Method) JUnit5TestEngine.fromUniqueId(
				"junit5:org.junit.gen5.engine.junit5.ATestClass#test1()");
		Assert.assertEquals("junit5:org.junit.gen5.engine.junit5.ATestClass#test1()", testable.getUniqueId());
		Method testMethod = ATestClass.class.getDeclaredMethod("test1", new Class[0]);
		Assert.assertEquals(testMethod, testable.getJavaMethod());
	}

	@org.junit.Test
	public void fromUniqueIdForMethodWithParameters() throws NoSuchMethodException {

		JUnit5Method testable = (JUnit5Method) JUnit5TestEngine.fromUniqueId(
				"junit5:org.junit.gen5.engine.junit5.BTestClass#test4(java.lang.String,java.math.BigDecimal)"
		);
		Assert.assertEquals(
			"junit5:org.junit.gen5.engine.junit5.BTestClass#test4(java.lang.String,java.math.BigDecimal)",
			testable.getUniqueId());
		Method testMethod = BTestClass.class.getDeclaredMethod("test4", new Class[] { String.class, BigDecimal.class });
		Assert.assertEquals(testMethod, testable.getJavaMethod());
	}

	@org.junit.Test
	public void fromUniqueIdForMethodInNestedClass() throws NoSuchMethodException {

		JUnit5Method testable = (JUnit5Method) JUnit5TestEngine.fromUniqueId(
				"junit5:org.junit.gen5.engine.junit5.ATestClass$AnInnerTestClass#test2()");
		Assert.assertEquals("junit5:org.junit.gen5.engine.junit5.ATestClass$AnInnerTestClass#test2()",
			testable.getUniqueId());
		Method testMethod = ATestClass.AnInnerTestClass.class.getDeclaredMethod("test2", new Class[0]);
		Assert.assertEquals(testMethod, testable.getJavaMethod());
	}

	@org.junit.Test
	public void fromClassName() throws NoSuchMethodException {
		JUnit5Class testable = (JUnit5Class) JUnit5TestEngine.fromClassName("org.junit.gen5.engine.junit5.ATestClass"
		);
		Assert.assertEquals("junit5:org.junit.gen5.engine.junit5.ATestClass", testable.getUniqueId());
		Assert.assertSame(ATestClass.class, testable.getJavaClass());
	}

	@org.junit.Test
	public void nestedClassFromClassName() throws NoSuchMethodException {
		JUnit5Class testable = (JUnit5Class) JUnit5TestEngine.fromClassName(
				"org.junit.gen5.engine.junit5.ATestClass$AnInnerTestClass");
		Assert.assertEquals("junit5:org.junit.gen5.engine.junit5.ATestClass$AnInnerTestClass", testable.getUniqueId());
		Assert.assertSame(ATestClass.AnInnerTestClass.class, testable.getJavaClass());
	}

	@org.junit.Test
	public void fromMethod() throws NoSuchMethodException {
		Method testMethod = ATestClass.class.getDeclaredMethod("test1", new Class[0]);
		JUnit5Method testable = (JUnit5Method) JUnit5TestEngine.fromMethod(testMethod, ATestClass.class
		);
		Assert.assertEquals("junit5:org.junit.gen5.engine.junit5.ATestClass#test1()", testable.getUniqueId());
		Assert.assertSame(testMethod, testable.getJavaMethod());
		Assert.assertSame(ATestClass.class, testable.getContainerClass());
	}

	@org.junit.Test
	public void fromMethodWithParameters() throws NoSuchMethodException {
		Method testMethod = BTestClass.class.getDeclaredMethod("test4", String.class, BigDecimal.class);
		JUnit5Method testable = (JUnit5Method) JUnit5TestEngine.fromMethod(testMethod, BTestClass.class
		);
		Assert.assertEquals(
			"junit5:org.junit.gen5.engine.junit5.BTestClass#test4(java.lang.String,java.math.BigDecimal)",
			testable.getUniqueId());
		Assert.assertSame(testMethod, testable.getJavaMethod());
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