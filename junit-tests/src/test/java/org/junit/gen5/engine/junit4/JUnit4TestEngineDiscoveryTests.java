/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4;

import static java.text.MessageFormat.format;
import static java.util.Collections.singleton;
import static java.util.function.Predicate.isEqual;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertFalse;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.junit.gen5.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.gen5.commons.util.FunctionUtils.where;
import static org.junit.gen5.engine.discovery.ClassSelector.forClass;
import static org.junit.gen5.engine.discovery.ClasspathSelector.forPaths;
import static org.junit.gen5.engine.discovery.PackageSelector.forPackageName;
import static org.junit.gen5.engine.discovery.UniqueIdSelector.forUniqueId;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestTag;
import org.junit.gen5.engine.discovery.ClassFilter;
import org.junit.gen5.engine.discovery.MethodSelector;
import org.junit.gen5.engine.junit4.samples.PlainOldJavaClassWithoutAnyTest;
import org.junit.gen5.engine.junit4.samples.junit3.JUnit3SuiteWithSingleTestCaseWithSingleTestWhichFails;
import org.junit.gen5.engine.junit4.samples.junit3.PlainJUnit3TestCaseWithSingleTestWhichFails;
import org.junit.gen5.engine.junit4.samples.junit4.Categories.Failing;
import org.junit.gen5.engine.junit4.samples.junit4.Categories.Plain;
import org.junit.gen5.engine.junit4.samples.junit4.Categories.Skipped;
import org.junit.gen5.engine.junit4.samples.junit4.Categories.SkippedWithReason;
import org.junit.gen5.engine.junit4.samples.junit4.IgnoredJUnit4TestCase;
import org.junit.gen5.engine.junit4.samples.junit4.JUnit4SuiteWithPlainJUnit4TestCaseWithSingleTestWhichIsIgnored;
import org.junit.gen5.engine.junit4.samples.junit4.JUnit4SuiteWithTwoTestCases;
import org.junit.gen5.engine.junit4.samples.junit4.JUnit4TestCaseWithOverloadedMethod;
import org.junit.gen5.engine.junit4.samples.junit4.PlainJUnit4TestCaseWithFiveTestMethods;
import org.junit.gen5.engine.junit4.samples.junit4.PlainJUnit4TestCaseWithSingleInheritedTestWhichFails;
import org.junit.gen5.engine.junit4.samples.junit4.PlainJUnit4TestCaseWithSingleTestWhichFails;
import org.junit.gen5.engine.junit4.samples.junit4.PlainJUnit4TestCaseWithSingleTestWhichIsIgnored;
import org.junit.gen5.engine.junit4.samples.junit4.PlainJUnit4TestCaseWithTwoTestMethods;
import org.junit.gen5.engine.junit4.samples.junit4.SingleFailingTheoryTestCase;
import org.junit.gen5.engine.junit4.samples.junit4.TestCaseRunWithJUnit5;
import org.junit.gen5.engine.support.descriptor.JavaSource;
import org.junit.gen5.launcher.TestDiscoveryRequest;
import org.junit.runner.manipulation.Filter;

class JUnit4TestEngineDiscoveryTests {

	JUnit4TestEngine engine = new JUnit4TestEngine();

	@Test
	void resolvesSimpleJUnit4TestClass() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithSingleTestWhichFails.class;
		TestDiscoveryRequest discoveryRequest = discoveryRequestForClass(testClass);

		TestDescriptor engineDescriptor = engine.discover(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		TestDescriptor childDescriptor = getOnlyElement(runnerDescriptor.getChildren());
		assertTestMethodDescriptor(childDescriptor, testClass, "failingTest", "junit4:" + testClass.getName() + "/");
	}

	@Test
	void resolvesIgnoredJUnit4TestClass() {
		Class<?> testClass = IgnoredJUnit4TestCase.class;
		TestDiscoveryRequest discoveryRequest = discoveryRequestForClass(testClass);

		TestDescriptor engineDescriptor = engine.discover(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertFalse(runnerDescriptor.isContainer());
		assertTrue(runnerDescriptor.isTest());
		assertEquals(testClass.getName(), runnerDescriptor.getDisplayName());
		assertEquals("junit4:" + testClass.getName(), runnerDescriptor.getUniqueId());
		assertThat(runnerDescriptor.getChildren()).isEmpty();
	}

	@Test
	void resolvesJUnit4TestClassWithCustomRunner() throws Exception {
		Class<?> testClass = SingleFailingTheoryTestCase.class;
		TestDiscoveryRequest discoveryRequest = discoveryRequestForClass(testClass);

		TestDescriptor engineDescriptor = engine.discover(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		TestDescriptor childDescriptor = getOnlyElement(runnerDescriptor.getChildren());
		assertTestMethodDescriptor(childDescriptor, testClass, "theory", "junit4:" + testClass.getName() + "/");
	}

	@Test
	void resolvesJUnit3TestCase() throws Exception {
		Class<?> testClass = PlainJUnit3TestCaseWithSingleTestWhichFails.class;
		TestDiscoveryRequest discoveryRequest = discoveryRequestForClass(testClass);

		TestDescriptor engineDescriptor = engine.discover(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		TestDescriptor childDescriptor = getOnlyElement(runnerDescriptor.getChildren());
		assertTestMethodDescriptor(childDescriptor, testClass, "test", "junit4:" + testClass.getName() + "/");
	}

	@Test
	void resolvesJUnit3SuiteWithSingleTestCaseWithSingleTestWhichFails() throws Exception {
		Class<?> suiteClass = JUnit3SuiteWithSingleTestCaseWithSingleTestWhichFails.class;
		Class<?> testClass = PlainJUnit3TestCaseWithSingleTestWhichFails.class;
		TestDiscoveryRequest discoveryRequest = discoveryRequestForClass(suiteClass);

		TestDescriptor engineDescriptor = engine.discover(discoveryRequest);

		TestDescriptor suiteDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(suiteDescriptor, suiteClass);

		TestDescriptor testClassDescriptor = getOnlyElement(suiteDescriptor.getChildren());
		assertContainerTestDescriptor(testClassDescriptor, "junit4:" + suiteClass.getName() + "/", testClass);

		TestDescriptor testMethodDescriptor = getOnlyElement(testClassDescriptor.getChildren());
		assertTestMethodDescriptor(testMethodDescriptor, testClass, "test",
			"junit4:" + suiteClass.getName() + "/" + testClass.getName() + "/");
	}

	@Test
	void resolvesJUnit4SuiteWithPlainJUnit4TestCaseWithSingleTestWhichIsIgnored() throws Exception {
		Class<?> suiteClass = JUnit4SuiteWithPlainJUnit4TestCaseWithSingleTestWhichIsIgnored.class;
		Class<?> testClass = PlainJUnit4TestCaseWithSingleTestWhichIsIgnored.class;
		TestDiscoveryRequest discoveryRequest = discoveryRequestForClass(suiteClass);

		TestDescriptor engineDescriptor = engine.discover(discoveryRequest);

		TestDescriptor suiteDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(suiteDescriptor, suiteClass);

		TestDescriptor testClassDescriptor = getOnlyElement(suiteDescriptor.getChildren());
		assertContainerTestDescriptor(testClassDescriptor, "junit4:" + suiteClass.getName() + "/", testClass);

		TestDescriptor testMethodDescriptor = getOnlyElement(testClassDescriptor.getChildren());
		assertTestMethodDescriptor(testMethodDescriptor, testClass, "ignoredTest",
			"junit4:" + suiteClass.getName() + "/" + testClass.getName() + "/");
	}

	@Test
	void resolvesJUnit4TestCaseWithOverloadedMethod() {
		Class<?> testClass = JUnit4TestCaseWithOverloadedMethod.class;
		TestDiscoveryRequest discoveryRequest = discoveryRequestForClass(testClass);

		TestDescriptor engineDescriptor = engine.discover(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		List<TestDescriptor> testMethodDescriptors = new ArrayList<>(runnerDescriptor.getChildren());
		assertThat(testMethodDescriptors).hasSize(2);

		TestDescriptor testMethodDescriptor = testMethodDescriptors.get(0);
		assertEquals("theory", testMethodDescriptor.getDisplayName());
		assertEquals("junit4:" + testClass.getName() + "/theory" + "(" + testClass.getName() + ")[0]",
			testMethodDescriptor.getUniqueId());
		assertClassSource(testClass, testMethodDescriptor);

		testMethodDescriptor = testMethodDescriptors.get(1);
		assertEquals("theory", testMethodDescriptor.getDisplayName());
		assertEquals("junit4:" + testClass.getName() + "/theory" + "(" + testClass.getName() + ")[1]",
			testMethodDescriptor.getUniqueId());
		assertClassSource(testClass, testMethodDescriptor);
	}

	@Test
	void doesNotResolvePlainOldJavaClassesWithoutAnyTest() {
		assertYieldsNoDescriptors(PlainOldJavaClassWithoutAnyTest.class);
	}

	@Test
	void doesNotResolveClassRunWithJUnit5() {
		assertYieldsNoDescriptors(TestCaseRunWithJUnit5.class);
	}

	@Test
	void resolvesClasspathSelector() throws Exception {
		File root = getClasspathRoot(PlainJUnit4TestCaseWithSingleTestWhichFails.class);
		TestDiscoveryRequest discoveryRequest = request().select(forPaths(singleton(root))).build();
		TestDescriptor engineDescriptor = engine.discover(discoveryRequest);

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

		TestDiscoveryRequest discoveryRequest = request().select(forPaths(singleton(root))).filter(
			ClassFilter.byNamePattern(".*JUnit4.*"), ClassFilter.byNamePattern(".*Plain.*")).build();

		TestDescriptor engineDescriptor = engine.discover(discoveryRequest);

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

		TestDiscoveryRequest discoveryRequest = request().select(
			forPackageName(testClass.getPackage().getName())).build();

		TestDescriptor engineDescriptor = engine.discover(discoveryRequest);

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

		TestDiscoveryRequest discoveryRequest = request().select(
			forPackageName(testClass.getPackage().getName())).build();

		TestDescriptor engineDescriptor = engine.discover(discoveryRequest);

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

		TestDescriptor engineDescriptor = engine.discover(discoveryRequest);

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

		TestDescriptor engineDescriptor = engine.discover(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertThat(runnerDescriptor.getTags()).containsOnly(new TestTag(Plain.class.getName()));

		TestDescriptor failingTest = findChildByDisplayName(runnerDescriptor, "failingTest");
		assertThat(failingTest.getTags()).containsOnly(//
			new TestTag(Plain.class.getName()), //
			new TestTag(Failing.class.getName()));

		TestDescriptor ignoredWithoutReason = findChildByDisplayName(runnerDescriptor, "ignoredTest1_withoutReason");
		assertThat(ignoredWithoutReason.getTags()).containsOnly(//
			new TestTag(Plain.class.getName()), //
			new TestTag(Skipped.class.getName()));

		TestDescriptor ignoredWithReason = findChildByDisplayName(runnerDescriptor, "ignoredTest2_withReason");
		assertThat(ignoredWithReason.getTags()).containsOnly(//
			new TestTag(Plain.class.getName()), //
			new TestTag(Skipped.class.getName()), //
			new TestTag(SkippedWithReason.class.getName()));
	}

	@Test
	void resolvesMethodSelectorForSingleMethod() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;
		TestDiscoveryRequest discoveryRequest = request().select(
			MethodSelector.forMethod(testClass, testClass.getMethod("failingTest"))).build();

		TestDescriptor engineDescriptor = engine.discover(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		TestDescriptor childDescriptor = getOnlyElement(runnerDescriptor.getChildren());
		assertTestMethodDescriptor(childDescriptor, testClass, "failingTest", "junit4:" + testClass.getName() + "/");
	}

	@Test
	void resolvesMethodSelectorForTwoMethodsOfSameClass() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;
		TestDiscoveryRequest discoveryRequest = request().select(
			MethodSelector.forMethod(testClass, testClass.getMethod("failingTest")),
			MethodSelector.forMethod(testClass, testClass.getMethod("successfulTest"))).build();

		TestDescriptor engineDescriptor = engine.discover(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		List<TestDescriptor> testMethodDescriptors = new ArrayList<>(runnerDescriptor.getChildren());
		assertThat(testMethodDescriptors).hasSize(2);

		TestDescriptor failingTest = testMethodDescriptors.get(0);
		assertTestMethodDescriptor(failingTest, testClass, "failingTest", "junit4:" + testClass.getName() + "/");

		TestDescriptor successfulTest = testMethodDescriptors.get(1);
		assertTestMethodDescriptor(successfulTest, testClass, "successfulTest", "junit4:" + testClass.getName() + "/");
	}

	@Test
	void resolvesUniqueIdSelectorForSingleMethod() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;
		TestDiscoveryRequest discoveryRequest = request().select(forUniqueId(
			"junit4:org.junit.gen5.engine.junit4.samples.junit4.PlainJUnit4TestCaseWithFiveTestMethods/failingTest(org.junit.gen5.engine.junit4.samples.junit4.PlainJUnit4TestCaseWithFiveTestMethods)")).build();

		TestDescriptor engineDescriptor = engine.discover(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		TestDescriptor childDescriptor = getOnlyElement(runnerDescriptor.getChildren());
		assertTestMethodDescriptor(childDescriptor, testClass, "failingTest", "junit4:" + testClass.getName() + "/");
	}

	@Test
	void resolvesUniqueIdSelectorForSingleClass() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;
		TestDiscoveryRequest discoveryRequest = request().select(forUniqueId(
			"junit4:org.junit.gen5.engine.junit4.samples.junit4.PlainJUnit4TestCaseWithFiveTestMethods")).build();

		TestDescriptor engineDescriptor = engine.discover(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		assertThat(runnerDescriptor.getChildren()).hasSize(5);
	}

	@Test
	void resolvesUniqueIdSelectorOfSingleClassWithinSuite() throws Exception {
		Class<?> suiteClass = JUnit4SuiteWithTwoTestCases.class;
		Class<?> testClass = PlainJUnit4TestCaseWithSingleTestWhichFails.class;
		TestDiscoveryRequest discoveryRequest = request().select(
			forUniqueId("junit4:org.junit.gen5.engine.junit4.samples.junit4.JUnit4SuiteWithTwoTestCases"
					+ "/org.junit.gen5.engine.junit4.samples.junit4.PlainJUnit4TestCaseWithSingleTestWhichFails")).build();

		TestDescriptor engineDescriptor = engine.discover(discoveryRequest);

		TestDescriptor suiteDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(suiteDescriptor, suiteClass);

		TestDescriptor testClassDescriptor = getOnlyElement(suiteDescriptor.getChildren());
		assertContainerTestDescriptor(testClassDescriptor, "junit4:" + suiteClass.getName() + "/", testClass);

		TestDescriptor testMethodDescriptor = getOnlyElement(testClassDescriptor.getChildren());
		assertTestMethodDescriptor(testMethodDescriptor, testClass, "failingTest",
			"junit4:" + suiteClass.getName() + "/" + testClass.getName() + "/");
	}

	@Test
	void resolvesUniqueIdSelectorOfSingleMethodWithinSuite() throws Exception {
		Class<?> suiteClass = JUnit4SuiteWithTwoTestCases.class;
		Class<?> testClass = PlainJUnit4TestCaseWithTwoTestMethods.class;
		TestDiscoveryRequest discoveryRequest = request().select(
			forUniqueId("junit4:org.junit.gen5.engine.junit4.samples.junit4.JUnit4SuiteWithTwoTestCases"
					+ "/org.junit.gen5.engine.junit4.samples.junit4.PlainJUnit4TestCaseWithTwoTestMethods"
					+ "/successfulTest(org.junit.gen5.engine.junit4.samples.junit4.PlainJUnit4TestCaseWithTwoTestMethods)")).build();

		TestDescriptor engineDescriptor = engine.discover(discoveryRequest);

		TestDescriptor suiteDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(suiteDescriptor, suiteClass);

		TestDescriptor testClassDescriptor = getOnlyElement(suiteDescriptor.getChildren());
		assertContainerTestDescriptor(testClassDescriptor, "junit4:" + suiteClass.getName() + "/", testClass);

		TestDescriptor testMethodDescriptor = getOnlyElement(testClassDescriptor.getChildren());
		assertTestMethodDescriptor(testMethodDescriptor, testClass, "successfulTest",
			"junit4:" + suiteClass.getName() + "/" + testClass.getName() + "/");
	}

	@Test
	void resolvesMultipleUniqueIdSelectorsForMethodsOfSameClass() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithTwoTestMethods.class;
		TestDiscoveryRequest discoveryRequest = request().select(
			forUniqueId("junit4:org.junit.gen5.engine.junit4.samples.junit4.PlainJUnit4TestCaseWithTwoTestMethods"
					+ "/successfulTest(org.junit.gen5.engine.junit4.samples.junit4.PlainJUnit4TestCaseWithTwoTestMethods)"),
			forUniqueId("junit4:org.junit.gen5.engine.junit4.samples.junit4.PlainJUnit4TestCaseWithTwoTestMethods"
					+ "/failingTest(org.junit.gen5.engine.junit4.samples.junit4.PlainJUnit4TestCaseWithTwoTestMethods)")).build();

		TestDescriptor engineDescriptor = engine.discover(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		List<TestDescriptor> testMethodDescriptors = new ArrayList<>(runnerDescriptor.getChildren());
		assertThat(testMethodDescriptors).hasSize(2);
		assertTestMethodDescriptor(testMethodDescriptors.get(0), testClass, "failingTest",
			"junit4:" + testClass.getName() + "/");
		assertTestMethodDescriptor(testMethodDescriptors.get(1), testClass, "successfulTest",
			"junit4:" + testClass.getName() + "/");
	}

	@Test
	void doesNotResolveMissingUniqueIdSelectorForSingleClass() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;
		TestDiscoveryRequest discoveryRequest = request().select(forUniqueId(
			"junit4:org.junit.gen5.engine.junit4.samples.junit4.PlainJUnit4TestCaseWithFiveTestMethods/doesNotExist")).build();

		TestDescriptor engineDescriptor = engine.discover(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		TestDescriptor testDescriptor = getOnlyElement(runnerDescriptor.getChildren());
		assertInitializationError(testDescriptor, Filter.class, "junit4:" + testClass.getName() + "/");
	}

	@Test
	void ignoresMoreFineGrainedSelectorsWhenClassIsSelectedAsWell() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;
		TestDiscoveryRequest discoveryRequest = request().select( //
			MethodSelector.forMethod(testClass, testClass.getMethod("failingTest")), //
			forUniqueId("junit4:org.junit.gen5.engine.junit4.samples.junit4.PlainJUnit4TestCaseWithFiveTestMethods"
					+ "/abortedTest(org.junit.gen5.engine.junit4.samples.junit4.PlainJUnit4TestCaseWithFiveTestMethods)"),
			forClass(testClass) //
		).build();

		TestDescriptor engineDescriptor = engine.discover(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		assertThat(runnerDescriptor.getChildren()).hasSize(5);
	}

	@Test
	void resolvesCombinationOfMethodAndUniqueIdSelector() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;
		TestDiscoveryRequest discoveryRequest = request().select( //
			MethodSelector.forMethod(testClass, testClass.getMethod("failingTest")), //
			forUniqueId("junit4:org.junit.gen5.engine.junit4.samples.junit4.PlainJUnit4TestCaseWithFiveTestMethods"
					+ "/abortedTest(org.junit.gen5.engine.junit4.samples.junit4.PlainJUnit4TestCaseWithFiveTestMethods)" //
		)).build();

		TestDescriptor engineDescriptor = engine.discover(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		List<TestDescriptor> testMethodDescriptors = new ArrayList<>(runnerDescriptor.getChildren());
		assertThat(testMethodDescriptors).hasSize(2);
		assertTestMethodDescriptor(testMethodDescriptors.get(0), testClass, "abortedTest",
			"junit4:" + testClass.getName() + "/");
		assertTestMethodDescriptor(testMethodDescriptors.get(1), testClass, "failingTest",
			"junit4:" + testClass.getName() + "/");
	}

	@Test
	void ignoresRedundantSelector() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;
		TestDiscoveryRequest discoveryRequest = request().select( //
			MethodSelector.forMethod(testClass, testClass.getMethod("failingTest")), //
			forUniqueId("junit4:org.junit.gen5.engine.junit4.samples.junit4.PlainJUnit4TestCaseWithFiveTestMethods"
					+ "/failingTest(org.junit.gen5.engine.junit4.samples.junit4.PlainJUnit4TestCaseWithFiveTestMethods)" //
		)).build();

		TestDescriptor engineDescriptor = engine.discover(discoveryRequest);

		TestDescriptor runnerDescriptor = getOnlyElement(engineDescriptor.getChildren());
		assertRunnerTestDescriptor(runnerDescriptor, testClass);

		TestDescriptor testMethodDescriptor = getOnlyElement(runnerDescriptor.getChildren());
		assertTestMethodDescriptor(testMethodDescriptor, testClass, "failingTest",
			"junit4:" + testClass.getName() + "/");
	}

	@Test
	void doesNotResolveMethodOfClassNotAcceptedByClassFilter() throws Exception {
		Class<?> testClass = PlainJUnit4TestCaseWithFiveTestMethods.class;
		// @formatter:off
		TestDiscoveryRequest request = request()
				.select(MethodSelector.forMethod(testClass, testClass.getMethod("failingTest")))
				.filter(ClassFilter.byNamePattern("Foo"))
				.build();
		// @formatter:on

		assertYieldsNoDescriptors(request);
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

	private File getClasspathRoot(Class<?> testClass) throws Exception {
		URL location = testClass.getProtectionDomain().getCodeSource().getLocation();
		return new File(location.toURI());
	}

	private void assertYieldsNoDescriptors(Class<?> testClass) {
		TestDiscoveryRequest request = discoveryRequestForClass(testClass);

		assertYieldsNoDescriptors(request);
	}

	private void assertYieldsNoDescriptors(TestDiscoveryRequest request) {
		TestDescriptor engineDescriptor = engine.discover(request);

		assertThat(engineDescriptor.getChildren()).isEmpty();
	}

	private static void assertRunnerTestDescriptor(TestDescriptor runnerDescriptor, Class<?> testClass) {
		assertContainerTestDescriptor(runnerDescriptor, "junit4:", testClass);
	}

	private static void assertTestMethodDescriptor(TestDescriptor testMethodDescriptor, Class<?> testClass,
			String methodName, String uniqueIdPrefix) throws Exception {
		assertTrue(testMethodDescriptor.isTest());
		assertFalse(testMethodDescriptor.isContainer());
		assertEquals(methodName, testMethodDescriptor.getDisplayName());
		assertEquals(methodName + "(" + testClass.getName() + ")", testMethodDescriptor.getName());
		assertEquals(uniqueIdPrefix + methodName + "(" + testClass.getName() + ")", testMethodDescriptor.getUniqueId());
		assertThat(testMethodDescriptor.getChildren()).isEmpty();
		assertMethodSource(testClass.getMethod(methodName), testMethodDescriptor);
	}

	private static void assertContainerTestDescriptor(TestDescriptor containerDescriptor, String uniqueIdPrefix,
			Class<?> testClass) {
		assertTrue(containerDescriptor.isContainer());
		assertFalse(containerDescriptor.isTest());
		assertEquals(testClass.getName(), containerDescriptor.getDisplayName());
		assertEquals(testClass.getName(), containerDescriptor.getName());
		assertEquals(uniqueIdPrefix + testClass.getName(), containerDescriptor.getUniqueId());
		assertClassSource(testClass, containerDescriptor);
	}

	private static void assertInitializationError(TestDescriptor testDescriptor, Class<?> failingClass,
			String uniqueIdPrefix) {
		assertTrue(testDescriptor.isTest());
		assertFalse(testDescriptor.isContainer());
		assertEquals("initializationError", testDescriptor.getDisplayName());
		assertEquals(uniqueIdPrefix + "initializationError" + "(" + failingClass.getName() + ")",
			testDescriptor.getUniqueId());
		assertThat(testDescriptor.getChildren()).isEmpty();
		assertClassSource(failingClass, testDescriptor);
	}

	private static void assertClassSource(Class<?> expectedClass, TestDescriptor testDescriptor) {
		assertThat(testDescriptor.getSource()).containsInstanceOf(JavaSource.class);
		JavaSource classSource = (JavaSource) testDescriptor.getSource().get();
		assertThat(classSource.getJavaClass()).hasValue(expectedClass);
		assertThat(classSource.getJavaMethodName()).isEmpty();
		assertThat(classSource.getJavaMethodParameterTypes()).isEmpty();
	}

	private static void assertMethodSource(Method expectedMethod, TestDescriptor testDescriptor) {
		assertThat(testDescriptor.getSource()).containsInstanceOf(JavaSource.class);
		JavaSource methodSource = (JavaSource) testDescriptor.getSource().get();
		assertThat(methodSource.getJavaClass()).hasValue(expectedMethod.getDeclaringClass());
		assertThat(methodSource.getJavaMethodName()).hasValue(expectedMethod.getName());
		assertThat(methodSource.getJavaMethodParameterTypes()).isPresent();
		assertThat(methodSource.getJavaMethodParameterTypes().get()).containsExactly(
			expectedMethod.getParameterTypes());
	}

	private static TestDiscoveryRequest discoveryRequestForClass(Class<?> testClass) {
		return request().select(forClass(testClass)).build();
	}
}
