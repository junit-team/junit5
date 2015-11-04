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

import org.junit.Assert;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.junit5.descriptor.UniqueId;

public class UniqueIdTest {

	EngineDescriptor engineDescriptor = new EngineDescriptor(new JUnit5TestEngine());

	@org.junit.Test
	public void fromUniqueIdForTopLevelClass() {
		UniqueId uniqueId = UniqueId.fromUniqueId("junit5:org.junit.gen5.engine.junit5.ATestClass", engineDescriptor);
		Assert.assertEquals("junit5:org.junit.gen5.engine.junit5.ATestClass", uniqueId.getUniqueId());
		Assert.assertSame(ATestClass.class, uniqueId.getJavaElement());
	}

	@org.junit.Test
	public void fromUniqueIdForNestedClass() {
		UniqueId uniqueId = UniqueId.fromUniqueId("junit5:org.junit.gen5.engine.junit5.ATestClass$AnInnerTestClass",
			engineDescriptor);
		Assert.assertEquals("junit5:org.junit.gen5.engine.junit5.ATestClass$AnInnerTestClass", uniqueId.getUniqueId());
		Assert.assertSame(ATestClass.AnInnerTestClass.class, uniqueId.getJavaElement());
	}

	@org.junit.Test
	public void fromUniqueIdForDoubleNestedClass() {
		UniqueId uniqueId = UniqueId.fromUniqueId(
			"junit5:org.junit.gen5.engine.junit5.ATestClass$AnInnerTestClass$InnerInnerTestClass", engineDescriptor);
		Assert.assertEquals("junit5:org.junit.gen5.engine.junit5.ATestClass$AnInnerTestClass$InnerInnerTestClass",
			uniqueId.getUniqueId());
		Assert.assertSame(ATestClass.AnInnerTestClass.InnerInnerTestClass.class, uniqueId.getJavaElement());
	}

	@org.junit.Test
	public void fromUniqueIdForMethod() throws NoSuchMethodException {
		UniqueId uniqueId = UniqueId.fromUniqueId("junit5:org.junit.gen5.engine.junit5.ATestClass#test1()",
			engineDescriptor);
		Assert.assertEquals("junit5:org.junit.gen5.engine.junit5.ATestClass#test1()", uniqueId.getUniqueId());
		Method testMethod = ATestClass.class.getDeclaredMethod("test1", new Class[0]);
		Assert.assertEquals(testMethod, uniqueId.getJavaElement());
	}

	@org.junit.Test
	public void fromUniqueIdForMethodInNestedClass() throws NoSuchMethodException {
		UniqueId uniqueId = UniqueId.fromUniqueId(
			"junit5:org.junit.gen5.engine.junit5.ATestClass$AnInnerTestClass#test2()", engineDescriptor);
		Assert.assertEquals("junit5:org.junit.gen5.engine.junit5.ATestClass$AnInnerTestClass#test2()",
			uniqueId.getUniqueId());
		Method testMethod = ATestClass.AnInnerTestClass.class.getDeclaredMethod("test2", new Class[0]);
		Assert.assertEquals(testMethod, uniqueId.getJavaElement());
	}

	@org.junit.Test
	public void fromClassName() throws NoSuchMethodException {
		UniqueId uniqueId = UniqueId.fromClassName("org.junit.gen5.engine.junit5.ATestClass", engineDescriptor);
		Assert.assertEquals("junit5:org.junit.gen5.engine.junit5.ATestClass", uniqueId.getUniqueId());
		Assert.assertSame(ATestClass.class, uniqueId.getJavaElement());
	}

	@org.junit.Test
	public void nestedClassFromClassName() throws NoSuchMethodException {
		UniqueId uniqueId = UniqueId.fromClassName("org.junit.gen5.engine.junit5.ATestClass$AnInnerTestClass",
			engineDescriptor);
		Assert.assertEquals("junit5:org.junit.gen5.engine.junit5.ATestClass$AnInnerTestClass", uniqueId.getUniqueId());
		Assert.assertSame(ATestClass.AnInnerTestClass.class, uniqueId.getJavaElement());
	}

	@org.junit.Test
	public void fromMethod() throws NoSuchMethodException {
		Method testMethod = ATestClass.class.getDeclaredMethod("test1", new Class[0]);
		UniqueId uniqueId = UniqueId.fromMethod(testMethod, ATestClass.class, engineDescriptor);
		Assert.assertEquals("junit5:org.junit.gen5.engine.junit5.ATestClass#test1()", uniqueId.getUniqueId());
		Assert.assertSame(testMethod, uniqueId.getJavaElement());
	}

	@org.junit.Test
	public void fromMethodInSubclass() throws NoSuchMethodException {
		Method testMethod = ATestClass.class.getDeclaredMethod("test1", new Class[0]);
		UniqueId uniqueId = UniqueId.fromMethod(testMethod, BTestClass.class, engineDescriptor);
		Assert.assertEquals("junit5:org.junit.gen5.engine.junit5.BTestClass#test1()", uniqueId.getUniqueId());
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

}