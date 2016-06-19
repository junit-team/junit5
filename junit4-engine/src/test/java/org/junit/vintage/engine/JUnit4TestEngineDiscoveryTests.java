/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.vintage.engine;

import static java.text.MessageFormat.format;
import static java.util.Collections.singleton;
import static java.util.function.Predicate.isEqual;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.platform.commons.util.FunctionUtils.where;
import static org.junit.platform.engine.discovery.ClassFilter.includeClassNamePattern;
import static org.junit.platform.engine.discovery.ClassSelector.selectClass;
import static org.junit.platform.engine.discovery.ClasspathSelector.selectClasspathRoots;
import static org.junit.platform.engine.discovery.MethodSelector.selectMethod;
import static org.junit.platform.engine.discovery.PackageSelector.selectPackage;
import static org.junit.platform.engine.discovery.UniqueIdSelector.selectUniqueId;
import static org.junit.platform.launcher.core.TestDiscoveryRequestBuilder.request;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.descriptor.JavaClassSource;
import org.junit.platform.engine.support.descriptor.JavaMethodSource;
import org.junit.platform.launcher.TestDiscoveryRequest;
import org.junit.runner.manipulation.Filter;
import org.junit.vintage.engine.samples.PlainOldJavaClassWithoutAnyTest;
import org.junit.vintage.engine.samples.junit3.JUnit3SuiteWithSingleTestCaseWithSingleTestWhichFails;
import org.junit.vintage.engine.samples.junit3.PlainJUnit3TestCaseWithSingleTestWhichFails;
import org.junit.vintage.engine.samples.junit4.Categories.Failing;
import org.junit.vintage.engine.samples.junit4.Categories.Plain;
import org.junit.vintage.engine.samples.junit4.Categories.Skipped;
import org.junit.vintage.engine.samples.junit4.Categories.SkippedWithReason;
import org.junit.vintage.engine.samples.junit4.IgnoredJUnit4TestCase;
import org.junit.vintage.engine.samples.junit4.JUnit4SuiteWithPlainJUnit4TestCaseWithSingleTestWhichIsIgnored;
import org.junit.vintage.engine.samples.junit4.JUnit4SuiteWithTwoTestCases;
import org.junit.vintage.engine.samples.junit4.JUnit4TestCaseWithOverloadedMethod;
import org.junit.vintage.engine.samples.junit4.JUnit4TestCaseWithRunnerWithCustomUniqueIds;
import org.junit.vintage.engine.samples.junit4.ParameterizedTestCase;
import org.junit.vintage.engine.samples.junit4.PlainJUnit4TestCaseWithFiveTestMethods;
import org.junit.vintage.engine.samples.junit4.PlainJUnit4TestCaseWithSingleInheritedTestWhichFails;
import org.junit.vintage.engine.samples.junit4.PlainJUnit4TestCaseWithSingleTestWhichFails;
import org.junit.vintage.engine.samples.junit4.PlainJUnit4TestCaseWithSingleTestWhichIsIgnored;
import org.junit.vintage.engine.samples.junit4.PlainJUnit4TestCaseWithTwoTestMethods;
import org.junit.vintage.engine.samples.junit4.SingleFailingTheoryTestCase;
import org.junit.vintage.engine.samples.junit4.TestCaseRunWithJUnitPlatformRunner;

/**
 * @since 5.0
 */
class JUnit4TestEngineDiscoveryTests {

	JUnit4TestEngine engine = new JUnit4TestEngine();

	@Test
	void resolvesSimpleJUnit4TestClass() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithSingleTestWhichFails.class;
		TestDiscoveryRequest discoveryRequest = discoveryRequestForClass(testClass);

		TestDescriptor engineDescriptor = discoverTests(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		TestDescriptor childDescriptor = getOnlyElement(runnerDescriptor.getChildren());
		assertTestMethodDescriptor(childDescriptor, testClass, "failingTest",
			JUnit4UniqueIdBuilder.uniqueIdForClass(testClass));
	}

	@Test
	void resolvesIgnoredJUnit4TestClass() {
		Class<?> testClass = IgnoredJUnit4TestCase.class;
		TestDiscoveryRequest discoveryRequest = discoveryRequestForClass(testClass);

		TestDescriptor engineDescriptor = discoverTests(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertFalse(runnerDescriptor.isContainer());
		assertTrue(runnerDescriptor.isTest());
		assertEquals(testClass.getName(), runnerDescriptor.getDisplayName());
		Assertions.assertEquals(JUnit4UniqueIdBuilder.uniqueIdForClass(testClass), runnerDescriptor.getUniqueId());
		assertThat(runnerDescriptor.getChildren()).isEmpty();
	}

	@Test
	void resolvesJUnit4TestClassWithCustomRunner() throws Exception {
		Class<?> testClass = SingleFailingTheoryTestCase.class;
		TestDiscoveryRequest discoveryRequest = discoveryRequestForClass(testClass);

		TestDescriptor engineDescriptor = discoverTests(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		TestDescriptor childDescriptor = getOnlyElement(runnerDescriptor.getChildren());
		assertTestMethodDescriptor(childDescriptor, testClass, "theory",
			JUnit4UniqueIdBuilder.uniqueIdForClass(testClass));
	}

	@Test
	void resolvesJUnit3TestCase() throws Exception {
		Class<?> testClass = PlainJUnit3TestCaseWithSingleTestWhichFails.class;
		TestDiscoveryRequest discoveryRequest = discoveryRequestForClass(testClass);

		TestDescriptor engineDescriptor = discoverTests(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		TestDescriptor childDescriptor = getOnlyElement(runnerDescriptor.getChildren());
		assertTestMethodDescriptor(childDescriptor, testClass, "test",
			JUnit4UniqueIdBuilder.uniqueIdForClass(testClass));
	}

	@Test
	void resolvesJUnit3SuiteWithSingleTestCaseWithSingleTestWhichFails() throws Exception {
		Class<?> suiteClass = JUnit3SuiteWithSingleTestCaseWithSingleTestWhichFails.class;
		Class<?> testClass = PlainJUnit3TestCaseWithSingleTestWhichFails.class;
		TestDiscoveryRequest discoveryRequest = discoveryRequestForClass(suiteClass);

		TestDescriptor engineDescriptor = discoverTests(discoveryRequest);

		TestDescriptor suiteDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(suiteDescriptor, suiteClass);

		TestDescriptor testClassDescriptor = getOnlyElement(suiteDescriptor.getChildren());
		assertContainerTestDescriptor(testClassDescriptor, suiteClass, testClass);

		TestDescriptor testMethodDescriptor = getOnlyElement(testClassDescriptor.getChildren());
		assertTestMethodDescriptor(testMethodDescriptor, testClass, "test",
			JUnit4UniqueIdBuilder.uniqueIdForClasses(suiteClass, testClass));
	}

	@Test
	void resolvesJUnit4SuiteWithPlainJUnit4TestCaseWithSingleTestWhichIsIgnored() throws Exception {
		Class<?> suiteClass = JUnit4SuiteWithPlainJUnit4TestCaseWithSingleTestWhichIsIgnored.class;
		Class<?> testClass = PlainJUnit4TestCaseWithSingleTestWhichIsIgnored.class;
		TestDiscoveryRequest discoveryRequest = discoveryRequestForClass(suiteClass);

		TestDescriptor engineDescriptor = discoverTests(discoveryRequest);

		TestDescriptor suiteDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(suiteDescriptor, suiteClass);

		TestDescriptor testClassDescriptor = getOnlyElement(suiteDescriptor.getChildren());
		assertContainerTestDescriptor(testClassDescriptor, suiteClass, testClass);

		TestDescriptor testMethodDescriptor = getOnlyElement(testClassDescriptor.getChildren());
		assertTestMethodDescriptor(testMethodDescriptor, testClass, "ignoredTest",
			JUnit4UniqueIdBuilder.uniqueIdForClasses(suiteClass, testClass));
	}

	@Test
	void resolvesJUnit4TestCaseWithOverloadedMethod() {
		Class<?> testClass = JUnit4TestCaseWithOverloadedMethod.class;
		TestDiscoveryRequest discoveryRequest = discoveryRequestForClass(testClass);

		TestDescriptor engineDescriptor = discoverTests(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		List<TestDescriptor> testMethodDescriptors = new ArrayList<>(runnerDescriptor.getChildren());
		assertThat(testMethodDescriptors).hasSize(2);

		TestDescriptor testMethodDescriptor = testMethodDescriptors.get(0);
		assertEquals("theory", testMethodDescriptor.getDisplayName());
		Assertions.assertEquals(JUnit4UniqueIdBuilder.uniqueIdForMethod(testClass, "theory", "0"),
			testMethodDescriptor.getUniqueId());
		assertClassSource(testClass, testMethodDescriptor);

		testMethodDescriptor = testMethodDescriptors.get(1);
		assertEquals("theory", testMethodDescriptor.getDisplayName());
		Assertions.assertEquals(JUnit4UniqueIdBuilder.uniqueIdForMethod(testClass, "theory", "1"),
			testMethodDescriptor.getUniqueId());
		assertClassSource(testClass, testMethodDescriptor);
	}

	@Test
	void doesNotResolvePlainOldJavaClassesWithoutAnyTest() {
		assertYieldsNoDescriptors(PlainOldJavaClassWithoutAnyTest.class);
	}

	@Test
	void doesNotResolveClassRunWithJUnitPlatform() {
		assertYieldsNoDescriptors(TestCaseRunWithJUnitPlatformRunner.class);
	}

	@Test
	void resolvesClasspathSelector() throws Exception {
		File root = getClasspathRoot(PlainJUnit4TestCaseWithSingleTestWhichFails.class);
		TestDiscoveryRequest discoveryRequest = request().selectors(selectClasspathRoots(singleton(root))).build();
		TestDescriptor engineDescriptor = discoverTests(discoveryRequest);

		// @formatter:off
		assertThat(engineDescriptor.getChildren())
			.extracting(TestDescriptor::getDisplayName)
			.contains(PlainJUnit4TestCaseWithSingleTestWhichFails.class.getName())
			.contains(PlainJUnit3TestCaseWithSingleTestWhichFails.class.getName())
			.doesNotContain(PlainOldJavaClassWithoutAnyTest.class.getName());
		// @formatter:on
	}

	@Test
	void resolvesApplyingClassFilters() throws Exception {
		File root = getClasspathRoot(PlainJUnit4TestCaseWithSingleTestWhichFails.class);

		TestDiscoveryRequest discoveryRequest = request().selectors(selectClasspathRoots(singleton(root))).filters(
			includeClassNamePattern(".*JUnit4.*"), includeClassNamePattern(".*Plain.*")).build();

		TestDescriptor engineDescriptor = discoverTests(discoveryRequest);

		// @formatter:off
		assertThat(engineDescriptor.getChildren())
			.extracting(TestDescriptor::getDisplayName)
			.contains(PlainJUnit4TestCaseWithSingleTestWhichFails.class.getName())
			.doesNotContain(JUnit4TestCaseWithOverloadedMethod.class.getName())
			.doesNotContain(PlainJUnit3TestCaseWithSingleTestWhichFails.class.getName());
		// @formatter:on
	}

	@Test
	void resolvesPackageSelectorForJUnit4SamplesPackage() {
		Class<?> testClass = PlainJUnit4TestCaseWithSingleTestWhichFails.class;

		TestDiscoveryRequest discoveryRequest = request().selectors(
			selectPackage(testClass.getPackage().getName())).build();

		TestDescriptor engineDescriptor = discoverTests(discoveryRequest);

		// @formatter:off
		assertThat(engineDescriptor.getChildren())
			.extracting(TestDescriptor::getDisplayName)
			.contains(testClass.getName())
			.doesNotContain(PlainJUnit3TestCaseWithSingleTestWhichFails.class.getName());
		// @formatter:on
	}

	@Test
	void resolvesPackageSelectorForJUnit3SamplesPackage() {
		Class<?> testClass = PlainJUnit3TestCaseWithSingleTestWhichFails.class;

		TestDiscoveryRequest discoveryRequest = request().selectors(
			selectPackage(testClass.getPackage().getName())).build();

		TestDescriptor engineDescriptor = discoverTests(discoveryRequest);

		// @formatter:off
		assertThat(engineDescriptor.getChildren())
			.extracting(TestDescriptor::getDisplayName)
			.contains(testClass.getName())
			.doesNotContain(PlainJUnit4TestCaseWithSingleTestWhichFails.class.getName());
		// @formatter:on
	}

	@Test
	void resolvesClassesWithInheritedMethods() throws Exception {
		Class<?> superclass = PlainJUnit4TestCaseWithSingleTestWhichFails.class;
		Class<?> testClass = PlainJUnit4TestCaseWithSingleInheritedTestWhichFails.class;
		TestDiscoveryRequest discoveryRequest = discoveryRequestForClass(testClass);

		TestDescriptor engineDescriptor = discoverTests(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertEquals(testClass.getName(), runnerDescriptor.getDisplayName());
		assertClassSource(testClass, runnerDescriptor);

		TestDescriptor testDescriptor = getOnlyElement(runnerDescriptor.getChildren());
		assertEquals("failingTest", testDescriptor.getDisplayName());
		assertMethodSource(superclass.getMethod("failingTest"), testDescriptor);
	}

	@Test
	void resolvesCategoriesIntoTags() {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;
		TestDiscoveryRequest discoveryRequest = discoveryRequestForClass(testClass);

		TestDescriptor engineDescriptor = discoverTests(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertThat(runnerDescriptor.getTags()).containsOnly(TestTag.of(Plain.class.getName()));

		TestDescriptor failingTest = findChildByDisplayName(runnerDescriptor, "failingTest");
		assertThat(failingTest.getTags()).containsOnly(//
			TestTag.of(Plain.class.getName()), //
			TestTag.of(Failing.class.getName()));

		TestDescriptor ignoredWithoutReason = findChildByDisplayName(runnerDescriptor, "ignoredTest1_withoutReason");
		assertThat(ignoredWithoutReason.getTags()).containsOnly(//
			TestTag.of(Plain.class.getName()), //
			TestTag.of(Skipped.class.getName()));

		TestDescriptor ignoredWithReason = findChildByDisplayName(runnerDescriptor, "ignoredTest2_withReason");
		assertThat(ignoredWithReason.getTags()).containsOnly(//
			TestTag.of(Plain.class.getName()), //
			TestTag.of(Skipped.class.getName()), //
			TestTag.of(SkippedWithReason.class.getName()));
	}

	@Test
	void resolvesMethodSelectorForSingleMethod() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;
		TestDiscoveryRequest discoveryRequest = request().selectors(
			selectMethod(testClass, testClass.getMethod("failingTest"))).build();

		TestDescriptor engineDescriptor = discoverTests(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		TestDescriptor childDescriptor = getOnlyElement(runnerDescriptor.getChildren());
		assertTestMethodDescriptor(childDescriptor, testClass, "failingTest",
			JUnit4UniqueIdBuilder.uniqueIdForClass(testClass));
	}

	@Test
	void resolvesMethodSelectorForTwoMethodsOfSameClass() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;
		TestDiscoveryRequest discoveryRequest = request().selectors(
			selectMethod(testClass, testClass.getMethod("failingTest")),
			selectMethod(testClass, testClass.getMethod("successfulTest"))).build();

		TestDescriptor engineDescriptor = discoverTests(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		List<TestDescriptor> testMethodDescriptors = new ArrayList<>(runnerDescriptor.getChildren());
		assertThat(testMethodDescriptors).hasSize(2);

		TestDescriptor failingTest = testMethodDescriptors.get(0);
		assertTestMethodDescriptor(failingTest, testClass, "failingTest",
			JUnit4UniqueIdBuilder.uniqueIdForClass(testClass));

		TestDescriptor successfulTest = testMethodDescriptors.get(1);
		assertTestMethodDescriptor(successfulTest, testClass, "successfulTest",
			JUnit4UniqueIdBuilder.uniqueIdForClass(testClass));
	}

	@Test
	void resolvesUniqueIdSelectorForSingleMethod() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;
		TestDiscoveryRequest discoveryRequest = request().selectors(
			UniqueIdSelector.selectUniqueId(JUnit4UniqueIdBuilder.uniqueIdForMethod(testClass, "failingTest"))).build();

		TestDescriptor engineDescriptor = discoverTests(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		TestDescriptor childDescriptor = getOnlyElement(runnerDescriptor.getChildren());
		assertTestMethodDescriptor(childDescriptor, testClass, "failingTest",
			JUnit4UniqueIdBuilder.uniqueIdForClass(testClass));
	}

	@Test
	void resolvesUniqueIdSelectorForSingleClass() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;
		TestDiscoveryRequest discoveryRequest = request().selectors(
			UniqueIdSelector.selectUniqueId(JUnit4UniqueIdBuilder.uniqueIdForClass(testClass))).build();

		TestDescriptor engineDescriptor = discoverTests(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		assertThat(runnerDescriptor.getChildren()).hasSize(5);
	}

	@Test
	void resolvesUniqueIdSelectorOfSingleClassWithinSuite() throws Exception {
		Class<?> suiteClass = JUnit4SuiteWithTwoTestCases.class;
		Class<?> testClass = PlainJUnit4TestCaseWithSingleTestWhichFails.class;
		TestDiscoveryRequest discoveryRequest = request().selectors(
			UniqueIdSelector.selectUniqueId(JUnit4UniqueIdBuilder.uniqueIdForClasses(suiteClass, testClass))).build();

		TestDescriptor engineDescriptor = discoverTests(discoveryRequest);

		TestDescriptor suiteDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(suiteDescriptor, suiteClass);

		TestDescriptor testClassDescriptor = getOnlyElement(suiteDescriptor.getChildren());
		assertContainerTestDescriptor(testClassDescriptor, suiteClass, testClass);

		TestDescriptor testMethodDescriptor = getOnlyElement(testClassDescriptor.getChildren());
		assertTestMethodDescriptor(testMethodDescriptor, testClass, "failingTest",
			JUnit4UniqueIdBuilder.uniqueIdForClasses(suiteClass, testClass));
	}

	@Test
	void resolvesUniqueIdSelectorOfSingleMethodWithinSuite() throws Exception {
		Class<?> suiteClass = JUnit4SuiteWithTwoTestCases.class;
		Class<?> testClass = PlainJUnit4TestCaseWithTwoTestMethods.class;
		TestDiscoveryRequest discoveryRequest = request().selectors(
			UniqueIdSelector.selectUniqueId(JUnit4UniqueIdBuilder.uniqueIdForMethod(
				JUnit4UniqueIdBuilder.uniqueIdForClasses(suiteClass, testClass), testClass, "successfulTest"))).build();

		TestDescriptor engineDescriptor = discoverTests(discoveryRequest);

		TestDescriptor suiteDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(suiteDescriptor, suiteClass);

		TestDescriptor testClassDescriptor = getOnlyElement(suiteDescriptor.getChildren());
		assertContainerTestDescriptor(testClassDescriptor, suiteClass, testClass);

		TestDescriptor testMethodDescriptor = getOnlyElement(testClassDescriptor.getChildren());
		assertTestMethodDescriptor(testMethodDescriptor, testClass, "successfulTest",
			JUnit4UniqueIdBuilder.uniqueIdForClasses(suiteClass, testClass));
	}

	@Test
	void resolvesMultipleUniqueIdSelectorsForMethodsOfSameClass() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithTwoTestMethods.class;
		TestDiscoveryRequest discoveryRequest = request().selectors(
			UniqueIdSelector.selectUniqueId(JUnit4UniqueIdBuilder.uniqueIdForMethod(testClass, "successfulTest")),
			UniqueIdSelector.selectUniqueId(JUnit4UniqueIdBuilder.uniqueIdForMethod(testClass, "failingTest"))).build();

		TestDescriptor engineDescriptor = discoverTests(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		List<TestDescriptor> testMethodDescriptors = new ArrayList<>(runnerDescriptor.getChildren());
		assertThat(testMethodDescriptors).hasSize(2);
		assertTestMethodDescriptor(testMethodDescriptors.get(0), testClass, "failingTest",
			JUnit4UniqueIdBuilder.uniqueIdForClass(testClass));
		assertTestMethodDescriptor(testMethodDescriptors.get(1), testClass, "successfulTest",
			JUnit4UniqueIdBuilder.uniqueIdForClass(testClass));
	}

	@Test
	void doesNotResolveMissingUniqueIdSelectorForSingleClass() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;
		TestDiscoveryRequest discoveryRequest = request().selectors(
			selectUniqueId(JUnit4UniqueIdBuilder.uniqueIdForClass(testClass) + "/[test:doesNotExist]")).build();

		TestDescriptor engineDescriptor = discoverTests(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		TestDescriptor testDescriptor = getOnlyElement(runnerDescriptor.getChildren());
		assertInitializationError(testDescriptor, Filter.class, testClass);
	}

	@Test
	void ignoresMoreFineGrainedSelectorsWhenClassIsSelectedAsWell() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;
		TestDiscoveryRequest discoveryRequest = request().selectors( //
			selectMethod(testClass, testClass.getMethod("failingTest")), //
			UniqueIdSelector.selectUniqueId(JUnit4UniqueIdBuilder.uniqueIdForMethod(testClass, "abortedTest")),
			selectClass(testClass) //
		).build();

		TestDescriptor engineDescriptor = discoverTests(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		assertThat(runnerDescriptor.getChildren()).hasSize(5);
	}

	@Test
	void resolvesCombinationOfMethodAndUniqueIdSelector() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;
		TestDiscoveryRequest discoveryRequest = request().selectors( //
			selectMethod(testClass, testClass.getMethod("failingTest")), //
			UniqueIdSelector.selectUniqueId(JUnit4UniqueIdBuilder.uniqueIdForMethod(testClass, "abortedTest") //
			)).build();

		TestDescriptor engineDescriptor = discoverTests(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		List<TestDescriptor> testMethodDescriptors = new ArrayList<>(runnerDescriptor.getChildren());
		assertThat(testMethodDescriptors).hasSize(2);
		assertTestMethodDescriptor(testMethodDescriptors.get(0), testClass, "abortedTest",
			JUnit4UniqueIdBuilder.uniqueIdForClass(testClass));
		assertTestMethodDescriptor(testMethodDescriptors.get(1), testClass, "failingTest",
			JUnit4UniqueIdBuilder.uniqueIdForClass(testClass));
	}

	@Test
	void ignoresRedundantSelector() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;
		TestDiscoveryRequest discoveryRequest = request().selectors( //
			selectMethod(testClass, testClass.getMethod("failingTest")), //
			UniqueIdSelector.selectUniqueId(JUnit4UniqueIdBuilder.uniqueIdForMethod(testClass, "failingTest") //
			)).build();

		TestDescriptor engineDescriptor = discoverTests(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		TestDescriptor testMethodDescriptor = getOnlyElement(runnerDescriptor.getChildren());
		assertTestMethodDescriptor(testMethodDescriptor, testClass, "failingTest",
			JUnit4UniqueIdBuilder.uniqueIdForClass(testClass));
	}

	@Test
	void doesNotResolveMethodOfClassNotAcceptedByClassFilter() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;
		// @formatter:off
		TestDiscoveryRequest request = request()
				.selectors(selectMethod(testClass, testClass.getMethod("failingTest")))
				.filters(includeClassNamePattern("Foo"))
				.build();
		// @formatter:on

		assertYieldsNoDescriptors(request);
	}

	@Test
	void resolvesClassForMethodSelectorForClassWithNonFilterableRunner() throws Exception {
		Class<?> testClass = IgnoredJUnit4TestCase.class;
		// @formatter:off
		TestDiscoveryRequest request = request()
				.selectors(selectMethod(testClass, testClass.getMethod("test")))
				.build();
		// @formatter:on

		TestDescriptor engineDescriptor = discoverTests(request);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertEquals(testClass.getName(), runnerDescriptor.getDisplayName());
		Assertions.assertEquals(JUnit4UniqueIdBuilder.uniqueIdForClass(testClass), runnerDescriptor.getUniqueId());
		assertThat(runnerDescriptor.getChildren()).isEmpty();
	}

	@Test
	void usesCustomUniqueIdsWhenPresent() throws Exception {
		Class<?> testClass = JUnit4TestCaseWithRunnerWithCustomUniqueIds.class;
		TestDiscoveryRequest request = request().selectors(selectClass(testClass)).build();

		TestDescriptor engineDescriptor = discoverTests(request);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		TestDescriptor childDescriptor = getOnlyElement(runnerDescriptor.getChildren());

		UniqueId prefix = JUnit4UniqueIdBuilder.uniqueIdForClass(testClass);
		assertThat(childDescriptor.getUniqueId().toString()).startsWith(prefix.toString());

		String customUniqueIdValue = childDescriptor.getUniqueId().getSegments().get(2).getType();
		assertNotNull(Base64.getDecoder().decode(customUniqueIdValue.getBytes(StandardCharsets.UTF_8)),
			"is a valid Base64 encoding scheme");
	}

	@Test
	void resolvesTestSourceForParameterizedTests() throws Exception {
		Class<?> testClass = ParameterizedTestCase.class;
		TestDiscoveryRequest request = request().selectors(selectClass(testClass)).build();

		TestDescriptor engineDescriptor = discoverTests(request);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		TestDescriptor fooParentDescriptor = findChildByDisplayName(runnerDescriptor, "[foo]");
		assertTrue(fooParentDescriptor.isContainer());
		assertFalse(fooParentDescriptor.isTest());
		assertThat(fooParentDescriptor.getSource()).isEmpty();

		TestDescriptor testMethodDescriptor = getOnlyElement(fooParentDescriptor.getChildren());
		assertEquals("test[foo]", testMethodDescriptor.getDisplayName());
		assertTrue(testMethodDescriptor.isTest());
		assertFalse(testMethodDescriptor.isContainer());
		assertMethodSource(testClass.getMethod("test"), testMethodDescriptor);
	}

	private TestDescriptor findChildByDisplayName(TestDescriptor runnerDescriptor, String displayName) {
		// @formatter:off
		Set<? extends TestDescriptor> children = runnerDescriptor.getChildren();
		return children
				.stream()
				.filter(where(TestDescriptor::getDisplayName, isEqual(displayName)))
				.findAny()
				.orElseThrow(() ->
					new AssertionError(format("No child with display name \"{0}\" in {1}", displayName, children)));
		// @formatter:on
	}

	private TestDescriptor discoverTests(TestDiscoveryRequest discoveryRequest) {
		return engine.discover(discoveryRequest, UniqueId.forEngine(engine.getId()));
	}

	private File getClasspathRoot(Class<?> testClass) throws Exception {
		URL location = testClass.getProtectionDomain().getCodeSource().getLocation();
		return new File(location.toURI());
	}

	private void assertYieldsNoDescriptors(Class<?> testClass) {
		TestDiscoveryRequest request = discoveryRequestForClass(testClass);

		assertYieldsNoDescriptors(request);
	}

	private void assertYieldsNoDescriptors(TestDiscoveryRequest request) {
		TestDescriptor engineDescriptor = discoverTests(request);

		assertThat(engineDescriptor.getChildren()).isEmpty();
	}

	private static void assertRunnerTestDescriptor(TestDescriptor runnerDescriptor, Class<?> testClass) {
		assertTrue(runnerDescriptor.isContainer());
		assertFalse(runnerDescriptor.isTest());
		assertEquals(testClass.getName(), runnerDescriptor.getDisplayName());
		Assertions.assertEquals(JUnit4UniqueIdBuilder.uniqueIdForClass(testClass), runnerDescriptor.getUniqueId());
		assertClassSource(testClass, runnerDescriptor);
	}

	private static void assertTestMethodDescriptor(TestDescriptor testMethodDescriptor, Class<?> testClass,
			String methodName, UniqueId uniqueContainerId) throws Exception {
		assertTrue(testMethodDescriptor.isTest());
		assertFalse(testMethodDescriptor.isContainer());
		assertEquals(methodName, testMethodDescriptor.getDisplayName());
		Assertions.assertEquals(JUnit4UniqueIdBuilder.uniqueIdForMethod(uniqueContainerId, testClass, methodName),
			testMethodDescriptor.getUniqueId());
		assertThat(testMethodDescriptor.getChildren()).isEmpty();
		assertMethodSource(testClass.getMethod(methodName), testMethodDescriptor);
	}

	private static void assertContainerTestDescriptor(TestDescriptor containerDescriptor, Class<?> suiteClass,
			Class<?> testClass) {
		assertTrue(containerDescriptor.isContainer());
		assertFalse(containerDescriptor.isTest());
		assertEquals(testClass.getName(), containerDescriptor.getDisplayName());
		Assertions.assertEquals(JUnit4UniqueIdBuilder.uniqueIdForClasses(suiteClass, testClass),
			containerDescriptor.getUniqueId());
		assertClassSource(testClass, containerDescriptor);
	}

	private static void assertInitializationError(TestDescriptor testDescriptor, Class<?> failingClass,
			Class<?> testClass) {
		assertTrue(testDescriptor.isTest());
		assertFalse(testDescriptor.isContainer());
		assertEquals("initializationError", testDescriptor.getDisplayName());
		Assertions.assertEquals(JUnit4UniqueIdBuilder.uniqueIdForErrorInClass(testClass, failingClass),
			testDescriptor.getUniqueId());
		assertThat(testDescriptor.getChildren()).isEmpty();
		assertClassSource(failingClass, testDescriptor);
	}

	private static void assertClassSource(Class<?> expectedClass, TestDescriptor testDescriptor) {
		assertThat(testDescriptor.getSource()).containsInstanceOf(JavaClassSource.class);
		JavaClassSource classSource = (JavaClassSource) testDescriptor.getSource().get();
		assertThat(classSource.getJavaClass()).isEqualTo(expectedClass);
	}

	private static void assertMethodSource(Method expectedMethod, TestDescriptor testDescriptor) {
		assertThat(testDescriptor.getSource()).containsInstanceOf(JavaMethodSource.class);
		JavaMethodSource methodSource = (JavaMethodSource) testDescriptor.getSource().get();
		assertThat(methodSource.getJavaClass()).isEqualTo(expectedMethod.getDeclaringClass());
		assertThat(methodSource.getJavaMethodName()).isEqualTo(expectedMethod.getName());
		assertThat(methodSource.getJavaMethodParameterTypes()).containsExactly(expectedMethod.getParameterTypes());
	}

	private static TestDiscoveryRequest discoveryRequestForClass(Class<?> testClass) {
		return request().selectors(selectClass(testClass)).build();
	}
}
