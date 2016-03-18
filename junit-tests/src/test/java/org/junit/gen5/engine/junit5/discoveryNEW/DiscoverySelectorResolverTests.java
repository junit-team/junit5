/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.discoveryNEW;

import static org.junit.gen5.api.Assertions.*;
import static org.junit.gen5.engine.junit5.discovery.JUnit5UniqueIdBuilder.*;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.gen5.api.Nested;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.discovery.ClassSelector;
import org.junit.gen5.engine.discovery.MethodSelector;
import org.junit.gen5.engine.discovery.PackageSelector;
import org.junit.gen5.engine.discovery.UniqueIdSelector;
import org.junit.gen5.engine.junit5.discovery.JUnit5EngineDescriptor;
import org.junit.gen5.junit4.runner.JUnit5;
import org.junit.runner.RunWith;

@RunWith(JUnit5.class)
public class DiscoverySelectorResolverTests {

	private final JUnit5EngineDescriptor engineDescriptor = new JUnit5EngineDescriptor(engineId());
	private DiscoverySelectorResolver resolver = new DiscoverySelectorResolver();

	@Test
	public void testSingleClassResolution() {
		ClassSelector selector = ClassSelector.forClass(MyTestClass.class);

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(MyTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test1()")));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test2()")));
	}

	@Test
	public void duplicateClassSelectorOnlyResolvesOnce() {
		resolver.resolveSelectors(request().select( //
			ClassSelector.forClass(MyTestClass.class), //
			ClassSelector.forClass(MyTestClass.class) //
		).build(), engineDescriptor);

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(MyTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test1()")));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test2()")));
	}

	@Test
	public void testTwoClassesResolution() {
		ClassSelector selector1 = ClassSelector.forClass(MyTestClass.class);
		ClassSelector selector2 = ClassSelector.forClass(YourTestClass.class);

		resolver.resolveSelectors(request().select(selector1, selector2).build(), engineDescriptor);

		assertEquals(6, engineDescriptor.allDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(MyTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test1()")));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test2()")));
		assertTrue(uniqueIds.contains(uniqueIdForClass(YourTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(YourTestClass.class, "test3()")));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(YourTestClass.class, "test4()")));
	}

	@Test
	public void testClassResolutionOfStaticNestedClass() {
		ClassSelector selector = ClassSelector.forClass(OtherTestClass.NestedTestClass.class);

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(OtherTestClass.NestedTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test5()")));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test6()")));
	}

	@Test
	public void testMethodResolution() throws NoSuchMethodException {
		MethodSelector selector = MethodSelector.forMethod(
			MyTestClass.class.getDeclaredMethod("test1").getDeclaringClass(),
			MyTestClass.class.getDeclaredMethod("test1"));

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(MyTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test1()")));
	}

	@Test
	public void testMethodResolutionFromInheritedMethod() throws NoSuchMethodException {
		MethodSelector selector = MethodSelector.forMethod(HerTestClass.class,
			MyTestClass.class.getDeclaredMethod("test1"));

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(HerTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(HerTestClass.class, "test1()")));
	}

	@Test
	public void resolvingSelectorOfNonTestMethodResolvesNothing() throws NoSuchMethodException {
		MethodSelector selector = MethodSelector.forMethod(
			MyTestClass.class.getDeclaredMethod("notATest").getDeclaringClass(),
			MyTestClass.class.getDeclaredMethod("notATest"));
		EngineDiscoveryRequest request = request().select(selector).build();
		resolver.resolveSelectors(request, engineDescriptor);
		assertTrue(engineDescriptor.allDescendants().isEmpty());
	}

	@Test
	public void testClassResolutionByUniqueId() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(uniqueIdForClass(MyTestClass.class).getUniqueString());

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(MyTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test1()")));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test2()")));
	}

	@Test
	public void testStaticNestedClassResolutionByUniqueId() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForClass(OtherTestClass.NestedTestClass.class).getUniqueString());

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(OtherTestClass.NestedTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test5()")));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test6()")));
	}

	@Test
	public void testMethodOfInnerClassByUniqueId() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test5()").getUniqueString());

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(OtherTestClass.NestedTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test5()")));
	}

	@Test
	public void resolvingUniqueIdWithUnknownSegmentTypeResolvesNothing() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			engineId().append("poops", "machine").getUniqueString());
		EngineDiscoveryRequest request = request().select(selector).build();

		resolver.resolveSelectors(request, engineDescriptor);
		assertTrue(engineDescriptor.allDescendants().isEmpty());
	}

	@Test
	public void resolvingUniqueIdOfNonTestMethodResolvesNothing() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(uniqueIdForMethod(MyTestClass.class, "notATest()"));
		EngineDiscoveryRequest request = request().select(selector).build();

		resolver.resolveSelectors(request, engineDescriptor);
		assertTrue(engineDescriptor.allDescendants().isEmpty());
	}

	@Test
	public void testMethodResolutionByUniqueId() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForMethod(MyTestClass.class, "test1()").getUniqueString());

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(MyTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test1()")));
	}

	@Test
	public void testMethodResolutionByUniqueIdFromInheritedClass() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForMethod(HerTestClass.class, "test1()").getUniqueString());

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();

		assertTrue(uniqueIds.contains(uniqueIdForClass(HerTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(HerTestClass.class, "test1()")));
	}

	@Test
	public void testMethodResolutionByUniqueIdWithParams() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForMethod(HerTestClass.class, "test7(java.lang.String)").getUniqueString());

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(HerTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(HerTestClass.class, "test7(java.lang.String)")));
	}

	@Test
	public void resolvingUniqueIdWithWrongParamsResolvesNothing() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForMethod(HerTestClass.class, "test7(java.math.BigDecimal)").getUniqueString());
		EngineDiscoveryRequest request = request().select(selector).build();

		resolver.resolveSelectors(request, engineDescriptor);
		assertTrue(engineDescriptor.allDescendants().isEmpty());
	}

	@Test
	public void testTwoMethodResolutionsByUniqueId() {
		UniqueIdSelector selector1 = UniqueIdSelector.forUniqueId(
			uniqueIdForMethod(MyTestClass.class, "test1()").getUniqueString());
		UniqueIdSelector selector2 = UniqueIdSelector.forUniqueId(
			uniqueIdForMethod(MyTestClass.class, "test2()").getUniqueString());

		// adding same selector twice should have no effect
		resolver.resolveSelectors(request().select(selector1, selector2, selector2).build(), engineDescriptor);

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(MyTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test1()")));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test2()")));

		TestDescriptor classFromMethod1 = descriptorByUniqueId(
			uniqueIdForMethod(MyTestClass.class, "test1()")).getParent().get();
		TestDescriptor classFromMethod2 = descriptorByUniqueId(
			uniqueIdForMethod(MyTestClass.class, "test2()")).getParent().get();

		assertEquals(classFromMethod1, classFromMethod2);
		assertSame(classFromMethod1, classFromMethod2);
	}

	//	@Test
	public void testPackageResolution() {
		PackageSelector selector = PackageSelector.forPackageName("org.junit.gen5.engine.junit5.descriptor.subpackage");

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		assertEquals(6, engineDescriptor.allDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(
			uniqueIdForClass(org.junit.gen5.engine.junit5.descriptor.subpackage.Class1WithTestCases.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(
			org.junit.gen5.engine.junit5.descriptor.subpackage.Class1WithTestCases.class, "test1()")));
		assertTrue(uniqueIds.contains(
			uniqueIdForClass(org.junit.gen5.engine.junit5.descriptor.subpackage.Class2WithTestCases.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(
			org.junit.gen5.engine.junit5.descriptor.subpackage.Class2WithTestCases.class, "test2()")));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(
			org.junit.gen5.engine.junit5.descriptor.subpackage.ClassWithStaticInnerTestCases.ShouldBeDiscovered.class,
			"test1()")));
	}

	@Test
	public void testNestedTestResolutionFromBaseClass() {
		ClassSelector selector = ClassSelector.forClass(TestCaseWithNesting.class);

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		List<UniqueId> uniqueIds = uniqueIds();
		assertEquals(6, uniqueIds.size());

		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(TestCaseWithNesting.class, "testA()")));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTest.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(TestCaseWithNesting.NestedTest.class, "testB()")));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTest.DoubleNestedTest.class)));
		assertTrue(
			uniqueIds.contains(uniqueIdForMethod(TestCaseWithNesting.NestedTest.DoubleNestedTest.class, "testC()")));
	}

	@Test
	public void testNestedTestResolutionFromNestedTestClass() {
		ClassSelector selector = ClassSelector.forClass(TestCaseWithNesting.NestedTest.class);

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		List<UniqueId> uniqueIds = uniqueIds();
		assertEquals(5, uniqueIds.size());

		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.class)));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTest.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(TestCaseWithNesting.NestedTest.class, "testB()")));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTest.DoubleNestedTest.class)));
		assertTrue(
			uniqueIds.contains(uniqueIdForMethod(TestCaseWithNesting.NestedTest.DoubleNestedTest.class, "testC()")));
	}

	@Test
	public void testNestedTestResolutionFromUniqueId() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForClass(TestCaseWithNesting.NestedTest.DoubleNestedTest.class).getUniqueString());

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		List<UniqueId> uniqueIds = uniqueIds();
		assertEquals(4, uniqueIds.size());

		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.class)));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTest.class)));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTest.DoubleNestedTest.class)));
		assertTrue(
			uniqueIds.contains(uniqueIdForMethod(TestCaseWithNesting.NestedTest.DoubleNestedTest.class, "testC()")));
	}

	@Test
	public void testDoubleNestedTestResolutionFromClass() {
		ClassSelector selector = ClassSelector.forClass(TestCaseWithNesting.NestedTest.DoubleNestedTest.class);

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		List<UniqueId> uniqueIds = uniqueIds();
		assertEquals(4, uniqueIds.size());

		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.class)));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTest.class)));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTest.DoubleNestedTest.class)));
		assertTrue(
			uniqueIds.contains(uniqueIdForMethod(TestCaseWithNesting.NestedTest.DoubleNestedTest.class, "testC()")));
	}

	@Test
	public void testMethodResolutionInDoubleNestedTestClass() throws NoSuchMethodException {
		MethodSelector selector = MethodSelector.forMethod(TestCaseWithNesting.NestedTest.DoubleNestedTest.class,
			TestCaseWithNesting.NestedTest.DoubleNestedTest.class.getDeclaredMethod("testC"));

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		assertEquals(4, engineDescriptor.allDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.class)));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTest.class)));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTest.DoubleNestedTest.class)));
		assertTrue(
			uniqueIds.contains(uniqueIdForMethod(TestCaseWithNesting.NestedTest.DoubleNestedTest.class, "testC()")));
	}

	@Test
	public void testNestedTestResolutionFromUniqueIdToMethod() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForMethod(TestCaseWithNesting.NestedTest.class, "testB()").getUniqueString());

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		List<UniqueId> uniqueIds = uniqueIds();
		assertEquals(3, uniqueIds.size());
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.class)));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTest.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(TestCaseWithNesting.NestedTest.class, "testB()")));
	}

	private TestDescriptor descriptorByUniqueId(UniqueId uniqueId) {
		return engineDescriptor.allDescendants().stream().filter(
			d -> d.getUniqueId().equals(uniqueId)).findFirst().get();
	}

	private List<UniqueId> uniqueIds() {
		return engineDescriptor.allDescendants().stream().map(TestDescriptor::getUniqueId).collect(Collectors.toList());
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
