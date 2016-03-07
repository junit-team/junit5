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
import static org.junit.gen5.engine.junit5.discovery.UniqueIdBuilder.*;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.gen5.api.Nested;
import org.junit.gen5.api.Test;
import org.junit.gen5.commons.util.PreconditionViolationException;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.discovery.ClassSelector;
import org.junit.gen5.engine.discovery.MethodSelector;
import org.junit.gen5.engine.discovery.PackageSelector;
import org.junit.gen5.engine.discovery.UniqueIdSelector;
import org.junit.gen5.engine.junit5.JUnit5TestEngine;

public class DiscoverySelectorResolverTests {

	private final JUnit5EngineDescriptor engineDescriptor = new JUnit5EngineDescriptor(JUnit5TestEngine.ENGINE_ID);
	private DiscoverySelectorResolver resolver = new DiscoverySelectorResolver(engineDescriptor);

	@Test
	public void testSingleClassResolution() {
		ClassSelector selector = ClassSelector.forClass(MyTestClass.class);

		resolver.resolveSelectors(request().select(selector).build());

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(MyTestClass.class).getUniqueString()));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test1()").getUniqueString()));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test2()").getUniqueString()));
	}

	@Test
	public void testTwoClassesResolution() {
		ClassSelector selector1 = ClassSelector.forClass(MyTestClass.class);
		ClassSelector selector2 = ClassSelector.forClass(YourTestClass.class);

		resolver.resolveSelectors(request().select(selector1, selector2).build());

		assertEquals(6, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertTrue(uniqueIds.contains(uniqueIdForClass(MyTestClass.class).getUniqueString()));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test1()").getUniqueString()));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test2()").getUniqueString()));
		assertTrue(uniqueIds.contains(uniqueIdForClass(YourTestClass.class).getUniqueString()));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(YourTestClass.class, "test3()").getUniqueString()));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(YourTestClass.class, "test4()").getUniqueString()));
	}

	@Test
	public void testClassResolutionOfNestedClass() {
		ClassSelector selector = ClassSelector.forClass(OtherTestClass.NestedTestClass.class);

		resolver.resolveSelectors(request().select(selector).build());

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertTrue(uniqueIds.contains(uniqueIdForClass(OtherTestClass.NestedTestClass.class).getUniqueString()));
		assertTrue(
			uniqueIds.contains(uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test5()").getUniqueString()));
		assertTrue(
			uniqueIds.contains(uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test6()").getUniqueString()));
	}

	@Test
	public void testMethodResolution() throws NoSuchMethodException {
		MethodSelector selector = MethodSelector.forMethod(
			MyTestClass.class.getDeclaredMethod("test1").getDeclaringClass(),
			MyTestClass.class.getDeclaredMethod("test1"));

		resolver.resolveSelectors(request().select(selector).build());

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(MyTestClass.class).getUniqueString()));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test1()").getUniqueString()));
	}

	@Test
	public void testMethodResolutionFromInheritedMethod() throws NoSuchMethodException {
		MethodSelector selector = MethodSelector.forMethod(HerTestClass.class,
			MyTestClass.class.getDeclaredMethod("test1"));

		resolver.resolveSelectors(request().select(selector).build());

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(HerTestClass.class).getUniqueString()));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(HerTestClass.class, "test1()").getUniqueString()));
	}

	@Test
	public void resolvingSelectorOfNonTestMethodResolvesNothing() throws NoSuchMethodException {
		MethodSelector selector = MethodSelector.forMethod(
			MyTestClass.class.getDeclaredMethod("notATest").getDeclaringClass(),
			MyTestClass.class.getDeclaredMethod("notATest"));
		EngineDiscoveryRequest request = request().select(selector).build();
		resolver.resolveSelectors(request);
		assertTrue(engineDescriptor.allDescendants().isEmpty());
	}

	@Test
	public void testClassResolutionByUniqueId() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(uniqueIdForClass(MyTestClass.class).getUniqueString());

		resolver.resolveSelectors(request().select(selector).build());

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		System.out.println(uniqueIds);
		assertTrue(uniqueIds.contains(uniqueIdForClass(MyTestClass.class).getUniqueString()));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test1()").getUniqueString()));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test2()").getUniqueString()));
	}

	@Test
	public void testInnerClassResolutionByUniqueId() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForClass(OtherTestClass.NestedTestClass.class).getUniqueString());

		resolver.resolveSelectors(request().select(selector).build());

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(OtherTestClass.NestedTestClass.class).getUniqueString()));
		assertTrue(
			uniqueIds.contains(uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test5()").getUniqueString()));
		assertTrue(
			uniqueIds.contains(uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test6()").getUniqueString()));
	}

	@Test
	public void testMethodOfInnerClassByUniqueId() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test5()").getUniqueString());

		resolver.resolveSelectors(request().select(selector).build());

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(OtherTestClass.NestedTestClass.class).getUniqueString()));
		assertTrue(
			uniqueIds.contains(uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test5()").getUniqueString()));
	}

	@Test
	public void testNonResolvableUniqueId() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			engineId().append("poops", "machine").getUniqueString());
		EngineDiscoveryRequest request = request().select(selector).build();

		PreconditionViolationException exception = expectThrows(PreconditionViolationException.class, () -> {
			resolver.resolveSelectors(request);
		});
		assertTrue(exception.getMessage().contains("Cannot resolve part '[poops:machine]'"),
			"Exception message wrong: " + exception.getMessage());
	}

	@Test
	public void resolvingUniqueIdOfNonTestMethodResolvesNothing() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForMethod(MyTestClass.class, "notATest()").getUniqueString());
		EngineDiscoveryRequest request = request().select(selector).build();

		resolver.resolveSelectors(request);
		assertTrue(engineDescriptor.allDescendants().isEmpty());
	}

	@Test
	public void testMethodResolutionByUniqueId() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForMethod(MyTestClass.class, "test1()").getUniqueString());

		resolver.resolveSelectors(request().select(selector).build());

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(MyTestClass.class).getUniqueString()));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test1()").getUniqueString()));
	}

	@Test
	public void testMethodResolutionByUniqueIdFromInheritedClass() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForMethod(HerTestClass.class, "test1()").getUniqueString());

		resolver.resolveSelectors(request().select(selector).build());

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();

		// System.out.println(uniqueIds);
		assertTrue(uniqueIds.contains(uniqueIdForClass(HerTestClass.class).getUniqueString()));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(HerTestClass.class, "test1()").getUniqueString()));
	}

	@Test
	public void testMethodResolutionByUniqueIdWithParams() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForMethod(HerTestClass.class, "test7(java.lang.String)").getUniqueString());

		resolver.resolveSelectors(request().select(selector).build());

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();

		// System.out.println(uniqueIds);
		assertTrue(uniqueIds.contains(uniqueIdForClass(HerTestClass.class).getUniqueString()));
		assertTrue(
			uniqueIds.contains(uniqueIdForMethod(HerTestClass.class, "test7(java.lang.String)").getUniqueString()));
	}

	@Test
	public void testMethodResolutionByUniqueIdWithWrongParams() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForMethod(HerTestClass.class, "test7(java.math.BigDecimal)").getUniqueString());
		EngineDiscoveryRequest request = request().select(selector).build();

		assertThrows(PreconditionViolationException.class, () -> resolver.resolveSelectors(request));
	}

	@Test
	public void testTwoMethodResolutionsByUniqueId() {
		UniqueIdSelector selector1 = UniqueIdSelector.forUniqueId(
			uniqueIdForMethod(MyTestClass.class, "test1()").getUniqueString());
		UniqueIdSelector selector2 = UniqueIdSelector.forUniqueId(
			uniqueIdForMethod(MyTestClass.class, "test2()").getUniqueString());

		// adding same selector twice should have no effect
		resolver.resolveSelectors(request().select(selector1, selector2, selector2).build());

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(MyTestClass.class).getUniqueString()));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test1()").getUniqueString()));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test2()").getUniqueString()));

		TestDescriptor classFromMethod1 = descriptorByUniqueId(
			uniqueIdForMethod(MyTestClass.class, "test1()").getUniqueString()).getParent().get();
		TestDescriptor classFromMethod2 = descriptorByUniqueId(
			uniqueIdForMethod(MyTestClass.class, "test2()").getUniqueString()).getParent().get();

		assertEquals(classFromMethod1, classFromMethod2);
		assertSame(classFromMethod1, classFromMethod2);
	}

	@Test
	public void testPackageResolution() {
		PackageSelector selector = PackageSelector.forPackageName("org.junit.gen5.engine.junit5.descriptor.subpackage");

		resolver.resolveSelectors(request().select(selector).build());

		assertEquals(6, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(
			org.junit.gen5.engine.junit5.descriptor.subpackage.Class1WithTestCases.class).getUniqueString()));
		assertTrue(uniqueIds.contains(
			uniqueIdForMethod(org.junit.gen5.engine.junit5.descriptor.subpackage.Class1WithTestCases.class,
				"test1()").getUniqueString()));
		assertTrue(uniqueIds.contains(uniqueIdForClass(
			org.junit.gen5.engine.junit5.descriptor.subpackage.Class2WithTestCases.class).getUniqueString()));
		assertTrue(uniqueIds.contains(
			uniqueIdForMethod(org.junit.gen5.engine.junit5.descriptor.subpackage.Class2WithTestCases.class,
				"test2()").getUniqueString()));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(
			org.junit.gen5.engine.junit5.descriptor.subpackage.ClassWithStaticInnerTestCases.ShouldBeDiscovered.class,
			"test1()").getUniqueString()));
	}

	@Test
	public void testNestedTestResolutionFromBaseClass() {
		ClassSelector selector = ClassSelector.forClass(TestCaseWithNesting.class);

		resolver.resolveSelectors(request().select(selector).build());

		// engineDescriptor.allDescendants().stream().forEach(d -> System.out.println(d));

		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertEquals(6, uniqueIds.size());

		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.class).getUniqueString()));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(TestCaseWithNesting.class, "testA()").getUniqueString()));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTest.class).getUniqueString()));
		assertTrue(
			uniqueIds.contains(uniqueIdForMethod(TestCaseWithNesting.NestedTest.class, "testB()").getUniqueString()));
		assertTrue(uniqueIds.contains(
			uniqueIdForClass(TestCaseWithNesting.NestedTest.DoubleNestedTest.class).getUniqueString()));
		assertTrue(uniqueIds.contains(
			uniqueIdForMethod(TestCaseWithNesting.NestedTest.DoubleNestedTest.class, "testC()").getUniqueString()));
	}

	@Test
	public void testNestedTestResolutionFromNestedTestClass() {
		ClassSelector selector = ClassSelector.forClass(TestCaseWithNesting.NestedTest.class);

		resolver.resolveSelectors(request().select(selector).build());

		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertEquals(5, uniqueIds.size());

		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.class).getUniqueString()));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTest.class).getUniqueString()));
		assertTrue(
			uniqueIds.contains(uniqueIdForMethod(TestCaseWithNesting.NestedTest.class, "testB()").getUniqueString()));
		assertTrue(uniqueIds.contains(
			uniqueIdForClass(TestCaseWithNesting.NestedTest.DoubleNestedTest.class).getUniqueString()));
		assertTrue(uniqueIds.contains(
			uniqueIdForMethod(TestCaseWithNesting.NestedTest.DoubleNestedTest.class, "testC()").getUniqueString()));
	}

	@Test
	public void testNestedTestResolutionFromUniqueId() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForClass(TestCaseWithNesting.NestedTest.DoubleNestedTest.class).getUniqueString());

		resolver.resolveSelectors(request().select(selector).build());

		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertEquals(4, uniqueIds.size());

		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.class).getUniqueString()));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTest.class).getUniqueString()));
		assertTrue(uniqueIds.contains(
			uniqueIdForClass(TestCaseWithNesting.NestedTest.DoubleNestedTest.class).getUniqueString()));
		assertTrue(uniqueIds.contains(
			uniqueIdForMethod(TestCaseWithNesting.NestedTest.DoubleNestedTest.class, "testC()").getUniqueString()));
	}

	@Test
	public void testNestedTestResolutionFromClass() {
		ClassSelector selector = ClassSelector.forClass(TestCaseWithNesting.NestedTest.DoubleNestedTest.class);

		resolver.resolveSelectors(request().select(selector).build());

		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertEquals(4, uniqueIds.size());

		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.class).getUniqueString()));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTest.class).getUniqueString()));
		assertTrue(uniqueIds.contains(
			uniqueIdForClass(TestCaseWithNesting.NestedTest.DoubleNestedTest.class).getUniqueString()));
		assertTrue(uniqueIds.contains(
			uniqueIdForMethod(TestCaseWithNesting.NestedTest.DoubleNestedTest.class, "testC()").getUniqueString()));
	}

	@Test
	public void testNestedTestResolutionFromUniqueIdToMethod() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForMethod(TestCaseWithNesting.NestedTest.class, "testB()").getUniqueString());

		resolver.resolveSelectors(request().select(selector).build());

		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertEquals(3, uniqueIds.size());

		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.class).getUniqueString()));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTest.class).getUniqueString()));
		assertTrue(
			uniqueIds.contains(uniqueIdForMethod(TestCaseWithNesting.NestedTest.class, "testB()").getUniqueString()));
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
