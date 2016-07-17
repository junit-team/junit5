/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.runner;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.platform.engine.TestExecutionResult.successful;
import static org.junit.platform.launcher.core.LauncherFactoryForTestingPurposesOnly.createLauncher;
import static org.junit.runner.Description.createSuiteDescription;
import static org.junit.runner.Description.createTestDescription;
import static org.junit.runner.manipulation.Filter.matchMethodDescription;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassFilter;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.descriptor.JavaClassSource;
import org.junit.platform.engine.support.descriptor.JavaMethodSource;
import org.junit.platform.engine.support.hierarchical.DummyContainerDescriptor;
import org.junit.platform.engine.support.hierarchical.DummyTestDescriptor;
import org.junit.platform.engine.support.hierarchical.DummyTestEngine;
import org.junit.platform.engine.test.TestDescriptorStub;
import org.junit.platform.engine.test.TestEngineStub;
import org.junit.platform.launcher.EngineFilter;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.platform.launcher.TestPlan;
import org.junit.runner.Description;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

/**
 * Tests for the {@link JUnitPlatform} runner.
 *
 * @since 1.0
 */
class JUnitPlatformRunnerTests {

	@Nested
	class Discovery {

		@Test
		void requestsClassSelectorForAnnotatedClassWhenNoAdditionalAnnotationsArePresent() throws Exception {

			class TestCase {
			}

			LauncherDiscoveryRequest request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			List<ClassSelector> selectors = request.getSelectorsByType(ClassSelector.class);
			assertThat(selectors).hasSize(1);
			ClassSelector classSelector = getOnlyElement(selectors);
			assertEquals(TestCase.class, classSelector.getJavaClass());
		}

		@Test
		void requestsClassSelectorsWhenSelectClassesAnnotationIsPresent() throws Exception {

			@SelectClasses({ Short.class, Byte.class })
			class TestCase {
			}

			LauncherDiscoveryRequest request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			List<ClassSelector> selectors = request.getSelectorsByType(ClassSelector.class);
			assertThat(selectors).hasSize(2);
			assertEquals(Short.class, selectors.get(0).getJavaClass());
			assertEquals(Byte.class, selectors.get(1).getJavaClass());
		}

		@Test
		void requestsPackageSelectorsWhenPackagesAnnotationIsPresent() throws Exception {

			@SelectPackages({ "foo", "bar" })
			class TestCase {
			}

			LauncherDiscoveryRequest request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			List<PackageSelector> selectors = request.getSelectorsByType(PackageSelector.class);
			assertThat(selectors).hasSize(2);
			assertEquals("foo", selectors.get(0).getPackageName());
			assertEquals("bar", selectors.get(1).getPackageName());
		}

		@Test
		void addsTagFilterToRequestWhenIncludeTagsAnnotationIsPresent() throws Exception {

			@IncludeTags({ "foo", "bar" })
			class TestCase {
			}

			LauncherDiscoveryRequest request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			List<PostDiscoveryFilter> filters = request.getPostDiscoveryFilters();
			assertThat(filters).hasSize(1);

			PostDiscoveryFilter filter = filters.get(0);
			assertTrue(filter.apply(testDescriptorWithTag("foo")).included());
			assertTrue(filter.apply(testDescriptorWithTag("bar")).included());
			assertTrue(filter.apply(testDescriptorWithTag("baz")).excluded());
		}

		@Test
		void addsTagFilterToRequestWhenExcludeTagsAnnotationIsPresent() throws Exception {

			@ExcludeTags({ "foo", "bar" })
			class TestCase {
			}

			LauncherDiscoveryRequest request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			List<PostDiscoveryFilter> filters = request.getPostDiscoveryFilters();
			assertThat(filters).hasSize(1);

			PostDiscoveryFilter filter = filters.get(0);
			assertTrue(filter.apply(testDescriptorWithTag("foo")).excluded());
			assertTrue(filter.apply(testDescriptorWithTag("bar")).excluded());
			assertTrue(filter.apply(testDescriptorWithTag("baz")).included());
		}

		@Test
		void addsEngineFiltersToRequestWhenIncludeEnginesOrExcludeEnginesAnnotationsArePresent() throws Exception {

			@IncludeEngines({ "foo", "bar", "baz" })
			@ExcludeEngines({ "bar", "quux" })
			class TestCase {
			}

			TestEngine fooEngine = new TestEngineStub("foo");
			TestEngine barEngine = new TestEngineStub("bar");
			TestEngine bazEngine = new TestEngineStub("baz");
			TestEngine quuxEngine = new TestEngineStub("quux");

			LauncherDiscoveryRequest request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			List<EngineFilter> filters = request.getEngineFilters();
			assertThat(filters).hasSize(2);

			EngineFilter includeFilter = filters.get(0);
			assertTrue(includeFilter.apply(fooEngine).included());
			assertTrue(includeFilter.apply(barEngine).included());
			assertTrue(includeFilter.apply(bazEngine).included());
			assertTrue(includeFilter.apply(quuxEngine).excluded());

			EngineFilter excludeFilter = filters.get(1);
			assertTrue(excludeFilter.apply(fooEngine).included());
			assertTrue(excludeFilter.apply(barEngine).excluded());
			assertTrue(excludeFilter.apply(bazEngine).included());
			assertTrue(excludeFilter.apply(quuxEngine).excluded());
		}

		@Test
		void addsClassFilterToRequestWhenFilterClassNameAnnotationIsPresent() throws Exception {

			@IncludeClassNamePattern(".*Foo")
			class TestCase {
			}
			class Foo {
			}
			class Bar {
			}

			LauncherDiscoveryRequest request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			List<ClassFilter> filters = request.getDiscoveryFiltersByType(ClassFilter.class);
			assertThat(filters).hasSize(1);

			ClassFilter filter = filters.get(0);
			assertTrue(filter.apply(Foo.class).included());
			assertTrue(filter.apply(Bar.class).excluded());
		}

		@Test
		void convertsTestIdentifiersIntoDescriptions() throws Exception {

			TestDescriptor container1 = new TestDescriptorStub(UniqueId.root("root", "container1"), "container1");
			container1.addChild(new TestDescriptorStub(UniqueId.root("root", "test1"), "test1"));
			TestDescriptor container2 = new TestDescriptorStub(UniqueId.root("root", "container2"), "container2");
			container2.addChild(new TestDescriptorStub(UniqueId.root("root", "test2a"), "test2a"));
			container2.addChild(new TestDescriptorStub(UniqueId.root("root", "test2b"), "test2b"));
			TestPlan testPlan = TestPlan.from(asList(container1, container2));

			Launcher launcher = mock(Launcher.class);
			when(launcher.discover(any())).thenReturn(testPlan);

			JUnitPlatform runner = new JUnitPlatform(TestClass.class, launcher);

			Description runnerDescription = runner.getDescription();
			assertEquals(createSuiteDescription(TestClass.class), runnerDescription);

			List<Description> containerDescriptions = runnerDescription.getChildren();
			assertThat(containerDescriptions).hasSize(2);
			assertEquals(suiteDescription("[root:container1]"), containerDescriptions.get(0));
			assertEquals(suiteDescription("[root:container2]"), containerDescriptions.get(1));

			List<Description> testDescriptions = containerDescriptions.get(0).getChildren();
			assertEquals(testDescription("[root:test1]"), getOnlyElement(testDescriptions));

			testDescriptions = containerDescriptions.get(1).getChildren();
			assertThat(testDescriptions).hasSize(2);
			assertEquals(testDescription("[root:test2a]"), testDescriptions.get(0));
			assertEquals(testDescription("[root:test2b]"), testDescriptions.get(1));
		}

	}

	@Nested
	class Filtering {

		@Test
		void appliesFilter() throws Exception {

			TestDescriptor originalParent1 = new TestDescriptorStub(UniqueId.root("root", "parent1"), "parent1");
			originalParent1.addChild(new TestDescriptorStub(UniqueId.root("root", "leaf1"), "leaf1"));
			TestDescriptor originalParent2 = new TestDescriptorStub(UniqueId.root("root", "parent2"), "parent2");
			originalParent2.addChild(new TestDescriptorStub(UniqueId.root("root", "leaf2a"), "leaf2a"));
			originalParent2.addChild(new TestDescriptorStub(UniqueId.root("root", "leaf2b"), "leaf2b"));
			TestPlan fullTestPlan = TestPlan.from(asList(originalParent1, originalParent2));

			TestDescriptor filteredParent = new TestDescriptorStub(UniqueId.root("root", "parent2"), "parent2");
			filteredParent.addChild(new TestDescriptorStub(UniqueId.root("root", "leaf2b"), "leaf2b"));
			TestPlan filteredTestPlan = TestPlan.from(singleton(filteredParent));

			Launcher launcher = mock(Launcher.class);
			ArgumentCaptor<LauncherDiscoveryRequest> captor = ArgumentCaptor.forClass(LauncherDiscoveryRequest.class);
			when(launcher.discover(captor.capture())).thenReturn(fullTestPlan).thenReturn(filteredTestPlan);

			JUnitPlatform runner = new JUnitPlatform(TestClass.class, launcher);
			runner.filter(matchMethodDescription(testDescription("[root:leaf2b]")));

			LauncherDiscoveryRequest lastDiscoveryRequest = captor.getValue();
			List<UniqueIdSelector> uniqueIdSelectors = lastDiscoveryRequest.getSelectorsByType(UniqueIdSelector.class);
			assertEquals("[root:leaf2b]", getOnlyElement(uniqueIdSelectors).getUniqueId().toString());

			Description parentDescription = getOnlyElement(runner.getDescription().getChildren());
			assertEquals(suiteDescription("[root:parent2]"), parentDescription);

			Description testDescription = getOnlyElement(parentDescription.getChildren());
			assertEquals(testDescription("[root:leaf2b]"), testDescription);
		}

		@Test
		void throwsNoTestsRemainExceptionWhenNoTestIdentifierMatchesFilter() throws Exception {
			TestPlan testPlan = TestPlan.from(singleton(new TestDescriptorStub(UniqueId.root("root", "test"), "test")));

			Launcher launcher = mock(Launcher.class);
			when(launcher.discover(any())).thenReturn(testPlan);

			JUnitPlatform runner = new JUnitPlatform(TestClass.class, launcher);

			assertThrows(NoTestsRemainException.class,
				() -> runner.filter(matchMethodDescription(suiteDescription("[root:doesNotExist]"))));
		}

	}

	@Nested
	class Execution {

		@Test
		void notifiesRunListenerOfTestExecution() throws Exception {
			DummyTestEngine engine = new DummyTestEngine("dummy");
			engine.addTest("failingTest", () -> fail("expected to fail"));
			engine.addTest("succeedingTest", () -> {
			});
			engine.addTest("abortedTest", () -> assumeFalse(true));
			engine.addTest("skippedTest", () -> fail("never called")).markSkipped("should be skipped");

			RunListener runListener = mock(RunListener.class);

			RunNotifier notifier = new RunNotifier();
			notifier.addListener(runListener);
			new JUnitPlatform(TestClass.class, createLauncher(engine)).run(notifier);

			InOrder inOrder = inOrder(runListener);

			inOrder.verify(runListener).testStarted(testDescription("[engine:dummy]/[test:failingTest]"));
			inOrder.verify(runListener).testFailure(any());
			inOrder.verify(runListener).testFinished(testDescription("[engine:dummy]/[test:failingTest]"));

			inOrder.verify(runListener).testStarted(testDescription("[engine:dummy]/[test:succeedingTest]"));
			inOrder.verify(runListener).testFinished(testDescription("[engine:dummy]/[test:succeedingTest]"));

			inOrder.verify(runListener).testStarted(testDescription("[engine:dummy]/[test:abortedTest]"));
			inOrder.verify(runListener).testAssumptionFailure(any());
			inOrder.verify(runListener).testFinished(testDescription("[engine:dummy]/[test:abortedTest]"));

			inOrder.verify(runListener).testIgnored(testDescription("[engine:dummy]/[test:skippedTest]"));

			inOrder.verifyNoMoreInteractions();
		}

		@Test
		void supportsDynamicTestRegistration() throws Exception {
			RunListener runListener = mock(RunListener.class);
			RunNotifier notifier = new RunNotifier();
			// notifier.addListener(new LoggingRunListener());
			notifier.addListener(runListener);
			new JUnitPlatform(TestClass.class, createLauncher(new DynamicTestEngine())).run(notifier);

			InOrder inOrder = inOrder(runListener);

			inOrder.verify(runListener).testStarted(testDescription("[engine:dynamic]/[container:1]/[test:1]"));
			inOrder.verify(runListener).testFinished(testDescription("[engine:dynamic]/[container:1]/[test:1]"));

			inOrder.verify(runListener).testStarted(testDescription("[engine:dynamic]/[container:1]/[test:2]"));
			inOrder.verify(runListener).testFinished(testDescription("[engine:dynamic]/[container:1]/[test:2]"));

			inOrder.verifyNoMoreInteractions();
		}

		@Test
		void reportsIgnoredEventsForLeavesWhenContainerIsSkipped() throws Exception {
			UniqueId uniqueEngineId = UniqueId.forEngine("engine");
			TestDescriptor engineDescriptor = new EngineDescriptor(uniqueEngineId, "engine");
			TestDescriptor container = new TestDescriptorStub(UniqueId.root("root", "container"), "container");
			container.addChild(new TestDescriptorStub(UniqueId.root("root", "leaf"), "leaf"));
			engineDescriptor.addChild(container);

			TestEngine engine = mock(TestEngine.class);
			when(engine.getId()).thenReturn("engine");
			when(engine.discover(any(), eq(uniqueEngineId))).thenReturn(engineDescriptor);
			doAnswer(invocation -> {
				ExecutionRequest request = invocation.getArgumentAt(0, ExecutionRequest.class);
				EngineExecutionListener listener = request.getEngineExecutionListener();
				listener.executionStarted(engineDescriptor);
				listener.executionSkipped(container, "deliberately skipped container");
				listener.executionFinished(engineDescriptor, successful());
				return null;
			}).when(engine).execute(any());

			RunListener runListener = mock(RunListener.class);

			RunNotifier notifier = new RunNotifier();
			notifier.addListener(runListener);
			new JUnitPlatform(TestClass.class, createLauncher(engine)).run(notifier);

			verify(runListener).testIgnored(testDescription("[root:leaf]"));
			verifyNoMoreInteractions(runListener);
		}

	}

	@Nested
	class Descriptions {

		@Test
		void descriptionForJavaMethodAndClassSources() throws Exception {
			DummyTestEngine engine = new DummyTestEngine("dummy");
			Method failingTest = getClass().getDeclaredMethod("failingTest");
			DummyContainerDescriptor containerDescriptor = engine.addContainer("uniqueContainerName",
				"containerDisplayName", new JavaClassSource(getClass()));
			containerDescriptor.addChild(
				new DummyTestDescriptor(containerDescriptor.getUniqueId().append("test", "failingTest"),
					"testDisplayName", new JavaMethodSource(failingTest), () -> {
					}));

			JUnitPlatform platformRunner = new JUnitPlatform(TestClass.class, createLauncher(engine));

			List<Description> children = platformRunner.getDescription().getChildren();
			assertEquals(1, children.size());
			Description engineDescription = children.get(0);
			assertEquals("dummy", engineDescription.getDisplayName());

			Description containerDescription = getOnlyElement(engineDescription.getChildren());
			Description testDescription = getOnlyElement(containerDescription.getChildren());

			// @formatter:off
			assertAll(
					() -> assertEquals("dummy", engineDescription.getDisplayName(), "engine display name"),
					() -> assertEquals("dummy", engineDescription.getClassName(), "engine class name"),
					() -> assertNull(engineDescription.getMethodName(), "engine method name"),
					() -> assertEquals("containerDisplayName", containerDescription.getDisplayName(), "container display name"),
					() -> assertEquals("containerDisplayName", containerDescription.getClassName(), "container class name"),
					() -> assertNull(containerDescription.getMethodName(), "container method name"),
					() -> assertEquals("testDisplayName(containerDisplayName)", testDescription.getDisplayName(), "test display name"),
					() -> assertEquals("containerDisplayName", testDescription.getClassName(), "test class name"),
					() -> assertEquals("testDisplayName", testDescription.getMethodName(), "test method name")
			);
			// @formatter:on
		}

		@Test
		void descriptionForJavaMethodAndClassSourcesUsingTechnicalNames() throws Exception {
			DummyTestEngine engine = new DummyTestEngine("dummy");
			Method failingTest = getClass().getDeclaredMethod("failingTest");
			DummyContainerDescriptor containerDescriptor = engine.addContainer("uniqueContainerName",
				"containerDisplayName", new JavaClassSource(getClass()));
			containerDescriptor.addChild(
				new DummyTestDescriptor(containerDescriptor.getUniqueId().append("test", "failingTest"),
					"testDisplayName", new JavaMethodSource(failingTest), () -> {
					}));

			JUnitPlatform platformRunner = new JUnitPlatform(TestClassWithTechnicalNames.class, createLauncher(engine));

			List<Description> children = platformRunner.getDescription().getChildren();
			assertEquals(1, children.size());
			Description engineDescription = children.get(0);
			assertEquals("dummy", engineDescription.getDisplayName());

			Description containerDescription = getOnlyElement(engineDescription.getChildren());
			Description testDescription = getOnlyElement(containerDescription.getChildren());

			// @formatter:off
			assertAll(
					() -> assertEquals("dummy", engineDescription.getDisplayName(), "engine display name"),
					() -> assertEquals("dummy", engineDescription.getClassName(), "engine class name"),
					() -> assertNull(engineDescription.getMethodName(), "engine method name"),
					() -> assertEquals(getClass().getName(), containerDescription.getDisplayName(), "container display name"),
					() -> assertEquals(getClass().getName(), containerDescription.getClassName(), "container class name"),
					() -> assertNull(containerDescription.getMethodName(), "container method name"),
					() -> assertEquals("failingTest(" + getClass().getName() + ")", testDescription.getDisplayName(), "test display name"),
					() -> assertEquals(getClass().getName(), testDescription.getClassName(), "test class name"),
					() -> assertEquals("failingTest", testDescription.getMethodName(), "test method name")
			);
			// @formatter:on
		}

		void failingTest() {
			// not actually invoked
		}

	}

	// -------------------------------------------------------------------------

	private static Description suiteDescription(String uniqueId) {
		return createSuiteDescription(uniqueId, uniqueId);
	}

	private static Description testDescription(String uniqueId) {
		return createTestDescription(uniqueId, uniqueId, uniqueId);
	}

	private TestDescriptor testDescriptorWithTag(String tag) {
		TestDescriptor testDescriptor = mock(TestDescriptor.class);
		when(testDescriptor.getTags()).thenReturn(singleton(TestTag.create(tag)));
		return testDescriptor;
	}

	private LauncherDiscoveryRequest instantiateRunnerAndCaptureGeneratedRequest(Class<?> testClass)
			throws InitializationError {
		Launcher launcher = mock(Launcher.class);
		ArgumentCaptor<LauncherDiscoveryRequest> captor = ArgumentCaptor.forClass(LauncherDiscoveryRequest.class);
		when(launcher.discover(captor.capture())).thenReturn(TestPlan.from(emptySet()));

		new JUnitPlatform(testClass, launcher);

		return captor.getValue();
	}

	private static class TestClass {
	}

	@UseTechnicalNames
	private static class TestClassWithTechnicalNames {
	}

	private static class DynamicTestEngine implements TestEngine {

		@Override
		public String getId() {
			return "dynamic";
		}

		@Override
		public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
			return new EngineDescriptor(uniqueId, "Dynamic Engine");
		}

		@Override
		public void execute(ExecutionRequest request) {
			EngineExecutionListener engineExecutionListener = request.getEngineExecutionListener();
			TestDescriptor root = request.getRootTestDescriptor();

			TestDescriptor container = new DemoContainerTestDescriptor(root.getUniqueId().append("container", "1"),
				"container #1");
			root.addChild(container);

			engineExecutionListener.dynamicTestRegistered(container);
			engineExecutionListener.executionStarted(container);

			UniqueId containerUid = container.getUniqueId();

			TestDescriptor dynamicTest1 = new DemoTestTestDescriptor(containerUid.append("test", "1"),
				"dynamic test #1");
			container.addChild(dynamicTest1);
			engineExecutionListener.dynamicTestRegistered(dynamicTest1);
			engineExecutionListener.executionStarted(dynamicTest1);
			engineExecutionListener.executionFinished(dynamicTest1, TestExecutionResult.successful());

			TestDescriptor dynamicTest2 = new DemoTestTestDescriptor(containerUid.append("test", "2"),
				"dynamic test #2");
			container.addChild(dynamicTest2);
			engineExecutionListener.dynamicTestRegistered(dynamicTest2);
			engineExecutionListener.executionStarted(dynamicTest2);
			engineExecutionListener.executionFinished(dynamicTest2, TestExecutionResult.successful());

			engineExecutionListener.executionFinished(container, TestExecutionResult.successful());
		}

	}

	private static class DemoContainerTestDescriptor extends AbstractTestDescriptor {

		DemoContainerTestDescriptor(UniqueId uniqueId, String displayName) {
			super(uniqueId, displayName);
		}

		@Override
		public boolean isContainer() {
			return true;
		}

		@Override
		public boolean isTest() {
			return false;
		}
	}

	private static class DemoTestTestDescriptor extends AbstractTestDescriptor {

		DemoTestTestDescriptor(UniqueId uniqueId, String displayName) {
			super(uniqueId, displayName);
		}

		@Override
		public boolean isContainer() {
			return false;
		}

		@Override
		public boolean isTest() {
			return true;
		}
	}

}
