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

import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertSame;
import static org.junit.gen5.api.Assertions.assertThrows;
import static org.junit.gen5.api.Assertions.assertTrue;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.gen5.api.Nested;
import org.junit.gen5.api.Test;
import org.junit.gen5.commons.util.PreconditionViolationException;
import org.junit.gen5.engine.DummyTestEngine;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.specification.ClassSpecification;
import org.junit.gen5.engine.specification.MethodSpecification;
import org.junit.gen5.engine.specification.PackageSpecification;
import org.junit.gen5.engine.specification.UniqueIdSpecification;

public class SpecificationResolverTests {

	private final JUnit5EngineDescriptor engineDescriptor = new JUnit5EngineDescriptor(
		new DummyTestEngine("ENGINE_ID"));
	private SpecificationResolver resolver = new SpecificationResolver(engineDescriptor);

	@Test
	public void testSingleClassResolution() {
		ClassSpecification specification = new ClassSpecification(MyTestClass.class);

		resolver.resolveElement(specification);

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test2()"));
	}

	@Test
	public void testTwoClassesResolution() {
		ClassSpecification specification1 = new ClassSpecification(MyTestClass.class);
		ClassSpecification specification2 = new ClassSpecification(YourTestClass.class);

		resolver.resolveElement(specification1);
		resolver.resolveElement(specification2);

		assertEquals(6, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test2()"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.YourTestClass"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.YourTestClass#test3()"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.YourTestClass#test4()"));
	}

	@Test
	public void testClassResolutionOfNestedClass() {
		ClassSpecification specification = new ClassSpecification(OtherTestClass.NestedTestClass.class);

		resolver.resolveElement(specification);

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertTrue(
			uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass#test5()"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass#test6()"));
	}

	@Test
	public void testMethodResolution() throws NoSuchMethodException {
		MethodSpecification specification = new MethodSpecification(
			MyTestClass.class.getDeclaredMethod("test1").getDeclaringClass(),
			MyTestClass.class.getDeclaredMethod("test1"));

		resolver.resolveElement(specification);

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()"));
	}

	@Test
	public void testMethodResolutionFromInheritedMethod() throws NoSuchMethodException {
		MethodSpecification specification = new MethodSpecification(HerTestClass.class,
			MyTestClass.class.getDeclaredMethod("test1"));

		resolver.resolveElement(specification);

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.HerTestClass"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.HerTestClass#test1()"));
	}

	@Test
	public void testResolutionOfNotTestMethod() throws NoSuchMethodException {
		MethodSpecification specification = new MethodSpecification(
			MyTestClass.class.getDeclaredMethod("notATest").getDeclaringClass(),
			MyTestClass.class.getDeclaredMethod("notATest"));
		assertThrows(PreconditionViolationException.class, () -> resolver.resolveElement(specification));
	}

	@Test
	public void testClassResolutionByUniqueId() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass");

		resolver.resolveElement(specification);

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test2()"));
	}

	@Test
	public void testInnerClassResolutionByUniqueId() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass");

		resolver.resolveElement(specification);

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(
			uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass#test5()"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass#test6()"));
	}

	@Test
	public void testMethodOfInnerClassByUniqueId() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass#test5()");

		resolver.resolveElement(specification);

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(
			uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass#test5()"));
	}

	@Test
	public void testNonResolvableUniqueId() {
		UniqueIdSpecification specification = new UniqueIdSpecification("ENGINE_ID:poops-machine");

		assertThrows(PreconditionViolationException.class, () -> resolver.resolveElement(specification));
	}

	@Test
	public void testUniqueIdOfNotTestMethod() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#notATest()");

		assertThrows(PreconditionViolationException.class, () -> resolver.resolveElement(specification));
	}

	@Test
	public void testMethodResolutionByUniqueId() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()");

		resolver.resolveElement(specification);

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()"));
	}

	@Test
	public void testMethodResolutionByUniqueIdFromInheritedClass() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.HerTestClass#test1()");

		resolver.resolveElement(specification);

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();

		// System.out.println(uniqueIds);
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.HerTestClass"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.HerTestClass#test1()"));
	}

	@Test
	public void testMethodResolutionByUniqueIdWithParams() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.HerTestClass#test7(java.lang.String)");

		resolver.resolveElement(specification);

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();

		// System.out.println(uniqueIds);
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.HerTestClass"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.HerTestClass#test7(java.lang.String)"));
	}

	@Test
	public void testMethodResolutionByUniqueIdWithWrongParams() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.HerTestClass#test7(java.math.BigDecimal)");

		assertThrows(PreconditionViolationException.class, () -> resolver.resolveElement(specification));
	}

	@Test
	public void testTwoMethodResolutionsByUniqueId() {
		UniqueIdSpecification specification1 = new UniqueIdSpecification(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()");
		UniqueIdSpecification specification2 = new UniqueIdSpecification(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test2()");

		resolver.resolveElement(specification1);
		resolver.resolveElement(specification2);
		resolver.resolveElement(specification2); // should have no effect

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test2()"));

		TestDescriptor classFromMethod1 = descriptorByUniqueId(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()").getParent().get();
		TestDescriptor classFromMethod2 = descriptorByUniqueId(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test2()").getParent().get();

		assertEquals(classFromMethod1, classFromMethod2);
		assertSame(classFromMethod1, classFromMethod2);
	}

	@Test
	public void testPackageResolution() {
		PackageSpecification specification = new PackageSpecification(
			"org.junit.gen5.engine.junit5.descriptor.subpackage");
		resolver.resolveElement(specification);

		assertEquals(6, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(
			uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.subpackage.Class1WithTestCases"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.subpackage.Class1WithTestCases#test1()"));
		assertTrue(
			uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.subpackage.Class2WithTestCases"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.subpackage.Class2WithTestCases#test2()"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.subpackage.ClassWithStaticInnerTestCases$ShouldBeDiscovered#test1()"));
	}

	@Test
	public void testNestedTestResolutionFromBaseClass() {
		ClassSpecification specification = new ClassSpecification(TestCaseWithNesting.class);

		resolver.resolveElement(specification);

		// engineDescriptor.allDescendants().stream().forEach(d -> System.out.println(d));

		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertEquals(6, uniqueIds.size());

		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting#testA()"));
		assertTrue(
			uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest#testB()"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest#testC()"));
	}

	@Test
	public void testNestedTestResolutionFromNestedTestClass() {
		ClassSpecification specification = new ClassSpecification(TestCaseWithNesting.NestedTest.class);

		resolver.resolveElement(specification);

		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertEquals(5, uniqueIds.size());

		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting"));
		assertTrue(
			uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest#testB()"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest#testC()"));
	}

	@Test
	public void testNestedTestResolutionFromUniqueId() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest");

		resolver.resolveElement(specification);

		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertEquals(4, uniqueIds.size());

		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting"));
		assertTrue(
			uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest#testC()"));
	}

	@Test
	public void testNestedTestResolutionFromClass() {
		ClassSpecification specification = new ClassSpecification(
			TestCaseWithNesting.NestedTest.DoubleNestedTest.class);

		resolver.resolveElement(specification);

		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertEquals(4, uniqueIds.size());

		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting"));
		assertTrue(
			uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest#testC()"));
	}

	@Test
	public void testNestedTestResolutionFromUniqueIdToMethod() {
		UniqueIdSpecification specification = new UniqueIdSpecification(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest#testB()");

		resolver.resolveElement(specification);

		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertEquals(3, uniqueIds.size());

		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting"));
		assertTrue(
			uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest#testB()"));
	}

	private TestDescriptor descriptorByUniqueId(String id) {
		return engineDescriptor.allDescendants().stream().filter(d -> d.getUniqueId().equals(id)).findFirst().get();
	}

	private List<String> uniqueIds() {
		return engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(Collectors.toList());
	}

}

class MyTestClass {

	@Test
	void test1() {

	}

	@Test
	void test2() {

	}

	void notATest() {

	}
}

class YourTestClass {

	@Test
	void test3() {

	}

	@Test
	void test4() {

	}

}

class HerTestClass extends MyTestClass {

	@Test
	void test7(String param) {

	}
}

class OtherTestClass {

	static class NestedTestClass {

		@Test
		void test5() {

		}

		@Test
		void test6() {

		}

	}
}

class TestCaseWithNesting {

	@Test
	void testA() {

	}

	@Nested
	class NestedTest {

		@Test
		void testB() {

		}

		@Nested
		class DoubleNestedTest {

			@Test
			void testC() {

			}

		}

	}
}
