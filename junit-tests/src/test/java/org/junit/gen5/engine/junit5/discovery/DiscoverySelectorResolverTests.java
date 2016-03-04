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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertSame;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.junit.gen5.api.Assertions.expectThrows;
import static org.junit.gen5.engine.junit5.discovery.JUnit5UniqueIdBuilder.engineId;
import static org.junit.gen5.engine.junit5.discovery.JUnit5UniqueIdBuilder.uniqueIdForClass;
import static org.junit.gen5.engine.junit5.discovery.JUnit5UniqueIdBuilder.uniqueIdForMethod;
import static org.junit.gen5.engine.junit5.discovery.JUnit5UniqueIdBuilder.uniqueIdForTestFactoryMethod;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.gen5.api.DynamicTest;
import org.junit.gen5.api.Nested;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.TestFactory;
import org.junit.gen5.commons.JUnitException;
import org.junit.gen5.commons.util.PreconditionViolationException;
import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.discovery.ClassSelector;
import org.junit.gen5.engine.discovery.ClasspathSelector;
import org.junit.gen5.engine.discovery.MethodSelector;
import org.junit.gen5.engine.discovery.PackageSelector;
import org.junit.gen5.engine.discovery.UniqueIdSelector;
import org.junit.gen5.engine.junit5.descriptor.TestFactoryTestDescriptor;

/**
 * @since 5.0
 */
public class DiscoverySelectorResolverTests {

	private final JUnit5EngineDescriptor engineDescriptor = new JUnit5EngineDescriptor(engineId());
	private DiscoverySelectorResolver resolver = new DiscoverySelectorResolver();

	@Test
	public void singleClassResolution() {
		ClassSelector selector = ClassSelector.forClass(MyTestClass.class);

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		assertEquals(4, engineDescriptor.allDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(MyTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test1()")));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test2()")));
		assertTrue(uniqueIds.contains(uniqueIdForTestFactoryMethod(MyTestClass.class, "dynamicTest()")));
	}

	@Test
	public void duplicateClassSelectorOnlyResolvesOnce() {
		resolver.resolveSelectors(request().select( //
			ClassSelector.forClass(MyTestClass.class), //
			ClassSelector.forClass(MyTestClass.class) //
		).build(), engineDescriptor);

		assertEquals(4, engineDescriptor.allDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(MyTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test1()")));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test2()")));
		assertTrue(uniqueIds.contains(uniqueIdForTestFactoryMethod(MyTestClass.class, "dynamicTest()")));
	}

	@Test
	public void twoClassesResolution() {
		ClassSelector selector1 = ClassSelector.forClass(MyTestClass.class);
		ClassSelector selector2 = ClassSelector.forClass(YourTestClass.class);

		resolver.resolveSelectors(request().select(selector1, selector2).build(), engineDescriptor);

		assertEquals(7, engineDescriptor.allDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(MyTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test1()")));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test2()")));
		assertTrue(uniqueIds.contains(uniqueIdForTestFactoryMethod(MyTestClass.class, "dynamicTest()")));
		assertTrue(uniqueIds.contains(uniqueIdForClass(YourTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(YourTestClass.class, "test3()")));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(YourTestClass.class, "test4()")));
	}

	@Test
	public void classResolutionOfStaticNestedClass() {
		ClassSelector selector = ClassSelector.forClass(OtherTestClass.NestedTestClass.class);

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(OtherTestClass.NestedTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test5()")));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test6()")));
	}

	@Test
	public void methodResolution() throws NoSuchMethodException {
		Method test1 = MyTestClass.class.getDeclaredMethod("test1");
		MethodSelector selector = MethodSelector.forMethod(test1.getDeclaringClass(), test1);

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(MyTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test1()")));
	}

	@Test
	public void methodResolutionFromInheritedMethod() throws NoSuchMethodException {
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
		Method notATest = MyTestClass.class.getDeclaredMethod("notATest");
		MethodSelector selector = MethodSelector.forMethod(notATest.getDeclaringClass(), notATest);
		EngineDiscoveryRequest request = request().select(selector).build();
		resolver.resolveSelectors(request, engineDescriptor);
		assertTrue(engineDescriptor.allDescendants().isEmpty());
	}

	@Test
	public void classResolutionByUniqueId() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(uniqueIdForClass(MyTestClass.class).toString());

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		assertEquals(4, engineDescriptor.allDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(MyTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test1()")));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test2()")));
		assertTrue(uniqueIds.contains(uniqueIdForTestFactoryMethod(MyTestClass.class, "dynamicTest()")));
	}

	@Test
	public void staticNestedClassResolutionByUniqueId() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForClass(OtherTestClass.NestedTestClass.class).toString());

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(OtherTestClass.NestedTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test5()")));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test6()")));
	}

	@Test
	public void methodOfInnerClassByUniqueId() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test5()").toString());

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(OtherTestClass.NestedTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test5()")));
	}

	@Test
	public void resolvingUniqueIdWithUnknownSegmentTypeResolvesNothing() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(engineId().append("poops", "machine").toString());
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
	public void methodResolutionByUniqueIdWithNullInput() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(uniqueIdForMethod(getClass(), null));
		assertMethodDoesNotMatchPattern(selector);
	}

	@Test
	public void methodResolutionByUniqueIdWithEmptyInput() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(uniqueIdForMethod(getClass(), "  "));
		assertMethodDoesNotMatchPattern(selector);
	}

	@Test
	public void methodResolutionByUniqueIdWithMissingMethodName() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(uniqueIdForMethod(getClass(), "()"));
		assertMethodDoesNotMatchPattern(selector);
	}

	@Test
	public void methodResolutionByUniqueIdWithMissingParameters() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(uniqueIdForMethod(getClass(), "methodName"));
		assertMethodDoesNotMatchPattern(selector);
	}

	@Test
	public void methodResolutionByUniqueIdWithBogusParameters() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForMethod(getClass(), "methodName(java.lang.String, junit.foo.Enigma)"));
		Exception exception = expectThrows(JUnitException.class,
			() -> resolver.resolveSelectors(request().select(selector).build(), engineDescriptor));
		assertThat(exception).hasMessageStartingWith("Failed to load parameter type");
		assertThat(exception).hasMessageContaining("junit.foo.Enigma");
	}

	private void assertMethodDoesNotMatchPattern(UniqueIdSelector selector) {
		Exception exception = expectThrows(PreconditionViolationException.class,
			() -> resolver.resolveSelectors(request().select(selector).build(), engineDescriptor));
		assertThat(exception).hasMessageStartingWith("Method");
		assertThat(exception).hasMessageContaining("does not match pattern");
	}

	@Test
	public void methodResolutionByUniqueId() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForMethod(MyTestClass.class, "test1()").toString());

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(MyTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test1()")));
	}

	@Test
	public void methodResolutionByUniqueIdFromInheritedClass() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForMethod(HerTestClass.class, "test1()").toString());

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();

		assertTrue(uniqueIds.contains(uniqueIdForClass(HerTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(HerTestClass.class, "test1()")));
	}

	@Test
	public void methodResolutionByUniqueIdWithParams() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForMethod(HerTestClass.class, "test7(java.lang.String)").toString());

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(HerTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(HerTestClass.class, "test7(java.lang.String)")));
	}

	@Test
	public void resolvingUniqueIdWithWrongParamsResolvesNothing() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForMethod(HerTestClass.class, "test7(java.math.BigDecimal)").toString());
		EngineDiscoveryRequest request = request().select(selector).build();

		resolver.resolveSelectors(request, engineDescriptor);
		assertTrue(engineDescriptor.allDescendants().isEmpty());
	}

	@Test
	public void twoMethodResolutionsByUniqueId() {
		UniqueIdSelector selector1 = UniqueIdSelector.forUniqueId(
			uniqueIdForMethod(MyTestClass.class, "test1()").toString());
		UniqueIdSelector selector2 = UniqueIdSelector.forUniqueId(
			uniqueIdForMethod(MyTestClass.class, "test2()").toString());

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

	@Test
	public void resolvingDynamicTestByUniqueIdResolvesOnlyUpToParentTestFactory() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForTestFactoryMethod(MyTestClass.class, "dynamicTest()").append(
				TestFactoryTestDescriptor.DYNAMIC_TEST_SEGMENT_TYPE, "%1"));

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		assertThat(engineDescriptor.allDescendants()).hasSize(2);

		assertThat(uniqueIds()).containsSequence(uniqueIdForClass(MyTestClass.class),
			uniqueIdForTestFactoryMethod(MyTestClass.class, "dynamicTest()"));
	}

	@Test
	public void resolvingTestFactoryMethodByUniqueId() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForTestFactoryMethod(MyTestClass.class, "dynamicTest()"));

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		assertThat(engineDescriptor.allDescendants()).hasSize(2);

		assertThat(uniqueIds()).containsSequence(uniqueIdForClass(MyTestClass.class),
			uniqueIdForTestFactoryMethod(MyTestClass.class, "dynamicTest()"));
	}

	@Test
	public void packageResolution() {
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
	public void classpathResolution() {
		File classpath = new File(
			DiscoverySelectorResolverTests.class.getProtectionDomain().getCodeSource().getLocation().getPath());

		List<DiscoverySelector> selector = ClasspathSelector.forPath(classpath.getAbsolutePath());

		resolver.resolveSelectors(request().select(selector).build(), engineDescriptor);

		assertTrue(engineDescriptor.allDescendants().size() > 500, "Too few test descriptors in classpath");

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
	public void nestedTestResolutionFromBaseClass() {
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
	public void nestedTestResolutionFromNestedTestClass() {
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
	public void nestedTestResolutionFromUniqueId() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForClass(TestCaseWithNesting.NestedTest.DoubleNestedTest.class).toString());

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
	public void doubleNestedTestResolutionFromClass() {
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
	public void methodResolutionInDoubleNestedTestClass() throws NoSuchMethodException {
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
	public void nestedTestResolutionFromUniqueIdToMethod() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			uniqueIdForMethod(TestCaseWithNesting.NestedTest.class, "testB()").toString());

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

	@TestFactory
	Stream<DynamicTest> dynamicTest() {
		return new ArrayList<DynamicTest>().stream();
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
