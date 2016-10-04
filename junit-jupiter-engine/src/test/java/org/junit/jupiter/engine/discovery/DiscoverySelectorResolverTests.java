/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.discovery;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.engine.discovery.JupiterUniqueIdBuilder.engineId;
import static org.junit.jupiter.engine.discovery.JupiterUniqueIdBuilder.uniqueIdForClass;
import static org.junit.jupiter.engine.discovery.JupiterUniqueIdBuilder.uniqueIdForMethod;
import static org.junit.jupiter.engine.discovery.JupiterUniqueIdBuilder.uniqueIdForTestFactoryMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathRoots;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectJavaClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectJavaMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectJavaPackage;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.lang.reflect.Method;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.jupiter.engine.descriptor.TestFactoryTestDescriptor;
import org.junit.jupiter.engine.descriptor.subpackage.Class1WithTestCases;
import org.junit.jupiter.engine.descriptor.subpackage.Class2WithTestCases;
import org.junit.jupiter.engine.descriptor.subpackage.ClassWithStaticInnerTestCases;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.JavaClassSelector;
import org.junit.platform.engine.discovery.JavaMethodSelector;
import org.junit.platform.engine.discovery.JavaPackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;

/**
 * @since 5.0
 */
public class DiscoverySelectorResolverTests {

	private final JupiterEngineDescriptor engineDescriptor = new JupiterEngineDescriptor(engineId());
	private final DiscoverySelectorResolver resolver = new DiscoverySelectorResolver();

	@Test
	public void singleClassResolution() {
		JavaClassSelector selector = selectJavaClass(MyTestClass.class);

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertEquals(4, engineDescriptor.getAllDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(MyTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test1()")));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test2()")));
		assertTrue(uniqueIds.contains(uniqueIdForTestFactoryMethod(MyTestClass.class, "dynamicTest()")));
	}

	@Test
	public void duplicateClassSelectorOnlyResolvesOnce() {
		resolver.resolveSelectors(request().selectors( //
			selectJavaClass(MyTestClass.class), //
			selectJavaClass(MyTestClass.class) //
		).build(), engineDescriptor);

		assertEquals(4, engineDescriptor.getAllDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(MyTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test1()")));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test2()")));
		assertTrue(uniqueIds.contains(uniqueIdForTestFactoryMethod(MyTestClass.class, "dynamicTest()")));
	}

	@Test
	public void twoClassesResolution() {
		JavaClassSelector selector1 = selectJavaClass(MyTestClass.class);
		JavaClassSelector selector2 = selectJavaClass(YourTestClass.class);

		resolver.resolveSelectors(request().selectors(selector1, selector2).build(), engineDescriptor);

		assertEquals(7, engineDescriptor.getAllDescendants().size());
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
		JavaClassSelector selector = selectJavaClass(OtherTestClass.NestedTestClass.class);

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertEquals(3, engineDescriptor.getAllDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(OtherTestClass.NestedTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test5()")));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test6()")));
	}

	@Test
	public void methodResolution() throws NoSuchMethodException {
		Method test1 = MyTestClass.class.getDeclaredMethod("test1");
		JavaMethodSelector selector = selectJavaMethod(test1.getDeclaringClass(), test1);

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertEquals(2, engineDescriptor.getAllDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(MyTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test1()")));
	}

	@Test
	public void methodResolutionFromInheritedMethod() throws NoSuchMethodException {
		JavaMethodSelector selector = selectJavaMethod(HerTestClass.class,
			MyTestClass.class.getDeclaredMethod("test1"));

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertEquals(2, engineDescriptor.getAllDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(HerTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(HerTestClass.class, "test1()")));
	}

	@Test
	public void resolvingSelectorOfNonTestMethodResolvesNothing() throws NoSuchMethodException {
		Method notATest = MyTestClass.class.getDeclaredMethod("notATest");
		JavaMethodSelector selector = selectJavaMethod(notATest.getDeclaringClass(), notATest);
		EngineDiscoveryRequest request = request().selectors(selector).build();
		resolver.resolveSelectors(request, engineDescriptor);
		assertTrue(engineDescriptor.getAllDescendants().isEmpty());
	}

	@Test
	public void classResolutionByUniqueId() {
		UniqueIdSelector selector = selectUniqueId(uniqueIdForClass(MyTestClass.class).toString());

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertEquals(4, engineDescriptor.getAllDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(MyTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test1()")));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test2()")));
		assertTrue(uniqueIds.contains(uniqueIdForTestFactoryMethod(MyTestClass.class, "dynamicTest()")));
	}

	@Test
	public void staticNestedClassResolutionByUniqueId() {
		UniqueIdSelector selector = selectUniqueId(uniqueIdForClass(OtherTestClass.NestedTestClass.class).toString());

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertEquals(3, engineDescriptor.getAllDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(OtherTestClass.NestedTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test5()")));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test6()")));
	}

	@Test
	public void methodOfInnerClassByUniqueId() {
		UniqueIdSelector selector = selectUniqueId(
			uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test5()").toString());

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertEquals(2, engineDescriptor.getAllDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(OtherTestClass.NestedTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test5()")));
	}

	@Test
	public void resolvingUniqueIdWithUnknownSegmentTypeResolvesNothing() {
		UniqueIdSelector selector = selectUniqueId(engineId().append("bogus", "enigma").toString());
		EngineDiscoveryRequest request = request().selectors(selector).build();

		resolver.resolveSelectors(request, engineDescriptor);
		assertTrue(engineDescriptor.getAllDescendants().isEmpty());
	}

	@Test
	public void resolvingUniqueIdOfNonTestMethodResolvesNothing() {
		UniqueIdSelector selector = selectUniqueId(uniqueIdForMethod(MyTestClass.class, "notATest()"));
		EngineDiscoveryRequest request = request().selectors(selector).build();

		resolver.resolveSelectors(request, engineDescriptor);
		assertTrue(engineDescriptor.getAllDescendants().isEmpty());
	}

	@Test
	public void methodResolutionByUniqueIdWithMissingMethodName() {
		UniqueIdSelector selector = selectUniqueId(uniqueIdForMethod(getClass(), "()"));
		assertMethodDoesNotMatchPattern(selector);
	}

	@Test
	public void methodResolutionByUniqueIdWithMissingParameters() {
		UniqueIdSelector selector = selectUniqueId(uniqueIdForMethod(getClass(), "methodName"));
		assertMethodDoesNotMatchPattern(selector);
	}

	@Test
	public void methodResolutionByUniqueIdWithBogusParameters() {
		UniqueIdSelector selector = selectUniqueId(
			uniqueIdForMethod(getClass(), "methodName(java.lang.String, junit.foo.Enigma)"));
		Exception exception = assertThrows(JUnitException.class,
			() -> resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor));
		assertThat(exception).hasMessageStartingWith("Failed to load parameter type");
		assertThat(exception).hasMessageContaining("junit.foo.Enigma");
	}

	private void assertMethodDoesNotMatchPattern(UniqueIdSelector selector) {
		Exception exception = assertThrows(PreconditionViolationException.class,
			() -> resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor));
		assertThat(exception).hasMessageStartingWith("Method");
		assertThat(exception).hasMessageContaining("does not match pattern");
	}

	@Test
	public void methodResolutionByUniqueId() {
		UniqueIdSelector selector = selectUniqueId(uniqueIdForMethod(MyTestClass.class, "test1()").toString());

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertEquals(2, engineDescriptor.getAllDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(MyTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(MyTestClass.class, "test1()")));
	}

	@Test
	public void methodResolutionByUniqueIdFromInheritedClass() {
		UniqueIdSelector selector = selectUniqueId(uniqueIdForMethod(HerTestClass.class, "test1()").toString());

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertEquals(2, engineDescriptor.getAllDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();

		assertTrue(uniqueIds.contains(uniqueIdForClass(HerTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(HerTestClass.class, "test1()")));
	}

	@Test
	public void methodResolutionByUniqueIdWithParams() {
		UniqueIdSelector selector = selectUniqueId(
			uniqueIdForMethod(HerTestClass.class, "test7(java.lang.String)").toString());

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertEquals(2, engineDescriptor.getAllDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(HerTestClass.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(HerTestClass.class, "test7(java.lang.String)")));
	}

	@Test
	public void resolvingUniqueIdWithWrongParamsResolvesNothing() {
		UniqueIdSelector selector = selectUniqueId(
			uniqueIdForMethod(HerTestClass.class, "test7(java.math.BigDecimal)").toString());
		EngineDiscoveryRequest request = request().selectors(selector).build();

		resolver.resolveSelectors(request, engineDescriptor);
		assertTrue(engineDescriptor.getAllDescendants().isEmpty());
	}

	@Test
	public void twoMethodResolutionsByUniqueId() {
		UniqueIdSelector selector1 = selectUniqueId(uniqueIdForMethod(MyTestClass.class, "test1()").toString());
		UniqueIdSelector selector2 = selectUniqueId(uniqueIdForMethod(MyTestClass.class, "test2()").toString());

		// adding same selector twice should have no effect
		resolver.resolveSelectors(request().selectors(selector1, selector2, selector2).build(), engineDescriptor);

		assertEquals(3, engineDescriptor.getAllDescendants().size());
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
		UniqueIdSelector selector = selectUniqueId(
			uniqueIdForTestFactoryMethod(MyTestClass.class, "dynamicTest()").append(
				TestFactoryTestDescriptor.DYNAMIC_TEST_SEGMENT_TYPE, "%1"));

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertThat(engineDescriptor.getAllDescendants()).hasSize(2);

		assertThat(uniqueIds()).containsSequence(uniqueIdForClass(MyTestClass.class),
			uniqueIdForTestFactoryMethod(MyTestClass.class, "dynamicTest()"));
	}

	@Test
	public void resolvingTestFactoryMethodByUniqueId() {
		UniqueIdSelector selector = selectUniqueId(uniqueIdForTestFactoryMethod(MyTestClass.class, "dynamicTest()"));

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertThat(engineDescriptor.getAllDescendants()).hasSize(2);

		assertThat(uniqueIds()).containsSequence(uniqueIdForClass(MyTestClass.class),
			uniqueIdForTestFactoryMethod(MyTestClass.class, "dynamicTest()"));
	}

	@Test
	public void packageResolutionUsingExplicitBasePackage() {
		JavaPackageSelector selector = selectJavaPackage("org.junit.jupiter.engine.descriptor.subpackage");

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertEquals(6, engineDescriptor.getAllDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(Class1WithTestCases.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(Class1WithTestCases.class, "test1()")));
		assertTrue(uniqueIds.contains(uniqueIdForClass(Class2WithTestCases.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(Class2WithTestCases.class, "test2()")));
		assertTrue(
			uniqueIds.contains(uniqueIdForMethod(ClassWithStaticInnerTestCases.ShouldBeDiscovered.class, "test1()")));
	}

	@Test
	public void packageResolutionUsingDefaultPackage() {
		resolver.resolveSelectors(request().selectors(selectJavaPackage("")).build(), engineDescriptor);

		// 150 is completely arbitrary. The actual number is likely much higher.
		assertThat(engineDescriptor.getAllDescendants().size()).isGreaterThan(150).as(
			"Too few test descriptors in classpath");

		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(ReflectionUtils.loadClass("DefaultPackageTestCase").get())),
			"Failed to pick up DefaultPackageTestCase via classpath scanning");
		assertTrue(uniqueIds.contains(uniqueIdForClass(Class1WithTestCases.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(Class1WithTestCases.class, "test1()")));
		assertTrue(uniqueIds.contains(uniqueIdForClass(Class2WithTestCases.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(Class2WithTestCases.class, "test2()")));
	}

	@Test
	public void classpathResolution() throws Exception {
		Path classpath = Paths.get(
			DiscoverySelectorResolverTests.class.getProtectionDomain().getCodeSource().getLocation().toURI());

		List<ClasspathRootSelector> selectors = selectClasspathRoots(singleton(classpath));

		resolver.resolveSelectors(request().selectors(selectors).build(), engineDescriptor);

		// 150 is completely arbitrary. The actual number is likely much higher.
		assertThat(engineDescriptor.getAllDescendants().size()).isGreaterThan(150).as(
			"Too few test descriptors in classpath");

		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds().contains(uniqueIdForClass(ReflectionUtils.loadClass("DefaultPackageTestCase").get())),
			"Failed to pick up DefaultPackageTestCase via classpath scanning");
		assertTrue(uniqueIds.contains(uniqueIdForClass(Class1WithTestCases.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(Class1WithTestCases.class, "test1()")));
		assertTrue(uniqueIds.contains(uniqueIdForClass(Class2WithTestCases.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(Class2WithTestCases.class, "test2()")));
		assertTrue(
			uniqueIds.contains(uniqueIdForMethod(ClassWithStaticInnerTestCases.ShouldBeDiscovered.class, "test1()")));
	}

	@Test
	public void nestedTestResolutionFromBaseClass() {
		JavaClassSelector selector = selectJavaClass(TestCaseWithNesting.class);

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		List<UniqueId> uniqueIds = uniqueIds();
		assertEquals(6, uniqueIds.size());

		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(TestCaseWithNesting.class, "testA()")));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTestCase.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(TestCaseWithNesting.NestedTestCase.class, "testB()")));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class)));
		assertTrue(uniqueIds.contains(
			uniqueIdForMethod(TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class, "testC()")));
	}

	@Test
	public void nestedTestResolutionFromNestedTestClass() {
		JavaClassSelector selector = selectJavaClass(TestCaseWithNesting.NestedTestCase.class);

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		List<UniqueId> uniqueIds = uniqueIds();
		assertEquals(5, uniqueIds.size());

		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.class)));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTestCase.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(TestCaseWithNesting.NestedTestCase.class, "testB()")));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class)));
		assertTrue(uniqueIds.contains(
			uniqueIdForMethod(TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class, "testC()")));
	}

	@Test
	public void nestedTestResolutionFromUniqueId() {
		UniqueIdSelector selector = selectUniqueId(
			uniqueIdForClass(TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class).toString());

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		List<UniqueId> uniqueIds = uniqueIds();
		assertEquals(4, uniqueIds.size());

		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.class)));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTestCase.class)));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class)));
		assertTrue(uniqueIds.contains(
			uniqueIdForMethod(TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class, "testC()")));
	}

	@Test
	public void doubleNestedTestResolutionFromClass() {
		JavaClassSelector selector = selectJavaClass(TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class);

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		List<UniqueId> uniqueIds = uniqueIds();
		assertEquals(4, uniqueIds.size());

		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.class)));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTestCase.class)));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class)));
		assertTrue(uniqueIds.contains(
			uniqueIdForMethod(TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class, "testC()")));
	}

	@Test
	public void methodResolutionInDoubleNestedTestClass() throws NoSuchMethodException {
		JavaMethodSelector selector = selectJavaMethod(TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class,
			TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class.getDeclaredMethod("testC"));

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertEquals(4, engineDescriptor.getAllDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.class)));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTestCase.class)));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class)));
		assertTrue(uniqueIds.contains(
			uniqueIdForMethod(TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class, "testC()")));
	}

	@Test
	public void nestedTestResolutionFromUniqueIdToMethod() {
		UniqueIdSelector selector = selectUniqueId(
			uniqueIdForMethod(TestCaseWithNesting.NestedTestCase.class, "testB()").toString());

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		List<UniqueId> uniqueIds = uniqueIds();
		assertEquals(3, uniqueIds.size());
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.class)));
		assertTrue(uniqueIds.contains(uniqueIdForClass(TestCaseWithNesting.NestedTestCase.class)));
		assertTrue(uniqueIds.contains(uniqueIdForMethod(TestCaseWithNesting.NestedTestCase.class, "testB()")));
	}

	private TestDescriptor descriptorByUniqueId(UniqueId uniqueId) {
		return engineDescriptor.getAllDescendants().stream().filter(
			d -> d.getUniqueId().equals(uniqueId)).findFirst().get();
	}

	private List<UniqueId> uniqueIds() {
		return engineDescriptor.getAllDescendants().stream().map(TestDescriptor::getUniqueId).collect(
			Collectors.toList());
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
	class NestedTestCase {

		@Test
		void testB() {
		}

		@Nested
		class DoubleNestedTestCase {

			@Test
			void testC() {
			}
		}
	}
}
