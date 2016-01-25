/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.junit4.runner;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertThrows;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.junit.gen5.api.Assumptions.assumeFalse;
import static org.junit.gen5.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.gen5.engine.TestExecutionResult.successful;
import static org.junit.gen5.launcher.main.LauncherFactoryForTestingPurposesOnly.createLauncher;
import static org.junit.runner.Description.createSuiteDescription;
import static org.junit.runner.Description.createTestDescription;
import static org.junit.runner.manipulation.Filter.matchMethodDescription;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.gen5.api.Nested;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.EngineExecutionListener;
import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestDescriptorStub;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestTag;
import org.junit.gen5.engine.discovery.ClassFilter;
import org.junit.gen5.engine.discovery.ClassSelector;
import org.junit.gen5.engine.discovery.PackageSelector;
import org.junit.gen5.engine.discovery.UniqueIdSelector;
import org.junit.gen5.engine.support.hierarchical.DummyTestEngine;
import org.junit.gen5.launcher.EngineIdFilter;
import org.junit.gen5.launcher.Launcher;
import org.junit.gen5.launcher.PostDiscoveryFilter;
import org.junit.gen5.launcher.TestDiscoveryRequest;
import org.junit.gen5.launcher.TestId;
import org.junit.gen5.launcher.TestPlan;
import org.junit.runner.Description;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

class JUnit5Tests {

	@Nested
	class Discovery {

		@Test
		void requestsClassSelectorForAnnotatedClassWhenNoAdditionalAnnotationsArePresent() throws Exception {
			class TestCase {
			}

			TestDiscoveryRequest request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			assertThat(request.getSelectors()).hasSize(1);
			ClassSelector classSelector = getOnlyElement(request.getSelectorsByType(ClassSelector.class));
			assertEquals(TestCase.class, classSelector.getTestClass());
		}

		@Test
		void requestsClassSelectorsWhenClassesAnnotationIsPresent() throws Exception {
			@Classes({ Short.class, Byte.class })
			class TestCase {
			}

			TestDiscoveryRequest request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			assertThat(request.getSelectors()).hasSize(2);
			List<ClassSelector> selectors = request.getSelectorsByType(ClassSelector.class);
			assertEquals(Short.class, selectors.get(0).getTestClass());
			assertEquals(Byte.class, selectors.get(1).getTestClass());
		}

		@Test
		void requestsUniqueIdSelectorsWhenUniqueIdsAnnotationIsPresent() throws Exception {
			@UniqueIds({ "foo", "bar" })
			class TestCase {
			}

			TestDiscoveryRequest request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			assertThat(request.getSelectors()).hasSize(2);
			List<UniqueIdSelector> selectors = request.getSelectorsByType(UniqueIdSelector.class);
			assertEquals("foo", selectors.get(0).getUniqueId());
			assertEquals("bar", selectors.get(1).getUniqueId());
		}

		@Test
		void requestsPackageSelectorsWhenPackagesAnnotationIsPresent() throws Exception {
			@Packages({ "foo", "bar" })
			class TestCase {
			}

			TestDiscoveryRequest request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			assertThat(request.getSelectors()).hasSize(2);
			List<PackageSelector> selectors = request.getSelectorsByType(PackageSelector.class);
			assertEquals("foo", selectors.get(0).getPackageName());
			assertEquals("bar", selectors.get(1).getPackageName());
		}

		@Test
		void addsTagFilterToRequestWhenOnlyIncludeTagsAnnotationIsPresent() throws Exception {
			@OnlyIncludeTags({ "foo", "bar" })
			class TestCase {
			}

			TestDiscoveryRequest request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			List<PostDiscoveryFilter> filters = request.getPostDiscoveryFilters();
			assertThat(filters).hasSize(1);

			PostDiscoveryFilter filter = filters.get(0);
			assertTrue(filter.filter(testDescriptorWithTag("foo")).included());
			assertTrue(filter.filter(testDescriptorWithTag("bar")).included());
			assertTrue(filter.filter(testDescriptorWithTag("baz")).excluded());
		}

		@Test
		void addsTagFilterToRequestWhenExcludeTagsAnnotationIsPresent() throws Exception {
			@ExcludeTags({ "foo", "bar" })
			class TestCase {
			}

			TestDiscoveryRequest request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			List<PostDiscoveryFilter> filters = request.getPostDiscoveryFilters();
			assertThat(filters).hasSize(1);

			PostDiscoveryFilter filter = filters.get(0);
			assertTrue(filter.filter(testDescriptorWithTag("foo")).excluded());
			assertTrue(filter.filter(testDescriptorWithTag("bar")).excluded());
			assertTrue(filter.filter(testDescriptorWithTag("baz")).included());
		}

		@Test
		void addsEngineIdFilterToRequestWhenOnlyEngineAnnotationIsPresent() throws Exception {
			@OnlyEngine("foo")
			class TestCase {
			}

			TestDiscoveryRequest request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			List<EngineIdFilter> filters = request.getEngineIdFilters();
			assertThat(filters).hasSize(1);

			EngineIdFilter filter = filters.get(0);
			assertTrue(filter.filter("foo").included());
			assertTrue(filter.filter("bar").excluded());
		}

		@Test
		void addsClassFilterToRequestWhenClassNamePatternAnnotationIsPresent() throws Exception {
			@ClassNamePattern(".*Foo")
			class TestCase {
			}
			class Foo {
			}
			class Bar {
			}

			TestDiscoveryRequest request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			List<ClassFilter> filters = request.getDiscoveryFiltersByType(ClassFilter.class);
			assertThat(filters).hasSize(1);

			ClassFilter filter = filters.get(0);
			assertTrue(filter.filter(Foo.class).included());
			assertTrue(filter.filter(Bar.class).excluded());
		}

		@Test
		void convertsTestIdentifiersIntoDescriptions() throws Exception {

			TestDescriptor container1 = new TestDescriptorStub("container1");
			container1.addChild(new TestDescriptorStub("test1"));
			TestDescriptor container2 = new TestDescriptorStub("container2");
			container2.addChild(new TestDescriptorStub("test2a"));
			container2.addChild(new TestDescriptorStub("test2b"));
			TestPlan testPlan = TestPlan.from(asList(container1, container2));

			Launcher launcher = mock(Launcher.class);
			when(launcher.discover(any())).thenReturn(testPlan);

			JUnit5 runner = new JUnit5(TestClass.class, launcher);

			Description runnerDescription = runner.getDescription();
			assertEquals(createSuiteDescription(TestClass.class), runnerDescription);

			List<Description> containerDescriptions = runnerDescription.getChildren();
			assertThat(containerDescriptions).hasSize(2);
			assertEquals(suiteDescription("container1"), containerDescriptions.get(0));
			assertEquals(suiteDescription("container2"), containerDescriptions.get(1));

			List<Description> testDescriptions = containerDescriptions.get(0).getChildren();
			assertEquals(testDescription("test1"), getOnlyElement(testDescriptions));

			testDescriptions = containerDescriptions.get(1).getChildren();
			assertThat(testDescriptions).hasSize(2);
			assertEquals(testDescription("test2a"), testDescriptions.get(0));
			assertEquals(testDescription("test2b"), testDescriptions.get(1));
		}

	}

	@Nested
	class Filtering {

		@Test
		void appliesFilter() throws Exception {

			TestDescriptor originalParent1 = new TestDescriptorStub("parent1");
			originalParent1.addChild(new TestDescriptorStub("leaf1"));
			TestDescriptor originalParent2 = new TestDescriptorStub("parent2");
			originalParent2.addChild(new TestDescriptorStub("leaf2a"));
			originalParent2.addChild(new TestDescriptorStub("leaf2b"));
			TestPlan fullTestPlan = TestPlan.from(asList(originalParent1, originalParent2));

			TestDescriptor filteredParent = new TestDescriptorStub("parent2");
			filteredParent.addChild(new TestDescriptorStub("leaf2b"));
			TestPlan filteredTestPlan = TestPlan.from(singleton(filteredParent));

			Launcher launcher = mock(Launcher.class);
			ArgumentCaptor<TestDiscoveryRequest> captor = ArgumentCaptor.forClass(TestDiscoveryRequest.class);
			when(launcher.discover(captor.capture())).thenReturn(fullTestPlan).thenReturn(filteredTestPlan);

			JUnit5 runner = new JUnit5(TestClass.class, launcher);
			runner.filter(matchMethodDescription(testDescription("leaf2b")));

			TestDiscoveryRequest lastDiscoveryRequest = captor.getValue();
			List<UniqueIdSelector> uniqueIdSelectors = lastDiscoveryRequest.getSelectorsByType(UniqueIdSelector.class);
			assertEquals("leaf2b", getOnlyElement(uniqueIdSelectors).getUniqueId());

			Description parentDescription = getOnlyElement(runner.getDescription().getChildren());
			assertEquals(suiteDescription("parent2"), parentDescription);

			Description testDescription = getOnlyElement(parentDescription.getChildren());
			assertEquals(testDescription("leaf2b"), testDescription);
		}

		@Test
		void throwsNoTestsRemainExceptionWhenNoTestIdentifierMatchesFilter() throws Exception {
			TestPlan testPlan = TestPlan.from(singleton(new TestDescriptorStub("test")));

			Launcher launcher = mock(Launcher.class);
			when(launcher.discover(any())).thenReturn(testPlan);

			JUnit5 runner = new JUnit5(TestClass.class, launcher);

			assertThrows(NoTestsRemainException.class,
				() -> runner.filter(matchMethodDescription(suiteDescription("doesNotExist"))));
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
			engine.addTest("skippedTest", () -> assumeFalse(true));
			engine.addTest("ignoredTest", () -> fail("never called")).markSkipped("should be skipped");

			RunListener runListener = mock(RunListener.class);

			RunNotifier notifier = new RunNotifier();
			notifier.addListener(runListener);
			new JUnit5(TestClass.class, createLauncher(engine)).run(notifier);

			InOrder inOrder = inOrder(runListener);

			inOrder.verify(runListener).testStarted(testDescription("dummy:failingTest"));
			inOrder.verify(runListener).testFailure(any());
			inOrder.verify(runListener).testFinished(testDescription("dummy:failingTest"));

			inOrder.verify(runListener).testStarted(testDescription("dummy:succeedingTest"));
			inOrder.verify(runListener).testFinished(testDescription("dummy:succeedingTest"));

			inOrder.verify(runListener).testStarted(testDescription("dummy:skippedTest"));
			inOrder.verify(runListener).testAssumptionFailure(any());
			inOrder.verify(runListener).testFinished(testDescription("dummy:skippedTest"));

			inOrder.verify(runListener).testIgnored(testDescription("dummy:ignoredTest"));

			inOrder.verifyNoMoreInteractions();
		}

		@Test
		void reportsIgnoredEventsForLeafsWhenContainerIsSkipped() throws Exception {
			TestDescriptor engineDescriptor = new TestDescriptorStub("engine");
			TestDescriptor container = new TestDescriptorStub("container");
			container.addChild(new TestDescriptorStub("leaf"));
			engineDescriptor.addChild(container);

			TestEngine engine = mock(TestEngine.class);
			when(engine.discover(any())).thenReturn(engineDescriptor);
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
			new JUnit5(TestClass.class, createLauncher(engine)).run(notifier);

			verify(runListener).testIgnored(testDescription("leaf"));
			verifyNoMoreInteractions(runListener);
		}

	}

	private static Description suiteDescription(String uniqueId) {
		return createSuiteDescription(uniqueId, new TestId(uniqueId));
	}

	private static Description testDescription(String uniqueId) {
		return createTestDescription(uniqueId, uniqueId, new TestId(uniqueId));
	}

	private TestDescriptor testDescriptorWithTag(String tag) {
		TestDescriptor testDescriptor = mock(TestDescriptor.class);
		when(testDescriptor.getTags()).thenReturn(singleton(new TestTag(tag)));
		return testDescriptor;
	}

	private TestDiscoveryRequest instantiateRunnerAndCaptureGeneratedRequest(Class<?> testClass)
			throws InitializationError {
		Launcher launcher = mock(Launcher.class);
		ArgumentCaptor<TestDiscoveryRequest> captor = ArgumentCaptor.forClass(TestDiscoveryRequest.class);
		when(launcher.discover(captor.capture())).thenReturn(TestPlan.from(emptySet()));

		new JUnit5(testClass, launcher);

		return captor.getValue();
	}

	private static class TestClass {
	}
}
