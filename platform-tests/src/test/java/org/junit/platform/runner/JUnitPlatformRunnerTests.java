/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.runner;

import static java.util.stream.Collectors.toSet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.platform.engine.TestExecutionResult.successful;
import static org.junit.platform.engine.discovery.ClassNameFilter.STANDARD_INCLUDE_PATTERN;
import static org.junit.platform.launcher.core.LauncherFactoryForTestingPurposesOnly.createLauncher;
import static org.junit.runner.Description.createSuiteDescription;
import static org.junit.runner.Description.createTestDescription;
import static org.junit.runner.manipulation.Filter.matchMethodDescription;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.engine.discovery.PackageNameFilter;
import org.junit.platform.engine.discovery.PackageSelector;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.engine.support.hierarchical.DemoHierarchicalTestDescriptor;
import org.junit.platform.engine.support.hierarchical.DemoHierarchicalTestEngine;
import org.junit.platform.fakes.TestDescriptorStub;
import org.junit.platform.fakes.TestEngineStub;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.suite.api.ExcludeClassNamePatterns;
import org.junit.platform.suite.api.ExcludeEngines;
import org.junit.platform.suite.api.ExcludePackages;
import org.junit.platform.suite.api.ExcludeTags;
import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.IncludePackages;
import org.junit.platform.suite.api.IncludeTags;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.junit.platform.suite.api.UseTechnicalNames;
import org.junit.runner.Description;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.mockito.ArgumentCaptor;

/**
 * Tests for the {@link JUnitPlatform} runner.
 *
 * @since 1.0
 */
@Tag("junit4")
@SuppressWarnings("deprecation")
class JUnitPlatformRunnerTests {

	@Nested
	class Discovery {

		@Test
		void requestsClassSelectorForAnnotatedClassWhenNoAdditionalAnnotationsArePresent() {

			class TestCase {
			}

			var request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			var selectors = request.getSelectorsByType(ClassSelector.class);
			assertThat(selectors).hasSize(1);
			var classSelector = getOnlyElement(selectors);
			assertEquals(TestCase.class, classSelector.getJavaClass());
		}

		@Test
		void requestsClassSelectorsWhenSelectClassesAnnotationIsPresent() {

			@SelectClasses({ Short.class, Byte.class })
			class TestCase {
			}

			var request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			var selectors = request.getSelectorsByType(ClassSelector.class);
			assertThat(selectors).hasSize(2);
			assertEquals(Short.class, selectors.get(0).getJavaClass());
			assertEquals(Byte.class, selectors.get(1).getJavaClass());
		}

		@Test
		void updatesIncludeClassNameFilterWhenSelectClassesAnnotationIsPresent() {

			@SelectClasses({ Short.class, Byte.class })
			class TestCase {
			}

			var request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			var filters = request.getFiltersByType(ClassNameFilter.class);
			assertThat(filters).hasSize(1);

			var filter = filters.get(0);

			// Excluded by default
			assertExcludes(filter, "example.MyClass");
			assertExcludes(filter, "example.MyTestClass");
			assertExcludes(filter, "example.Short");
			assertExcludes(filter, "example.Byte");

			// Included due to ClassNameFilter.STANDARD_INCLUDE_PATTERN
			assertIncludes(filter, "TestClass");
			assertIncludes(filter, "example.TestClass");
			assertIncludes(filter, "example.MyTests");
			assertIncludes(filter, "example.MyTest");

			// Included due to @SelectClasses({ Short.class, Byte.class })
			assertIncludes(filter, Short.class.getName());
			assertIncludes(filter, Byte.class.getName());
		}

		@Test
		void requestsPackageSelectorsWhenPackagesAnnotationIsPresent() {

			@SelectPackages({ "foo", "bar" })
			class TestCase {
			}

			var request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			var selectors = request.getSelectorsByType(PackageSelector.class);
			assertThat(selectors).hasSize(2);
			assertEquals("foo", selectors.get(0).getPackageName());
			assertEquals("bar", selectors.get(1).getPackageName());
		}

		@Test
		void addsPackageFiltersToRequestWhenIncludePackageAnnotationIsPresent() {

			@IncludePackages({ "includedpackage1", "includedpackage2" })
			class TestCase {
			}

			var request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			var filters = request.getFiltersByType(PackageNameFilter.class);
			assertThat(filters).hasSize(1);

			var filter = filters.get(0);
			assertIncludes(filter, "includedpackage1.TestClass");
			assertIncludes(filter, "includedpackage2.TestClass");
			assertExcludes(filter, "excludedpackage1.TestClass");
		}

		@Test
		void addsPackageFiltersToRequestWhenExcludePackageAnnotationIsPresent() {

			@ExcludePackages({ "excludedpackage1", "excludedpackage2" })
			class TestCase {
			}

			var request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			var filters = request.getFiltersByType(PackageNameFilter.class);
			assertThat(filters).hasSize(1);

			var filter = filters.get(0);
			assertIncludes(filter, "includedpackage1.TestClass");
			assertExcludes(filter, "excludedpackage1.TestClass");
			assertExcludes(filter, "excludedpackage2.TestClass");
		}

		@Test
		void addsTagFilterToRequestWhenIncludeTagsAnnotationIsPresent() {

			@IncludeTags({ "foo", "bar" })
			class TestCase {
			}

			var request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			var filters = request.getPostDiscoveryFilters();
			assertThat(filters).hasSize(1);

			var filter = filters.get(0);
			assertIncludes(filter, testDescriptorWithTags("foo"));
			assertIncludes(filter, testDescriptorWithTags("bar"));
			assertExcludes(filter, testDescriptorWithTags("baz"));
		}

		@Test
		void addsTagFilterToRequestWhenExcludeTagsAnnotationIsPresent() {

			@ExcludeTags({ "foo", "bar" })
			class TestCase {
			}

			var request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			var filters = request.getPostDiscoveryFilters();
			assertThat(filters).hasSize(1);

			var filter = filters.get(0);
			assertExcludes(filter, testDescriptorWithTags("foo"));
			assertExcludes(filter, testDescriptorWithTags("bar"));
			assertIncludes(filter, testDescriptorWithTags("baz"));
		}

		@Test
		void includeTagsAcceptsTagExpressions() {

			@IncludeTags("foo & !bar")
			class TestCase {
			}

			var request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			var filters = request.getPostDiscoveryFilters();
			assertThat(filters).hasSize(1);

			var filter = filters.get(0);
			assertIncludes(filter, testDescriptorWithTags("foo"));
			assertIncludes(filter, testDescriptorWithTags("foo", "any_other_tag"));
			assertExcludes(filter, testDescriptorWithTags("foo", "bar"));
			assertExcludes(filter, testDescriptorWithTags("bar"));
			assertExcludes(filter, testDescriptorWithTags("bar", "any_other_tag"));
		}

		@Test
		void excludeTagsAcceptsTagExpressions() {

			@ExcludeTags("foo & !bar")
			class TestCase {
			}

			var request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			var filters = request.getPostDiscoveryFilters();
			assertThat(filters).hasSize(1);

			var filter = filters.get(0);
			assertExcludes(filter, testDescriptorWithTags("foo"));
			assertExcludes(filter, testDescriptorWithTags("foo", "any_other_tag"));
			assertIncludes(filter, testDescriptorWithTags("foo", "bar"));
			assertIncludes(filter, testDescriptorWithTags("bar"));
			assertIncludes(filter, testDescriptorWithTags("bar", "any_other_tag"));
		}

		@Test
		void addsEngineFiltersToRequestWhenIncludeEnginesOrExcludeEnginesAnnotationsArePresent() {

			@IncludeEngines({ "foo", "bar", "baz" })
			@ExcludeEngines({ "bar", "quux" })
			class TestCase {
			}

			TestEngine fooEngine = new TestEngineStub("foo");
			TestEngine barEngine = new TestEngineStub("bar");
			TestEngine bazEngine = new TestEngineStub("baz");
			TestEngine quuxEngine = new TestEngineStub("quux");

			var request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			var filters = request.getEngineFilters();
			assertThat(filters).hasSize(2);

			var includeFilter = filters.get(1);
			assertIncludes(includeFilter, fooEngine);
			assertIncludes(includeFilter, barEngine);
			assertIncludes(includeFilter, bazEngine);
			assertExcludes(includeFilter, quuxEngine);

			var excludeFilter = filters.get(0);
			assertIncludes(excludeFilter, fooEngine);
			assertExcludes(excludeFilter, barEngine);
			assertIncludes(excludeFilter, bazEngine);
			assertExcludes(excludeFilter, quuxEngine);
		}

		@Test
		void addsDefaultClassNameFilterToRequestWhenFilterClassNameAnnotationIsNotPresentOnTestSuite() {

			@SelectPackages("foo")
			class TestCase {
			}

			var request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			var filters = request.getFiltersByType(ClassNameFilter.class);
			assertThat(getOnlyElement(filters).toString()).contains(STANDARD_INCLUDE_PATTERN);
		}

		@Test
		void addsDefaultClassNameFilterToRequestWhenFilterClassNameAnnotationIsNotPresentOnTestClass() {

			class TestCase {
			}

			var request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			var filters = request.getFiltersByType(ClassNameFilter.class);
			assertThat(filters).isEmpty();
		}

		@Test
		void addsSingleExplicitClassNameFilterToRequestWhenIncludeClassNamePatternsAnnotationIsPresent() {

			@IncludeClassNamePatterns(".*Foo")
			class TestCase {
			}

			var request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			var filters = request.getFiltersByType(ClassNameFilter.class);
			assertThat(getOnlyElement(filters).toString()).contains(".*Foo");
		}

		@Test
		void addsSingleClassNameFilterToRequestWhenExcludeClassNamePatternsAnnotationIsPresent() {

			@ExcludeClassNamePatterns(".*Foo")
			class TestCase {
			}

			var request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			var filters = request.getFiltersByType(ClassNameFilter.class);
			assertThat(getOnlyElement(filters).toString()).contains(".*Foo");
		}

		@Test
		void addsMultipleExplicitClassNameFilterToRequestWhenIncludeClassNamePatternsAnnotationIsPresent() {

			@IncludeClassNamePatterns({ ".*Foo", "Bar.*" })
			class TestCase {
			}

			var request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			var filters = request.getFiltersByType(ClassNameFilter.class);
			assertThat(getOnlyElement(filters).toString()).contains(".*Foo", "Bar.*");
		}

		@Test
		void addsMultipleClassNameFilterToRequestWhenExcludeClassNamePatternsAnnotationIsPresent() {

			@ExcludeClassNamePatterns({ ".*Foo", "Bar.*" })
			class TestCase {
			}

			var request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			var filters = request.getFiltersByType(ClassNameFilter.class);
			assertThat(getOnlyElement(filters).toString()).contains(".*Foo", "Bar.*");
		}

		@Test
		void usesStandardIncludePatternWhenIncludeClassNamePatternsAnnotationIsPresentWithoutArguments() {

			@IncludeClassNamePatterns
			class TestCase {
			}

			var request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			var filters = request.getFiltersByType(ClassNameFilter.class);
			assertThat(getOnlyElement(filters).toString()).contains(STANDARD_INCLUDE_PATTERN);
		}

		@Test
		void doesNotAddClassNameFilterWhenIncludeClassNamePatternsAnnotationIsPresentWithEmptyArguments() {

			@IncludeClassNamePatterns({})
			class TestCase {
			}

			var request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			var filters = request.getFiltersByType(ClassNameFilter.class);
			assertThat(filters).isEmpty();
		}

		@Test
		void doesNotAddClassNameFilterWhenExcludeClassNamePatternsAnnotationIsPresentWithEmptyArguments() {

			@ExcludeClassNamePatterns({})
			class TestCase {
			}

			var request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			var filters = request.getFiltersByType(ClassNameFilter.class);
			assertThat(filters).isEmpty();
		}

		@Test
		void trimsArgumentsOfIncludeClassNamePatternsAnnotation() {

			@IncludeClassNamePatterns({ " foo", "bar " })
			class TestCase {
			}

			var request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			var filters = request.getFiltersByType(ClassNameFilter.class);
			assertThat(getOnlyElement(filters).toString()).contains("'foo'", "'bar'");
		}

		@Test
		void trimsArgumentsOfExcludeClassNamePatternsAnnotation() {

			@ExcludeClassNamePatterns({ " foo", "bar " })
			class TestCase {
			}

			var request = instantiateRunnerAndCaptureGeneratedRequest(TestCase.class);

			var filters = request.getFiltersByType(ClassNameFilter.class);
			assertThat(getOnlyElement(filters).toString()).contains("'foo'", "'bar'");
		}

		@Test
		void convertsTestIdentifiersIntoDescriptions() {

			TestDescriptor container1 = new TestDescriptorStub(UniqueId.root("root", "container1"), "container1");
			container1.addChild(new TestDescriptorStub(UniqueId.root("root", "test1"), "test1"));
			TestDescriptor container2 = new TestDescriptorStub(UniqueId.root("root", "container2"), "container2");
			container2.addChild(new TestDescriptorStub(UniqueId.root("root", "test2a"), "test2a"));
			container2.addChild(new TestDescriptorStub(UniqueId.root("root", "test2b"), "test2b"));
			var testPlan = TestPlan.from(List.of(container1, container2), mock());

			var launcher = mock(Launcher.class);
			when(launcher.discover(any())).thenReturn(testPlan);

			var runner = new JUnitPlatform(TestClass.class, launcher);

			var runnerDescription = runner.getDescription();
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

		private static <T> void assertIncludes(Filter<T> filter, T included) {
			assertThat(filter.apply(included).included()).isTrue();
		}

		private static <T> void assertExcludes(Filter<T> filter, T excluded) {
			assertThat(filter.apply(excluded).excluded()).isTrue();
		}

	}

	@Nested
	class Filtering {

		private final ConfigurationParameters configParams = mock();

		@Test
		void appliesFilter() throws Exception {

			TestDescriptor originalParent1 = new TestDescriptorStub(UniqueId.root("root", "parent1"), "parent1");
			originalParent1.addChild(new TestDescriptorStub(UniqueId.root("root", "leaf1"), "leaf1"));
			TestDescriptor originalParent2 = new TestDescriptorStub(UniqueId.root("root", "parent2"), "parent2");
			originalParent2.addChild(new TestDescriptorStub(UniqueId.root("root", "leaf2a"), "leaf2a"));
			originalParent2.addChild(new TestDescriptorStub(UniqueId.root("root", "leaf2b"), "leaf2b"));
			var fullTestPlan = TestPlan.from(List.of(originalParent1, originalParent2), configParams);

			TestDescriptor filteredParent = new TestDescriptorStub(UniqueId.root("root", "parent2"), "parent2");
			filteredParent.addChild(new TestDescriptorStub(UniqueId.root("root", "leaf2b"), "leaf2b"));
			var filteredTestPlan = TestPlan.from(Set.of(filteredParent), configParams);

			var launcher = mock(Launcher.class);
			var captor = ArgumentCaptor.forClass(LauncherDiscoveryRequest.class);
			when(launcher.discover(captor.capture())).thenReturn(fullTestPlan).thenReturn(filteredTestPlan);

			var runner = new JUnitPlatform(TestClass.class, launcher);
			runner.filter(matchMethodDescription(testDescription("[root:leaf2b]")));

			var lastDiscoveryRequest = captor.getValue();
			var uniqueIdSelectors = lastDiscoveryRequest.getSelectorsByType(UniqueIdSelector.class);
			assertEquals("[root:leaf2b]", getOnlyElement(uniqueIdSelectors).getUniqueId().toString());

			var parentDescription = getOnlyElement(runner.getDescription().getChildren());
			assertEquals(suiteDescription("[root:parent2]"), parentDescription);

			var testDescription = getOnlyElement(parentDescription.getChildren());
			assertEquals(testDescription("[root:leaf2b]"), testDescription);
		}

		@Test
		void throwsNoTestsRemainExceptionWhenNoTestIdentifierMatchesFilter() {
			var testPlan = TestPlan.from(Set.of(new TestDescriptorStub(UniqueId.root("root", "test"), "test")),
				configParams);

			var launcher = mock(Launcher.class);
			when(launcher.discover(any())).thenReturn(testPlan);

			var runner = new JUnitPlatform(TestClass.class, launcher);

			assertThrows(NoTestsRemainException.class,
				() -> runner.filter(matchMethodDescription(suiteDescription("[root:doesNotExist]"))));
		}

	}

	@Nested
	class Execution {

		@Test
		void notifiesRunListenerOfTestExecution() throws Exception {
			var engine = new DemoHierarchicalTestEngine("dummy");
			engine.addTest("failingTest", () -> fail("expected to fail"));
			engine.addTest("succeedingTest", () -> {
			});
			engine.addTest("abortedTest", () -> assumeFalse(true));
			engine.addTest("skippedTest", () -> fail("never called")).markSkipped("should be skipped");

			var runListener = mock(RunListener.class);

			var notifier = new RunNotifier();
			notifier.addListener(runListener);
			new JUnitPlatform(TestClass.class, createLauncher(engine)).run(notifier);

			var inOrder = inOrder(runListener);

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
			var runListener = mock(RunListener.class);
			var notifier = new RunNotifier();
			// notifier.addListener(new LoggingRunListener());
			notifier.addListener(runListener);
			new JUnitPlatform(TestClass.class, createLauncher(new DynamicTestEngine())).run(notifier);

			var inOrder = inOrder(runListener);

			inOrder.verify(runListener).testStarted(testDescription("[engine:dynamic]/[container:1]/[test:1]"));
			inOrder.verify(runListener).testFinished(testDescription("[engine:dynamic]/[container:1]/[test:1]"));

			inOrder.verify(runListener).testStarted(testDescription("[engine:dynamic]/[container:1]/[test:2]"));
			inOrder.verify(runListener).testFinished(testDescription("[engine:dynamic]/[container:1]/[test:2]"));

			inOrder.verify(runListener).testStarted(testDescription("[engine:dynamic]/[container:1]/[test:3]"));
			inOrder.verify(runListener).testFinished(testDescription("[engine:dynamic]/[container:1]/[test:3]"));

			inOrder.verify(runListener).testStarted(
				testDescription("[engine:dynamic]/[container:1]/[test:3]/[test:3a]"));
			inOrder.verify(runListener).testFinished(
				testDescription("[engine:dynamic]/[container:1]/[test:3]/[test:3a]"));

			inOrder.verifyNoMoreInteractions();
		}

		@Test
		void reportsIgnoredEventsForLeavesWhenContainerIsSkipped() throws Exception {
			var uniqueEngineId = UniqueId.forEngine("engine");
			TestDescriptor engineDescriptor = new EngineDescriptor(uniqueEngineId, "engine");
			TestDescriptor container = new TestDescriptorStub(UniqueId.root("root", "container"), "container");
			container.addChild(new TestDescriptorStub(UniqueId.root("root", "leaf"), "leaf"));
			engineDescriptor.addChild(container);

			var engine = mock(TestEngine.class);
			when(engine.getId()).thenReturn("engine");
			when(engine.discover(any(), eq(uniqueEngineId))).thenReturn(engineDescriptor);
			doAnswer(invocation -> {
				ExecutionRequest request = invocation.getArgument(0);
				var listener = request.getEngineExecutionListener();
				listener.executionStarted(engineDescriptor);
				listener.executionSkipped(container, "deliberately skipped container");
				listener.executionFinished(engineDescriptor, successful());
				return null;
			}).when(engine).execute(any());

			var runListener = mock(RunListener.class);

			var notifier = new RunNotifier();
			notifier.addListener(runListener);
			new JUnitPlatform(TestClass.class, createLauncher(engine)).run(notifier);

			verify(runListener).testIgnored(testDescription("[root:leaf]"));
			verifyNoMoreInteractions(runListener);
		}

	}

	@Nested
	class Descriptions {

		@Test
		@DisplayName("Suite with default display name")
		void descriptionForTestSuiteWithDefaultDisplayName() {
			Class<?> testClass = TestSuiteWithDefaultDisplayName.class;
			var platformRunner = new JUnitPlatform(testClass,
				createLauncher(new DemoHierarchicalTestEngine("suite names")));

			assertEquals(testClass.getName(), platformRunner.getDescription().getDisplayName());
		}

		@Test
		@DisplayName("Suite with @SuiteDisplayName")
		void descriptionForTestSuiteWithCustomDisplayName() {
			var platformRunner = new JUnitPlatform(TestSuiteWithCustomDisplayName.class,
				createLauncher(new DemoHierarchicalTestEngine("suite names")));

			assertEquals("Sweeeeeeet Name!", platformRunner.getDescription().getDisplayName());
		}

		@Test
		@DisplayName("Suite with @SuiteDisplayName and @UseTechnicalNames")
		void descriptionForTestSuiteWithCustomDisplayNameAndTechnicalNames() {
			Class<?> testClass = TestSuiteWithCustomDisplayNameAndTechnicalNames.class;
			var platformRunner = new JUnitPlatform(testClass,
				createLauncher(new DemoHierarchicalTestEngine("suite names")));

			assertEquals(testClass.getName(), platformRunner.getDescription().getDisplayName());
		}

		@Test
		void descriptionForJavaMethodAndClassSources() throws Exception {
			var engine = new DemoHierarchicalTestEngine("dummy");
			var failingTest = getClass().getDeclaredMethod("failingTest");
			var containerDescriptor = engine.addContainer("uniqueContainerName", "containerDisplayName",
				ClassSource.from(getClass()));
			containerDescriptor.addChild(
				new DemoHierarchicalTestDescriptor(containerDescriptor.getUniqueId().append("test", "failingTest"),
					"testDisplayName", MethodSource.from(failingTest), (c, t) -> {
					}));

			var platformRunner = new JUnitPlatform(TestClass.class, createLauncher(engine));

			List<Description> children = platformRunner.getDescription().getChildren();
			assertEquals(1, children.size());
			var engineDescription = children.get(0);
			assertEquals("dummy", engineDescription.getDisplayName());

			var containerDescription = getOnlyElement(engineDescription.getChildren());
			var testDescription = getOnlyElement(containerDescription.getChildren());

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
			var engine = new DemoHierarchicalTestEngine("dummy");
			var failingTest = getClass().getDeclaredMethod("failingTest");
			var containerDescriptor = engine.addContainer("uniqueContainerName", "containerDisplayName",
				ClassSource.from(getClass()));
			containerDescriptor.addChild(
				new DemoHierarchicalTestDescriptor(containerDescriptor.getUniqueId().append("test", "failingTest"),
					"testDisplayName", MethodSource.from(failingTest), (c, t) -> {
					}));

			var platformRunner = new JUnitPlatform(TestClassWithTechnicalNames.class, createLauncher(engine));

			List<Description> children = platformRunner.getDescription().getChildren();
			assertEquals(1, children.size());
			var engineDescription = children.get(0);
			assertEquals("dummy", engineDescription.getDisplayName());

			var containerDescription = getOnlyElement(engineDescription.getChildren());
			var testDescription = getOnlyElement(containerDescription.getChildren());

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
		return createSuiteDescription(uniqueId, UniqueId.parse(uniqueId));
	}

	private static Description testDescription(String uniqueId) {
		return createTestDescription(uniqueId, uniqueId, UniqueId.parse(uniqueId));
	}

	private TestDescriptor testDescriptorWithTags(String... tag) {
		var testDescriptor = mock(TestDescriptor.class);
		var tags = Arrays.stream(tag).map(TestTag::create).collect(toSet());
		when(testDescriptor.getTags()).thenReturn(tags);
		return testDescriptor;
	}

	private LauncherDiscoveryRequest instantiateRunnerAndCaptureGeneratedRequest(Class<?> testClass) {
		var launcher = mock(Launcher.class);
		var captor = ArgumentCaptor.forClass(LauncherDiscoveryRequest.class);
		when(launcher.discover(captor.capture())).thenReturn(TestPlan.from(Set.of(), mock()));

		new JUnitPlatform(testClass, launcher);

		return captor.getValue();
	}

	private static class TestClass {
	}

	@UseTechnicalNames
	private static class TestClassWithTechnicalNames {
	}

	private static class TestSuiteWithDefaultDisplayName {
	}

	@SuiteDisplayName("Sweeeeeeet Name!")
	private static class TestSuiteWithCustomDisplayName {
	}

	@SuiteDisplayName("Sweeeeeeet Name!")
	@UseTechnicalNames
	private static class TestSuiteWithCustomDisplayNameAndTechnicalNames {
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
			var engineExecutionListener = request.getEngineExecutionListener();
			var root = request.getRootTestDescriptor();

			TestDescriptor container = new DemoContainerTestDescriptor(root.getUniqueId().append("container", "1"),
				"container #1");
			root.addChild(container);

			engineExecutionListener.dynamicTestRegistered(container);
			engineExecutionListener.executionStarted(container);

			var containerUid = container.getUniqueId();

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

			TestDescriptor dynamicTest3 = new DemoContainerAndTestTestDescriptor(containerUid.append("test", "3"),
				"dynamic test #3");
			container.addChild(dynamicTest3);
			engineExecutionListener.dynamicTestRegistered(dynamicTest3);
			engineExecutionListener.executionStarted(dynamicTest3);
			engineExecutionListener.executionFinished(dynamicTest3, TestExecutionResult.successful());

			TestDescriptor dynamicTest3a = new DemoTestTestDescriptor(dynamicTest3.getUniqueId().append("test", "3a"),
				"dynamic test #3a");
			dynamicTest3.addChild(dynamicTest3a);
			engineExecutionListener.dynamicTestRegistered(dynamicTest3a);
			engineExecutionListener.executionStarted(dynamicTest3a);
			engineExecutionListener.executionFinished(dynamicTest3a, TestExecutionResult.successful());

			engineExecutionListener.executionFinished(container, TestExecutionResult.successful());
		}

	}

	private static class DemoContainerTestDescriptor extends AbstractTestDescriptor {

		DemoContainerTestDescriptor(UniqueId uniqueId, String displayName) {
			super(uniqueId, displayName);
		}

		@Override
		public Type getType() {
			return Type.CONTAINER;
		}
	}

	private static class DemoTestTestDescriptor extends AbstractTestDescriptor {

		DemoTestTestDescriptor(UniqueId uniqueId, String displayName) {
			super(uniqueId, displayName);
		}

		@Override
		public Type getType() {
			return Type.TEST;
		}
	}

	private static class DemoContainerAndTestTestDescriptor extends AbstractTestDescriptor {

		DemoContainerAndTestTestDescriptor(UniqueId uniqueId, String displayName) {
			super(uniqueId, displayName);
		}

		@Override
		public Type getType() {
			return Type.CONTAINER_AND_TEST;
		}
	}

}
