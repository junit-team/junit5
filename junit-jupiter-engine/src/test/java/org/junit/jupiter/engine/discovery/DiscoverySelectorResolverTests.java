/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery;

import static java.util.Collections.singleton;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.engine.descriptor.TestFactoryTestDescriptor.DYNAMIC_CONTAINER_SEGMENT_TYPE;
import static org.junit.jupiter.engine.descriptor.TestFactoryTestDescriptor.DYNAMIC_TEST_SEGMENT_TYPE;
import static org.junit.jupiter.engine.discovery.JupiterUniqueIdBuilder.engineId;
import static org.junit.jupiter.engine.discovery.JupiterUniqueIdBuilder.uniqueIdForClass;
import static org.junit.jupiter.engine.discovery.JupiterUniqueIdBuilder.uniqueIdForMethod;
import static org.junit.jupiter.engine.discovery.JupiterUniqueIdBuilder.uniqueIdForTestFactoryMethod;
import static org.junit.jupiter.engine.discovery.JupiterUniqueIdBuilder.uniqueIdForTestTemplateMethod;
import static org.junit.jupiter.engine.discovery.JupiterUniqueIdBuilder.uniqueIdForTopLevelClass;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathRoots;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.engine.TrackLogRecords;
import org.junit.jupiter.engine.descriptor.DynamicDescendantFilter;
import org.junit.jupiter.engine.descriptor.Filterable;
import org.junit.jupiter.engine.descriptor.JupiterEngineDescriptor;
import org.junit.jupiter.engine.descriptor.JupiterTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestTemplateInvocationTestDescriptor;
import org.junit.jupiter.engine.descriptor.subpackage.Class1WithTestCases;
import org.junit.jupiter.engine.descriptor.subpackage.Class2WithTestCases;
import org.junit.jupiter.engine.descriptor.subpackage.ClassWithStaticInnerTestCases;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.logging.LogRecordListener;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.ClasspathRootSelector;
import org.junit.platform.engine.discovery.MethodSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * @since 5.0
 */
class DiscoverySelectorResolverTests {

	private final JupiterEngineDescriptor engineDescriptor = new JupiterEngineDescriptor(engineId());
	private final DiscoverySelectorResolver resolver = new DiscoverySelectorResolver();

	@Test
	void nonTestClassResolution() {
		resolver.resolveSelectors(request().selectors(selectClass(NonTestClass.class)).build(), engineDescriptor);

		assertTrue(engineDescriptor.getDescendants().isEmpty());
	}

	@Test
	@TrackLogRecords
	void abstractClassResolution(LogRecordListener listener) {
		resolver.resolveSelectors(request().selectors(selectClass(AbstractTestClass.class)).build(), engineDescriptor);

		assertTrue(engineDescriptor.getDescendants().isEmpty());
		assertThat(firstDebugLogRecord(listener).getMessage())//
				.isEqualTo("Class 'org.junit.jupiter.engine.discovery.AbstractTestClass' could not be resolved.");
	}

	@Test
	void singleClassResolution() {
		ClassSelector selector = selectClass(MyTestClass.class);

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertEquals(4, engineDescriptor.getDescendants().size());
		assertUniqueIdsForMyTestClass(uniqueIds());
	}

	@Test
	@TrackLogRecords
	void classResolutionForNonexistentClass(LogRecordListener listener) {
		ClassSelector selector = selectClass("org.example.DoesNotExist");

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertTrue(engineDescriptor.getDescendants().isEmpty());
		assertThat(firstDebugLogRecord(listener).getMessage())//
				.isEqualTo("Class 'org.example.DoesNotExist' could not be resolved.");
	}

	@Test
	void duplicateClassSelectorOnlyResolvesOnce() {
		resolver.resolveSelectors(request().selectors( //
			selectClass(MyTestClass.class), //
			selectClass(MyTestClass.class) //
		).build(), engineDescriptor);

		assertEquals(4, engineDescriptor.getDescendants().size());
		assertUniqueIdsForMyTestClass(uniqueIds());
	}

	@Test
	void twoClassesResolution() {
		ClassSelector selector1 = selectClass(MyTestClass.class);
		ClassSelector selector2 = selectClass(YourTestClass.class);

		resolver.resolveSelectors(request().selectors(selector1, selector2).build(), engineDescriptor);

		assertEquals(7, engineDescriptor.getDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertUniqueIdsForMyTestClass(uniqueIds);
		assertThat(uniqueIds).contains(uniqueIdForClass(YourTestClass.class));
		assertThat(uniqueIds).contains(uniqueIdForMethod(YourTestClass.class, "test3()"));
		assertThat(uniqueIds).contains(uniqueIdForMethod(YourTestClass.class, "test4()"));
	}

	private void assertUniqueIdsForMyTestClass(List<UniqueId> uniqueIds) {
		assertThat(uniqueIds).contains(uniqueIdForClass(MyTestClass.class));
		assertThat(uniqueIds).contains(uniqueIdForMethod(MyTestClass.class, "test1()"));
		assertThat(uniqueIds).contains(uniqueIdForMethod(MyTestClass.class, "test2()"));
		assertThat(uniqueIds).contains(uniqueIdForTestFactoryMethod(MyTestClass.class, "dynamicTest()"));
	}

	@Test
	void classResolutionOfStaticNestedClass() {
		ClassSelector selector = selectClass(OtherTestClass.NestedTestClass.class);

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertEquals(3, engineDescriptor.getDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertThat(uniqueIds).contains(uniqueIdForClass(OtherTestClass.NestedTestClass.class));
		assertThat(uniqueIds).contains(uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test5()"));
		assertThat(uniqueIds).contains(uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test6()"));
	}

	@Test
	void methodResolution() throws NoSuchMethodException {
		Method test1 = MyTestClass.class.getDeclaredMethod("test1");
		MethodSelector selector = selectMethod(test1.getDeclaringClass(), test1);

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertEquals(2, engineDescriptor.getDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertThat(uniqueIds).contains(uniqueIdForClass(MyTestClass.class));
		assertThat(uniqueIds).contains(uniqueIdForMethod(MyTestClass.class, "test1()"));
	}

	@Test
	void methodResolutionFromInheritedMethod() throws NoSuchMethodException {
		MethodSelector selector = selectMethod(HerTestClass.class, MyTestClass.class.getDeclaredMethod("test1"));

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertEquals(2, engineDescriptor.getDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertThat(uniqueIds).contains(uniqueIdForClass(HerTestClass.class));
		assertThat(uniqueIds).contains(uniqueIdForMethod(HerTestClass.class, "test1()"));
	}

	@Test
	void resolvingSelectorOfNonTestMethodResolvesNothing() throws NoSuchMethodException {
		Method notATest = MyTestClass.class.getDeclaredMethod("notATest");
		MethodSelector selector = selectMethod(notATest.getDeclaringClass(), notATest);
		EngineDiscoveryRequest request = request().selectors(selector).build();

		resolver.resolveSelectors(request, engineDescriptor);

		assertTrue(engineDescriptor.getDescendants().isEmpty());
	}

	@Test
	@TrackLogRecords
	void methodResolutionForNonexistentClass(LogRecordListener listener) {
		String className = "org.example.DoesNotExist";
		String methodName = "bogus";
		MethodSelector selector = selectMethod(className, methodName, "");

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertTrue(engineDescriptor.getDescendants().isEmpty());
		LogRecord logRecord = firstDebugLogRecord(listener);
		assertThat(logRecord.getMessage())//
				.isEqualTo("Method '" + methodName + "' in class '" + className + "' could not be resolved.");
		assertThat(logRecord.getThrown())//
				.isInstanceOf(PreconditionViolationException.class)//
				.hasMessageStartingWith("Could not load class with name: " + className);
	}

	@Test
	@TrackLogRecords
	void methodResolutionForNonexistentMethod(LogRecordListener listener) {
		MethodSelector selector = selectMethod(MyTestClass.class, "bogus", "");

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertTrue(engineDescriptor.getDescendants().isEmpty());
		assertThat(firstDebugLogRecord(listener).getMessage())//
				.isEqualTo("Method 'bogus' in class '" + MyTestClass.class.getName() + "' could not be resolved.");
	}

	@Test
	void classResolutionByUniqueId() {
		UniqueIdSelector selector = selectUniqueId(uniqueIdForClass(MyTestClass.class).toString());

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertEquals(4, engineDescriptor.getDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertUniqueIdsForMyTestClass(uniqueIds);
	}

	@Test
	void staticNestedClassResolutionByUniqueId() {
		UniqueIdSelector selector = selectUniqueId(uniqueIdForClass(OtherTestClass.NestedTestClass.class).toString());

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertEquals(3, engineDescriptor.getDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertThat(uniqueIds).contains(uniqueIdForClass(OtherTestClass.NestedTestClass.class));
		assertThat(uniqueIds).contains(uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test5()"));
		assertThat(uniqueIds).contains(uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test6()"));
	}

	@Test
	void methodOfInnerClassByUniqueId() {
		UniqueIdSelector selector = selectUniqueId(
			uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test5()").toString());

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertEquals(2, engineDescriptor.getDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertThat(uniqueIds).contains(uniqueIdForClass(OtherTestClass.NestedTestClass.class));
		assertThat(uniqueIds).contains(uniqueIdForMethod(OtherTestClass.NestedTestClass.class, "test5()"));
	}

	@Test
	@TrackLogRecords
	void resolvingUniqueIdWithUnknownSegmentTypeResolvesNothing(LogRecordListener listener) {
		UniqueId uniqueId = engineId().append("bogus", "enigma");
		UniqueIdSelector selector = selectUniqueId(uniqueId);
		EngineDiscoveryRequest request = request().selectors(selector).build();

		resolver.resolveSelectors(request, engineDescriptor);
		assertTrue(engineDescriptor.getDescendants().isEmpty());
		assertThat(firstWarningLogRecord(listener).getMessage()) //
				.isEqualTo("Unique ID '" + uniqueId + "' could not be resolved.");
	}

	@Test
	void resolvingUniqueIdOfNonTestMethodResolvesNothing() {
		UniqueIdSelector selector = selectUniqueId(uniqueIdForMethod(MyTestClass.class, "notATest()"));
		EngineDiscoveryRequest request = request().selectors(selector).build();

		resolver.resolveSelectors(request, engineDescriptor);
		assertTrue(engineDescriptor.getDescendants().isEmpty());
	}

	@Test
	@TrackLogRecords
	void methodResolutionByUniqueIdWithMissingMethodName(LogRecordListener listener) {
		UniqueId uniqueId = uniqueIdForMethod(getClass(), "()");

		resolver.resolveSelectors(request().selectors(selectUniqueId(uniqueId)).build(), engineDescriptor);

		assertTrue(engineDescriptor.getDescendants().isEmpty());
		LogRecord logRecord = firstWarningLogRecord(listener);
		assertThat(logRecord.getMessage()).isEqualTo("Unique ID '" + uniqueId + "' could not be resolved.");
		assertThat(logRecord.getThrown())//
				.isInstanceOf(PreconditionViolationException.class)//
				.hasMessageStartingWith("Method [()] does not match pattern");
	}

	@Test
	@TrackLogRecords
	void methodResolutionByUniqueIdWithMissingParameters(LogRecordListener listener) {
		UniqueId uniqueId = uniqueIdForMethod(getClass(), "methodName");

		resolver.resolveSelectors(request().selectors(selectUniqueId(uniqueId)).build(), engineDescriptor);

		assertTrue(engineDescriptor.getDescendants().isEmpty());
		LogRecord logRecord = firstWarningLogRecord(listener);
		assertThat(logRecord.getMessage()).isEqualTo("Unique ID '" + uniqueId + "' could not be resolved.");
		assertThat(logRecord.getThrown())//
				.isInstanceOf(PreconditionViolationException.class)//
				.hasMessageStartingWith("Method [methodName] does not match pattern");
	}

	@Test
	@TrackLogRecords
	void methodResolutionByUniqueIdWithBogusParameters(LogRecordListener listener) {
		UniqueId uniqueId = uniqueIdForMethod(getClass(), "methodName(java.lang.String, junit.foo.Enigma)");

		resolver.resolveSelectors(request().selectors(selectUniqueId(uniqueId)).build(), engineDescriptor);

		assertTrue(engineDescriptor.getDescendants().isEmpty());
		LogRecord logRecord = firstWarningLogRecord(listener);
		assertThat(logRecord.getMessage()).isEqualTo("Unique ID '" + uniqueId + "' could not be resolved.");
		assertThat(logRecord.getThrown())//
				.isInstanceOf(JUnitException.class)//
				.hasMessage("Failed to load parameter type [%s] for method [%s] in class [%s].", "junit.foo.Enigma",
					"methodName", getClass().getName());
	}

	@Test
	void methodResolutionByUniqueId() {
		UniqueIdSelector selector = selectUniqueId(uniqueIdForMethod(MyTestClass.class, "test1()").toString());

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertEquals(2, engineDescriptor.getDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertThat(uniqueIds).contains(uniqueIdForClass(MyTestClass.class));
		assertThat(uniqueIds).contains(uniqueIdForMethod(MyTestClass.class, "test1()"));
	}

	@Test
	void methodResolutionByUniqueIdFromInheritedClass() {
		UniqueIdSelector selector = selectUniqueId(uniqueIdForMethod(HerTestClass.class, "test1()").toString());

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertEquals(2, engineDescriptor.getDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();

		assertThat(uniqueIds).contains(uniqueIdForClass(HerTestClass.class));
		assertThat(uniqueIds).contains(uniqueIdForMethod(HerTestClass.class, "test1()"));
	}

	@Test
	@TrackLogRecords
	void methodResolutionByUniqueIdWithParams(LogRecordListener listener) {
		UniqueIdSelector selector = selectUniqueId(
			uniqueIdForMethod(HerTestClass.class, "test7(java.lang.String)").toString());

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertEquals(2, engineDescriptor.getDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertThat(uniqueIds).contains(uniqueIdForClass(HerTestClass.class));
		assertThat(uniqueIds).contains(uniqueIdForMethod(HerTestClass.class, "test7(java.lang.String)"));

		assertZeroLogRecords(listener);
	}

	@Test
	@TrackLogRecords
	void resolvingUniqueIdWithWrongParamsResolvesNothing(LogRecordListener listener) {
		UniqueId uniqueId = uniqueIdForMethod(HerTestClass.class, "test7(java.math.BigDecimal)");
		EngineDiscoveryRequest request = request().selectors(selectUniqueId(uniqueId)).build();

		resolver.resolveSelectors(request, engineDescriptor);

		assertTrue(engineDescriptor.getDescendants().isEmpty());
		assertThat(firstWarningLogRecord(listener).getMessage())//
				.isEqualTo("Unique ID '" + uniqueId + "' could only be partially resolved. "
						+ "All resolved segments will be executed; however, the following segments "
						+ "could not be resolved: [Segment [type = 'method', value = 'test7(java.math.BigDecimal)']]");
	}

	@Test
	void twoMethodResolutionsByUniqueId() {
		UniqueIdSelector selector1 = selectUniqueId(uniqueIdForMethod(MyTestClass.class, "test1()").toString());
		UniqueIdSelector selector2 = selectUniqueId(uniqueIdForMethod(MyTestClass.class, "test2()").toString());

		// adding same selector twice should have no effect
		resolver.resolveSelectors(request().selectors(selector1, selector2, selector2).build(), engineDescriptor);

		assertEquals(3, engineDescriptor.getDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertThat(uniqueIds).contains(uniqueIdForClass(MyTestClass.class));
		assertThat(uniqueIds).contains(uniqueIdForMethod(MyTestClass.class, "test1()"));
		assertThat(uniqueIds).contains(uniqueIdForMethod(MyTestClass.class, "test2()"));

		TestDescriptor classFromMethod1 = descriptorByUniqueId(
			uniqueIdForMethod(MyTestClass.class, "test1()")).getParent().get();
		TestDescriptor classFromMethod2 = descriptorByUniqueId(
			uniqueIdForMethod(MyTestClass.class, "test2()")).getParent().get();

		assertEquals(classFromMethod1, classFromMethod2);
		assertSame(classFromMethod1, classFromMethod2);
	}

	@Test
	void packageResolutionUsingExplicitBasePackage() {
		PackageSelector selector = selectPackage("org.junit.jupiter.engine.descriptor.subpackage");

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertEquals(6, engineDescriptor.getDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertThat(uniqueIds).contains(uniqueIdForClass(Class1WithTestCases.class));
		assertThat(uniqueIds).contains(uniqueIdForMethod(Class1WithTestCases.class, "test1()"));
		assertThat(uniqueIds).contains(uniqueIdForClass(Class2WithTestCases.class));
		assertThat(uniqueIds).contains(uniqueIdForMethod(Class2WithTestCases.class, "test2()"));
		assertThat(uniqueIds).contains(
			uniqueIdForMethod(ClassWithStaticInnerTestCases.ShouldBeDiscovered.class, "test1()"));
	}

	@Test
	void packageResolutionUsingDefaultPackage() {
		resolver.resolveSelectors(request().selectors(selectPackage("")).build(), engineDescriptor);

		// 150 is completely arbitrary. The actual number is likely much higher.
		assertThat(engineDescriptor.getDescendants().size())//
				.describedAs("Too few test descriptors in classpath")//
				.isGreaterThan(150);

		List<UniqueId> uniqueIds = uniqueIds();
		assertThat(uniqueIds)//
				.describedAs("Failed to pick up DefaultPackageTestCase via classpath scanning")//
				.contains(uniqueIdForClass(ReflectionUtils.loadClass("DefaultPackageTestCase").get()));
		assertThat(uniqueIds).contains(uniqueIdForClass(Class1WithTestCases.class));
		assertThat(uniqueIds).contains(uniqueIdForMethod(Class1WithTestCases.class, "test1()"));
		assertThat(uniqueIds).contains(uniqueIdForClass(Class2WithTestCases.class));
		assertThat(uniqueIds).contains(uniqueIdForMethod(Class2WithTestCases.class, "test2()"));
	}

	@Test
	void classpathResolution() throws Exception {
		Path classpath = Paths.get(
			DiscoverySelectorResolverTests.class.getProtectionDomain().getCodeSource().getLocation().toURI());

		List<ClasspathRootSelector> selectors = selectClasspathRoots(singleton(classpath));

		resolver.resolveSelectors(request().selectors(selectors).build(), engineDescriptor);

		// 150 is completely arbitrary. The actual number is likely much higher.
		assertThat(engineDescriptor.getDescendants().size())//
				.describedAs("Too few test descriptors in classpath")//
				.isGreaterThan(150);

		List<UniqueId> uniqueIds = uniqueIds();
		assertThat(uniqueIds)//
				.describedAs("Failed to pick up DefaultPackageTestCase via classpath scanning")//
				.contains(uniqueIdForClass(ReflectionUtils.loadClass("DefaultPackageTestCase").get()));
		assertThat(uniqueIds).contains(uniqueIdForClass(Class1WithTestCases.class));
		assertThat(uniqueIds).contains(uniqueIdForMethod(Class1WithTestCases.class, "test1()"));
		assertThat(uniqueIds).contains(uniqueIdForClass(Class2WithTestCases.class));
		assertThat(uniqueIds).contains(uniqueIdForMethod(Class2WithTestCases.class, "test2()"));
		assertThat(uniqueIds).contains(
			uniqueIdForMethod(ClassWithStaticInnerTestCases.ShouldBeDiscovered.class, "test1()"));
	}

	@Test
	void classpathResolutionForJarFiles() throws Exception {
		URL jarUrl = getClass().getResource("/jupiter-testjar.jar");
		Path path = Paths.get(jarUrl.toURI());
		List<ClasspathRootSelector> selectors = selectClasspathRoots(singleton(path));

		ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		try (URLClassLoader classLoader = new URLClassLoader(new URL[] { jarUrl })) {
			Thread.currentThread().setContextClassLoader(classLoader);

			resolver.resolveSelectors(request().selectors(selectors).build(), engineDescriptor);

			assertThat(uniqueIds()) //
					.contains(uniqueIdForTopLevelClass("com.example.project.FirstTest")) //
					.contains(uniqueIdForTopLevelClass("com.example.project.SecondTest"));
		}
		finally {
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		}
	}

	@Test
	void nestedTestResolutionFromBaseClass() {
		ClassSelector selector = selectClass(TestCaseWithNesting.class);

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		List<UniqueId> uniqueIds = uniqueIds();
		assertEquals(6, uniqueIds.size());

		assertThat(uniqueIds).contains(uniqueIdForClass(TestCaseWithNesting.class));
		assertThat(uniqueIds).contains(uniqueIdForMethod(TestCaseWithNesting.class, "testA()"));
		assertThat(uniqueIds).contains(uniqueIdForClass(TestCaseWithNesting.NestedTestCase.class));
		assertThat(uniqueIds).contains(uniqueIdForMethod(TestCaseWithNesting.NestedTestCase.class, "testB()"));
		assertThat(uniqueIds).contains(uniqueIdForClass(TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class));
		assertThat(uniqueIds).contains(
			uniqueIdForMethod(TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class, "testC()"));
	}

	@Test
	void nestedTestResolutionFromNestedTestClass() {
		ClassSelector selector = selectClass(TestCaseWithNesting.NestedTestCase.class);

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		List<UniqueId> uniqueIds = uniqueIds();
		assertEquals(5, uniqueIds.size());

		assertThat(uniqueIds).contains(uniqueIdForClass(TestCaseWithNesting.class));
		assertThat(uniqueIds).contains(uniqueIdForClass(TestCaseWithNesting.NestedTestCase.class));
		assertThat(uniqueIds).contains(uniqueIdForMethod(TestCaseWithNesting.NestedTestCase.class, "testB()"));
		assertThat(uniqueIds).contains(uniqueIdForClass(TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class));
		assertThat(uniqueIds).contains(
			uniqueIdForMethod(TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class, "testC()"));
	}

	@Test
	void nestedTestResolutionFromUniqueId() {
		UniqueIdSelector selector = selectUniqueId(
			uniqueIdForClass(TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class).toString());

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		List<UniqueId> uniqueIds = uniqueIds();
		assertEquals(4, uniqueIds.size());

		assertThat(uniqueIds).contains(uniqueIdForClass(TestCaseWithNesting.class));
		assertThat(uniqueIds).contains(uniqueIdForClass(TestCaseWithNesting.NestedTestCase.class));
		assertThat(uniqueIds).contains(uniqueIdForClass(TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class));
		assertThat(uniqueIds).contains(
			uniqueIdForMethod(TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class, "testC()"));
	}

	@Test
	void doubleNestedTestResolutionFromClass() {
		ClassSelector selector = selectClass(TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class);

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		List<UniqueId> uniqueIds = uniqueIds();
		assertEquals(4, uniqueIds.size());

		assertThat(uniqueIds).contains(uniqueIdForClass(TestCaseWithNesting.class));
		assertThat(uniqueIds).contains(uniqueIdForClass(TestCaseWithNesting.NestedTestCase.class));
		assertThat(uniqueIds).contains(uniqueIdForClass(TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class));
		assertThat(uniqueIds).contains(
			uniqueIdForMethod(TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class, "testC()"));
	}

	@Test
	void methodResolutionInDoubleNestedTestClass() throws NoSuchMethodException {
		MethodSelector selector = selectMethod(TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class,
			TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class.getDeclaredMethod("testC"));

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		assertEquals(4, engineDescriptor.getDescendants().size());
		List<UniqueId> uniqueIds = uniqueIds();
		assertThat(uniqueIds).contains(uniqueIdForClass(TestCaseWithNesting.class));
		assertThat(uniqueIds).contains(uniqueIdForClass(TestCaseWithNesting.NestedTestCase.class));
		assertThat(uniqueIds).contains(uniqueIdForClass(TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class));
		assertThat(uniqueIds).contains(
			uniqueIdForMethod(TestCaseWithNesting.NestedTestCase.DoubleNestedTestCase.class, "testC()"));
	}

	@Test
	void nestedTestResolutionFromUniqueIdToMethod() {
		UniqueIdSelector selector = selectUniqueId(
			uniqueIdForMethod(TestCaseWithNesting.NestedTestCase.class, "testB()").toString());

		resolver.resolveSelectors(request().selectors(selector).build(), engineDescriptor);

		List<UniqueId> uniqueIds = uniqueIds();
		assertEquals(3, uniqueIds.size());
		assertThat(uniqueIds).contains(uniqueIdForClass(TestCaseWithNesting.class));
		assertThat(uniqueIds).contains(uniqueIdForClass(TestCaseWithNesting.NestedTestCase.class));
		assertThat(uniqueIds).contains(uniqueIdForMethod(TestCaseWithNesting.NestedTestCase.class, "testB()"));
	}

	@Test
	void testFactoryMethodResolutionByUniqueId() {
		Class<?> clazz = MyTestClass.class;
		UniqueId factoryUid = uniqueIdForTestFactoryMethod(clazz, "dynamicTest()");

		resolver.resolveSelectors(request().selectors(selectUniqueId(factoryUid)).build(), engineDescriptor);

		assertThat(engineDescriptor.getDescendants()).hasSize(2);
		assertThat(uniqueIds()).containsSequence(uniqueIdForClass(clazz), factoryUid);
	}

	@Test
	void testTemplateMethodResolutionByUniqueId() {
		Class<?> clazz = TestClassWithTemplate.class;
		UniqueId templateUid = uniqueIdForTestTemplateMethod(clazz, "testTemplate()");

		resolver.resolveSelectors(request().selectors(selectUniqueId(templateUid)).build(), engineDescriptor);

		assertThat(engineDescriptor.getDescendants()).hasSize(2);
		assertThat(uniqueIds()).containsSequence(uniqueIdForClass(clazz), templateUid);
	}

	@Test
	@TrackLogRecords
	void resolvingDynamicTestByUniqueIdResolvesUpToParentTestFactory(LogRecordListener listener) {
		Class<?> clazz = MyTestClass.class;
		UniqueId factoryUid = uniqueIdForTestFactoryMethod(clazz, "dynamicTest()");
		UniqueId dynamicTestUid = factoryUid.append(DYNAMIC_TEST_SEGMENT_TYPE, "#1");
		UniqueId differentDynamicTestUid = factoryUid.append(DYNAMIC_TEST_SEGMENT_TYPE, "#2");

		resolver.resolveSelectors(request().selectors(selectUniqueId(dynamicTestUid)).build(), engineDescriptor);

		assertThat(engineDescriptor.getDescendants()).hasSize(2);
		assertThat(uniqueIds()).containsSequence(uniqueIdForClass(clazz), factoryUid);
		TestDescriptor testClassDescriptor = getOnlyElement(engineDescriptor.getChildren());

		TestDescriptor testFactoryDescriptor = getOnlyElement(testClassDescriptor.getChildren());
		DynamicDescendantFilter dynamicDescendantFilter = getDynamicDescendantFilter(testFactoryDescriptor);
		assertThat(dynamicDescendantFilter.test(dynamicTestUid)).isTrue();
		assertThat(dynamicDescendantFilter.test(differentDynamicTestUid)).isFalse();

		assertZeroLogRecords(listener);
	}

	@Test
	@TrackLogRecords
	void resolvingDynamicContainerByUniqueIdResolvesUpToParentTestFactory(LogRecordListener listener) {
		Class<?> clazz = MyTestClass.class;
		UniqueId factoryUid = uniqueIdForTestFactoryMethod(clazz, "dynamicTest()");
		UniqueId dynamicContainerUid = factoryUid.append(DYNAMIC_CONTAINER_SEGMENT_TYPE, "#1");
		UniqueId differentDynamicContainerUid = factoryUid.append(DYNAMIC_CONTAINER_SEGMENT_TYPE, "#2");
		UniqueId dynamicTestUid = dynamicContainerUid.append(DYNAMIC_TEST_SEGMENT_TYPE, "#1");
		UniqueId differentDynamicTestUid = dynamicContainerUid.append(DYNAMIC_TEST_SEGMENT_TYPE, "#2");

		resolver.resolveSelectors(request().selectors(selectUniqueId(dynamicTestUid)).build(), engineDescriptor);

		assertThat(engineDescriptor.getDescendants()).hasSize(2);
		assertThat(uniqueIds()).containsSequence(uniqueIdForClass(clazz), factoryUid);
		TestDescriptor testClassDescriptor = getOnlyElement(engineDescriptor.getChildren());

		TestDescriptor testFactoryDescriptor = getOnlyElement(testClassDescriptor.getChildren());
		DynamicDescendantFilter dynamicDescendantFilter = getDynamicDescendantFilter(testFactoryDescriptor);
		assertThat(dynamicDescendantFilter.test(dynamicTestUid)).isTrue();
		assertThat(dynamicDescendantFilter.test(differentDynamicContainerUid)).isFalse();
		assertThat(dynamicDescendantFilter.test(differentDynamicTestUid)).isFalse();

		assertZeroLogRecords(listener);
	}

	@Test
	void resolvingDynamicTestByUniqueIdAndTestFactoryByMethodSelectorResolvesTestFactory() {
		Class<?> clazz = MyTestClass.class;
		UniqueId factoryUid = uniqueIdForTestFactoryMethod(clazz, "dynamicTest()");
		UniqueId dynamicTestUid = factoryUid.append(DYNAMIC_TEST_SEGMENT_TYPE, "#1");

		LauncherDiscoveryRequest request = request() //
				.selectors(selectUniqueId(dynamicTestUid), selectMethod(clazz, "dynamicTest")) //
				.build();

		resolver.resolveSelectors(request, engineDescriptor);

		assertThat(engineDescriptor.getDescendants()).hasSize(2);
		assertThat(uniqueIds()).containsSequence(uniqueIdForClass(clazz), factoryUid);
		TestDescriptor testClassDescriptor = getOnlyElement(engineDescriptor.getChildren());
		TestDescriptor testFactoryDescriptor = getOnlyElement(testClassDescriptor.getChildren());
		DynamicDescendantFilter dynamicDescendantFilter = getDynamicDescendantFilter(testFactoryDescriptor);
		assertThat(dynamicDescendantFilter.test(UniqueId.root("foo", "bar"))).isTrue();
	}

	private DynamicDescendantFilter getDynamicDescendantFilter(TestDescriptor testDescriptor) {
		assertThat(testDescriptor).isInstanceOf(JupiterTestDescriptor.class);
		return ((Filterable) testDescriptor).getDynamicDescendantFilter();
	}

	@Test
	void resolvingTestTemplateInvocationByUniqueIdResolvesOnlyUpToParentTestTemplate() {
		Class<?> clazz = TestClassWithTemplate.class;
		UniqueId templateUid = uniqueIdForTestTemplateMethod(clazz, "testTemplate()");
		UniqueId invocationUid = templateUid.append(TestTemplateInvocationTestDescriptor.SEGMENT_TYPE, "#1");

		resolver.resolveSelectors(request().selectors(selectUniqueId(invocationUid)).build(), engineDescriptor);

		assertThat(engineDescriptor.getDescendants()).hasSize(2);
		assertThat(uniqueIds()).containsSequence(uniqueIdForClass(clazz), templateUid);
	}

	private TestDescriptor descriptorByUniqueId(UniqueId uniqueId) {
		return engineDescriptor.getDescendants().stream().filter(
			d -> d.getUniqueId().equals(uniqueId)).findFirst().get();
	}

	private List<UniqueId> uniqueIds() {
		return engineDescriptor.getDescendants().stream().map(TestDescriptor::getUniqueId).collect(toList());
	}

	private void assertZeroLogRecords(LogRecordListener listener) {
		assertThat(listener.stream(JavaElementsResolver.class)).isEmpty();
	}

	private LogRecord firstWarningLogRecord(LogRecordListener listener) throws AssertionError {
		return listener.stream(JavaElementsResolver.class, Level.WARNING).findFirst().orElseThrow(
			() -> new AssertionError("Failed to find warning log record"));
	}

	private LogRecord firstDebugLogRecord(LogRecordListener listener) throws AssertionError {
		return listener.stream(JavaElementsResolver.class, Level.FINE).findFirst().orElseThrow(
			() -> new AssertionError("Failed to find debug log record"));
	}

}

// -----------------------------------------------------------------------------

class NonTestClass {
}

abstract class AbstractTestClass {

	@Test
	void test() {
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

class TestClassWithTemplate {

	@TestTemplate
	void testTemplate() {
	}
}
