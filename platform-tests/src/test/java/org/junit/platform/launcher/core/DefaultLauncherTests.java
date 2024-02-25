/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.platform.engine.TestExecutionResult.successful;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.launcher.LauncherConstants.DRY_RUN_PROPERTY_NAME;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.DEFAULT_DISCOVERY_LISTENER_CONFIGURATION_PROPERTY_NAME;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;
import static org.junit.platform.launcher.core.LauncherFactoryForTestingPurposesOnly.createLauncher;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.fixtures.TrackLogRecords;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.logging.LogRecordListener;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.hierarchical.DemoHierarchicalTestDescriptor;
import org.junit.platform.engine.support.hierarchical.DemoHierarchicalTestEngine;
import org.junit.platform.fakes.TestDescriptorStub;
import org.junit.platform.fakes.TestEngineSpy;
import org.junit.platform.fakes.TestEngineStub;
import org.junit.platform.launcher.EngineDiscoveryResult;
import org.junit.platform.launcher.LauncherDiscoveryListener;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.platform.launcher.PostDiscoveryFilterStub;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.mockito.ArgumentCaptor;

/**
 * @since 1.0
 */
class DefaultLauncherTests {

	private static final String FOO = DefaultLauncherTests.class.getSimpleName() + ".foo";
	private static final String BAR = DefaultLauncherTests.class.getSimpleName() + ".bar";

	private static final Runnable noOp = () -> {
	};

	@Test
	void constructLauncherWithoutAnyEngines() {
		var launcher = createLauncher();

		Throwable exception = assertThrows(PreconditionViolationException.class,
			() -> launcher.discover(request().build()));

		assertThat(exception).hasMessageContaining("Cannot create Launcher without at least one TestEngine");
	}

	@Test
	void constructLauncherWithMultipleTestEnginesWithDuplicateIds() {
		var launcher = createLauncher(new DemoHierarchicalTestEngine("dummy id"),
			new DemoHierarchicalTestEngine("dummy id"));

		var exception = assertThrows(JUnitException.class, () -> launcher.discover(request().build()));

		assertThat(exception).hasMessageContaining("multiple engines with the same ID");
	}

	@Test
	void discoverEmptyTestPlanWithEngineWithoutAnyTests() {
		var launcher = createLauncher(new DemoHierarchicalTestEngine());

		var testPlan = launcher.discover(request().build());

		assertThat(testPlan.getRoots()).hasSize(1);
	}

	@Test
	void discoverTestPlanForEngineThatReturnsNullForItsRootDescriptor() {
		TestEngine engine = new TestEngineStub("some-engine-id") {

			@Override
			public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
				return null;
			}
		};

		var discoveryListener = mock(LauncherDiscoveryListener.class);
		var testPlan = createLauncher(engine).discover(request() //
				.listeners(discoveryListener) //
				.configurationParameter(DEFAULT_DISCOVERY_LISTENER_CONFIGURATION_PROPERTY_NAME, "logging") //
				.build());
		assertThat(testPlan.getRoots()).hasSize(1);
		assertDiscoveryFailed(engine, discoveryListener);
	}

	@ParameterizedTest
	@ValueSource(classes = { Error.class, RuntimeException.class })
	void discoverErrorTestDescriptorForEngineThatThrowsInDiscoveryPhase(Class<? extends Throwable> throwableClass) {
		TestEngine engine = new TestEngineStub("my-engine-id") {

			@Override
			public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
				try {
					var constructor = throwableClass.getDeclaredConstructor(String.class);
					throw ExceptionUtils.throwAsUncheckedException(constructor.newInstance("ignored"));
				}
				catch (Exception ignored) {
					return null;
				}
			}
		};

		var launcher = createLauncher(engine);
		var discoveryListener = mock(LauncherDiscoveryListener.class);
		var request = request() //
				.listeners(discoveryListener) //
				.configurationParameter(DEFAULT_DISCOVERY_LISTENER_CONFIGURATION_PROPERTY_NAME, "logging") //
				.build();
		var testPlan = launcher.discover(request);

		assertThat(testPlan.getRoots()).hasSize(1);
		var engineIdentifier = getOnlyElement(testPlan.getRoots());
		assertThat(getOnlyElement(testPlan.getRoots()).getDisplayName()).isEqualTo("my-engine-id");
		verify(discoveryListener).launcherDiscoveryStarted(request);
		verify(discoveryListener).launcherDiscoveryFinished(request);
		assertDiscoveryFailed(engine, discoveryListener);

		var listener = mock(TestExecutionListener.class);
		launcher.execute(testPlan, listener);

		var testExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		verify(listener).executionStarted(engineIdentifier);
		verify(listener).executionFinished(eq(engineIdentifier), testExecutionResult.capture());
		assertThat(testExecutionResult.getValue().getThrowable()).isPresent();
		assertThat(testExecutionResult.getValue().getThrowable().get()) //
				.hasMessage("TestEngine with ID 'my-engine-id' failed to discover tests");
	}

	private void assertDiscoveryFailed(TestEngine testEngine, LauncherDiscoveryListener discoveryListener) {
		var engineId = testEngine.getId();
		var failureCaptor = ArgumentCaptor.forClass(EngineDiscoveryResult.class);
		verify(discoveryListener).engineDiscoveryFinished(eq(UniqueId.forEngine(engineId)), failureCaptor.capture());
		var result = failureCaptor.getValue();
		assertThat(result.getStatus()).isEqualTo(EngineDiscoveryResult.Status.FAILED);
		assertThat(result.getThrowable()).isPresent();
		assertThat(result.getThrowable().get()).hasMessage(
			"TestEngine with ID '" + engineId + "' failed to discover tests");
	}

	@Test
	void reportsEngineExecutionFailuresWithoutPriorEvents() {
		var rootCause = new RuntimeException("something went horribly wrong");
		var engine = new TestEngineStub() {
			@Override
			public void execute(ExecutionRequest request) {
				throw rootCause;
			}
		};

		var listener = mock(TestExecutionListener.class);
		createLauncher(engine).execute(request().build(), listener);

		var testExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		verify(listener).executionStarted(any());
		verify(listener).executionFinished(any(), testExecutionResult.capture());
		assertThat(testExecutionResult.getValue().getThrowable()).isPresent();
		assertThat(testExecutionResult.getValue().getThrowable().get()) //
				.hasMessage("TestEngine with ID 'TestEngineStub' failed to execute tests") //
				.hasCauseReference(rootCause);
	}

	@Test
	void reportsEngineExecutionFailuresForSkippedEngine() {
		var rootCause = new RuntimeException("something went horribly wrong");
		var engine = new TestEngineStub() {
			@Override
			public void execute(ExecutionRequest request) {
				var engineDescriptor = request.getRootTestDescriptor();
				request.getEngineExecutionListener().executionSkipped(engineDescriptor, "not today");
				throw rootCause;
			}
		};

		var listener = mock(TestExecutionListener.class);
		createLauncher(engine).execute(request().build(), listener);

		var testExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		verify(listener).executionStarted(any());
		verify(listener).executionFinished(any(), testExecutionResult.capture());
		assertThat(testExecutionResult.getValue().getThrowable()).isPresent();
		assertThat(testExecutionResult.getValue().getThrowable().get()) //
				.hasMessage("TestEngine with ID 'TestEngineStub' failed to execute tests") //
				.hasCauseReference(rootCause);
	}

	@Test
	void reportsEngineExecutionFailuresForStartedEngine() {
		var rootCause = new RuntimeException("something went horribly wrong");
		var engine = new TestEngineStub() {
			@Override
			public void execute(ExecutionRequest request) {
				var engineDescriptor = request.getRootTestDescriptor();
				request.getEngineExecutionListener().executionStarted(engineDescriptor);
				throw rootCause;
			}
		};

		var listener = mock(TestExecutionListener.class);
		createLauncher(engine).execute(request().build(), listener);

		var testExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		verify(listener).executionStarted(any());
		verify(listener).executionFinished(any(), testExecutionResult.capture());
		assertThat(testExecutionResult.getValue().getThrowable()).isPresent();
		assertThat(testExecutionResult.getValue().getThrowable().get()) //
				.hasMessage("TestEngine with ID 'TestEngineStub' failed to execute tests") //
				.hasCauseReference(rootCause);
	}

	@Test
	void reportsEngineExecutionFailuresForSuccessfullyFinishedEngine() {
		var rootCause = new RuntimeException("something went horribly wrong");
		var engine = new TestEngineStub() {
			@Override
			public void execute(ExecutionRequest request) {
				var engineDescriptor = request.getRootTestDescriptor();
				request.getEngineExecutionListener().executionStarted(engineDescriptor);
				request.getEngineExecutionListener().executionFinished(engineDescriptor,
					TestExecutionResult.successful());
				throw rootCause;
			}
		};

		var listener = mock(TestExecutionListener.class);
		createLauncher(engine).execute(request().build(), listener);

		var testExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		verify(listener).executionStarted(any());
		verify(listener).executionFinished(any(), testExecutionResult.capture());
		assertThat(testExecutionResult.getValue().getThrowable()).isPresent();
		assertThat(testExecutionResult.getValue().getThrowable().get()) //
				.hasMessage("TestEngine with ID 'TestEngineStub' failed to execute tests") //
				.hasCauseReference(rootCause);
	}

	@Test
	void reportsEngineExecutionFailuresForFailedFinishedEngine() {
		var rootCause = new RuntimeException("something went horribly wrong");
		var originalFailure = new RuntimeException("suppressed");
		var engine = new TestEngineStub() {
			@Override
			public void execute(ExecutionRequest request) {
				var engineDescriptor = request.getRootTestDescriptor();
				var listener = request.getEngineExecutionListener();
				listener.executionStarted(engineDescriptor);
				listener.executionFinished(engineDescriptor, TestExecutionResult.failed(originalFailure));
				throw rootCause;
			}
		};

		var listener = mock(TestExecutionListener.class);
		createLauncher(engine).execute(request().build(), listener);

		var testExecutionResult = ArgumentCaptor.forClass(TestExecutionResult.class);
		verify(listener).executionStarted(any());
		verify(listener).executionFinished(any(), testExecutionResult.capture());
		assertThat(testExecutionResult.getValue().getThrowable()).isPresent();
		assertThat(testExecutionResult.getValue().getThrowable().get()) //
				.hasMessage("TestEngine with ID 'TestEngineStub' failed to execute tests") //
				.hasCauseReference(rootCause) //
				.hasSuppressedException(originalFailure);
	}

	@Test
	void reportsSkippedEngines() {
		var engine = new TestEngineStub() {
			@Override
			public void execute(ExecutionRequest request) {
				var engineDescriptor = request.getRootTestDescriptor();
				request.getEngineExecutionListener().executionSkipped(engineDescriptor, "not today");
			}
		};

		var listener = mock(TestExecutionListener.class);
		createLauncher(engine).execute(request().build(), listener);

		verify(listener).executionSkipped(any(TestIdentifier.class), eq("not today"));
		verify(listener, times(0)).executionStarted(any());
		verify(listener, times(0)).executionFinished(any(), any());
	}

	@Test
	void reportsFinishedEngines() {
		var engine = new TestEngineStub() {
			@Override
			public void execute(ExecutionRequest request) {
				var engineDescriptor = request.getRootTestDescriptor();
				var listener = request.getEngineExecutionListener();
				listener.executionStarted(engineDescriptor);
				listener.executionFinished(engineDescriptor, TestExecutionResult.successful());
			}
		};

		var listener = mock(TestExecutionListener.class);
		createLauncher(engine).execute(request().build(), listener);

		verify(listener).executionStarted(any());
		verify(listener).executionFinished(any(), eq(TestExecutionResult.successful()));
	}

	@Test
	void discoverTestPlanForSingleEngine() {
		var engine = new DemoHierarchicalTestEngine("myEngine");
		engine.addTest("test1", noOp);
		engine.addTest("test2", noOp);

		var launcher = createLauncher(engine);

		var testPlan = launcher.discover(request().selectors(selectPackage("any")).build());

		assertThat(testPlan.getRoots()).hasSize(1);
		var rootIdentifier = testPlan.getRoots().iterator().next();
		assertThat(testPlan.getChildren(rootIdentifier.getUniqueIdObject())).hasSize(2);
		assertThat(testPlan.getChildren(UniqueId.parse("[engine:myEngine]"))).hasSize(2);
	}

	@Test
	void discoverTestPlanForMultipleEngines() {
		var firstEngine = new DemoHierarchicalTestEngine("engine1");
		TestDescriptor test1 = firstEngine.addTest("test1", noOp);
		var secondEngine = new DemoHierarchicalTestEngine("engine2");
		TestDescriptor test2 = secondEngine.addTest("test2", noOp);

		var launcher = createLauncher(firstEngine, secondEngine);

		var testPlan = launcher.discover(
			request().selectors(selectUniqueId(test1.getUniqueId()), selectUniqueId(test2.getUniqueId())).build());

		assertThat(testPlan.getRoots()).hasSize(2);
		assertThat(testPlan.getChildren(UniqueId.forEngine("engine1"))).hasSize(1);
		assertThat(testPlan.getChildren(UniqueId.forEngine("engine2"))).hasSize(1);
	}

	@Test
	void launcherAppliesPostDiscoveryFilters() {
		var engine = new DemoHierarchicalTestEngine("myEngine");
		var test1 = engine.addTest("test1", noOp);
		engine.addTest("test2", noOp);

		var launcher = createLauncher(engine);

		PostDiscoveryFilter includeWithUniqueIdContainsTest = new PostDiscoveryFilterStub(
			descriptor -> FilterResult.includedIf(descriptor.getUniqueId().toString().contains("test")),
			() -> "filter1");
		PostDiscoveryFilter includeWithUniqueIdContains1 = new PostDiscoveryFilterStub(
			descriptor -> FilterResult.includedIf(descriptor.getUniqueId().toString().contains("1")), () -> "filter2");

		var testPlan = launcher.discover( //
			request() //
					.selectors(selectPackage("any")) //
					.filters(includeWithUniqueIdContainsTest, includeWithUniqueIdContains1) //
					.build());

		assertThat(testPlan.getChildren(UniqueId.forEngine("myEngine"))).hasSize(1);
		assertThat(testPlan.getTestIdentifier(test1.getUniqueId())).isNotNull();
	}

	@Test
	@SuppressWarnings("deprecation")
	void withoutConfigurationParameters_LauncherPassesEmptyConfigurationParametersIntoTheExecutionRequest() {
		var engine = new TestEngineSpy();

		var launcher = createLauncher(engine);
		launcher.execute(request().build());

		var configurationParameters = engine.requestForExecution.getConfigurationParameters();
		assertThat(configurationParameters.get("key")).isNotPresent();
		assertThat(configurationParameters.size()).isEqualTo(0);
	}

	@Test
	@SuppressWarnings("deprecation")
	void withConfigurationParameters_LauncherPassesPopulatedConfigurationParametersIntoTheExecutionRequest() {
		var engine = new TestEngineSpy();

		var launcher = createLauncher(engine);
		launcher.execute(request().configurationParameter("key", "value").build());

		var configurationParameters = engine.requestForExecution.getConfigurationParameters();
		assertThat(configurationParameters.size()).isEqualTo(1);
		assertThat(configurationParameters.get("key")).isPresent();
		assertThat(configurationParameters.get("key")).contains("value");
	}

	@Test
	@SuppressWarnings("deprecation")
	void withoutConfigurationParameters_LookupFallsBackToSystemProperty() {
		System.setProperty(FOO, BAR);

		try {
			var engine = new TestEngineSpy();

			var launcher = createLauncher(engine);
			launcher.execute(request().build());

			var configurationParameters = engine.requestForExecution.getConfigurationParameters();
			assertThat(configurationParameters.size()).isEqualTo(0);
			var optionalFoo = configurationParameters.get(FOO);
			assertTrue(optionalFoo.isPresent(), "foo should have been picked up via system property");
			assertEquals(BAR, optionalFoo.get(), "foo property");
		}
		finally {
			System.clearProperty(FOO);
		}
	}

	@Test
	void withAdditionalListener() {
		var engine = new TestEngineSpy();
		var listener = new SummaryGeneratingListener();

		var launcher = createLauncher(engine);
		launcher.execute(request().build(), listener);

		assertThat(listener.getSummary()).isNotNull();
		assertThat(listener.getSummary().getContainersFoundCount()).isEqualTo(1);
		assertThat(listener.getSummary().getTestsFoundCount()).isEqualTo(1);
	}

	@Test
	void prunesTestDescriptorsAfterApplyingPostDiscoveryFilters() {
		var engine = new TestEngineSpy() {

			@Override
			public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
				super.discover(discoveryRequest, uniqueId);
				var engineDescriptor = new TestDescriptorStub(uniqueId, uniqueId.toString());
				var containerDescriptor = new TestDescriptorStub(uniqueId.append("container", "a"), "container") {

					@Override
					public Type getType() {
						return Type.CONTAINER;
					}
				};
				containerDescriptor.addChild(
					new TestDescriptorStub(containerDescriptor.getUniqueId().append("test", "b"), "test"));
				engineDescriptor.addChild(containerDescriptor);
				return engineDescriptor;
			}
		};

		var launcher = createLauncher(engine);
		var testPlan = launcher.discover(request().filters(
			(PostDiscoveryFilter) testDescriptor -> FilterResult.includedIf(testDescriptor.isContainer())).build());

		assertThat(testPlan.getRoots()).hasSize(1);
		var engineIdentifier = getOnlyElement(testPlan.getRoots());
		assertThat(testPlan.getChildren(engineIdentifier)).isEmpty();
	}

	@Test
	void reportsDynamicTestDescriptorsCorrectly() {
		var engineId = UniqueId.forEngine("engine");
		var containerAndTestId = engineId.append("c&t", "c&t");
		var dynamicTestId = containerAndTestId.append("test", "test");

		var engine = new TestEngineSpy(engineId.getLastSegment().getValue()) {

			@Override
			public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
				super.discover(discoveryRequest, uniqueId);
				var engineDescriptor = new TestDescriptorStub(uniqueId, uniqueId.toString());
				engineDescriptor.addChild(new TestDescriptorStub(containerAndTestId, "c&t") {

					@Override
					public Type getType() {
						return Type.CONTAINER_AND_TEST;
					}
				});
				return engineDescriptor;
			}

			@Override
			public void execute(ExecutionRequest request) {
				super.execute(request);
				var listener = request.getEngineExecutionListener();

				listener.executionStarted(request.getRootTestDescriptor());
				var containerAndTest = getOnlyElement(request.getRootTestDescriptor().getChildren());
				listener.executionStarted(containerAndTest);

				var dynamicTest = new TestDescriptorStub(dynamicTestId, "test");
				dynamicTest.setParent(containerAndTest);
				listener.dynamicTestRegistered(dynamicTest);
				listener.executionStarted(dynamicTest);
				listener.executionFinished(dynamicTest, successful());

				listener.executionFinished(containerAndTest, successful());
				listener.executionFinished(request.getRootTestDescriptor(), successful());
			}
		};

		var launcher = createLauncher(engine);
		var listener = mock(TestExecutionListener.class);
		launcher.execute(request().build(), listener);

		var inOrder = inOrder(listener);
		var testPlanArgumentCaptor = ArgumentCaptor.forClass(TestPlan.class);
		inOrder.verify(listener).testPlanExecutionStarted(testPlanArgumentCaptor.capture());

		var testPlan = testPlanArgumentCaptor.getValue();
		var engineTestIdentifier = testPlan.getTestIdentifier(engineId);
		var containerAndTestIdentifier = testPlan.getTestIdentifier(containerAndTestId);
		var dynamicTestIdentifier = testPlan.getTestIdentifier(dynamicTestId);
		assertThat(engineTestIdentifier.getParentIdObject()).isEmpty();
		assertThat(containerAndTestIdentifier.getParentIdObject()).contains(engineId);
		assertThat(dynamicTestIdentifier.getParentIdObject()).contains(containerAndTestId);

		inOrder.verify(listener).executionStarted(engineTestIdentifier);
		inOrder.verify(listener).executionStarted(containerAndTestIdentifier);
		inOrder.verify(listener).dynamicTestRegistered(dynamicTestIdentifier);
		inOrder.verify(listener).executionStarted(dynamicTestIdentifier);
		inOrder.verify(listener).executionFinished(dynamicTestIdentifier, successful());
		inOrder.verify(listener).executionFinished(containerAndTestIdentifier, successful());
		inOrder.verify(listener).executionFinished(engineTestIdentifier, successful());
		inOrder.verify(listener).testPlanExecutionFinished(same(testPlan));
	}

	@Test
	void launcherCanExecuteTestPlanExactlyOnce() {
		var engine = mock(TestEngine.class);
		when(engine.getId()).thenReturn("some-engine");
		when(engine.discover(any(), any())).thenAnswer(invocation -> {
			UniqueId uniqueId = invocation.getArgument(1);
			return new EngineDescriptor(uniqueId, uniqueId.toString());
		});

		var launcher = createLauncher(engine);
		var testPlan = launcher.discover(request().build());
		verify(engine, times(1)).discover(any(), any());

		launcher.execute(testPlan);
		verify(engine, times(1)).execute(any());

		var e = assertThrows(PreconditionViolationException.class, () -> launcher.execute(testPlan));
		assertEquals(e.getMessage(), "TestPlan must only be executed once");
	}

	@Test
	@SuppressWarnings("deprecation")
	void testPlanThrowsExceptionWhenModified() {
		TestEngine engine = new TestEngineSpy();
		var launcher = createLauncher(engine);
		var testPlan = launcher.discover(request().build());
		var engineIdentifier = getOnlyElement(testPlan.getRoots());
		var engineUniqueId = engineIdentifier.getUniqueIdObject();
		assertThat(testPlan.getChildren(engineIdentifier)).hasSize(1);

		var addedIdentifier = TestIdentifier.from(
			new TestDescriptorStub(engineUniqueId.append("test", "test2"), "test2"));

		var exception = assertThrows(JUnitException.class, () -> testPlan.add(addedIdentifier));
		assertThat(exception).hasMessage("Unsupported attempt to modify the TestPlan was detected. "
				+ "Please contact your IDE/tool vendor and request a fix or downgrade to JUnit 5.7.x (see https://github.com/junit-team/junit5/issues/1732 for details).");
		assertThat(testPlan.getChildren(engineIdentifier)).hasSize(1).doesNotContain(addedIdentifier);
	}

	@Test
	void thirdPartyEngineUsingReservedEngineIdPrefixEmitsWarning(@TrackLogRecords LogRecordListener listener) {
		var id = "junit-using-reserved-prefix";
		var launcher = createLauncher(new TestEngineStub(id));
		launcher.discover(request().build());
		assertThat(listener.stream(EngineIdValidator.class, Level.WARNING).map(LogRecord::getMessage)) //
				.containsExactly(
					"Third-party TestEngine implementations are forbidden to use the reserved 'junit-' prefix for their ID: '"
							+ id + "'");
	}

	@Test
	void thirdPartyEngineClaimingToBeJupiterResultsInException() {
		assertImposter("junit-jupiter");
	}

	@Test
	void thirdPartyEngineClaimingToBeVintageResultsInException() {
		assertImposter("junit-vintage");
	}

	private void assertImposter(String id) {
		TestEngine impostor = new TestEngineStub(id);
		var launcher = createLauncher(impostor);
		Exception exception = assertThrows(JUnitException.class, () -> launcher.discover(request().build()));
		assertThat(exception).hasMessage(
			"Third-party TestEngine '%s' is forbidden to use the reserved '%s' TestEngine ID.",
			impostor.getClass().getName(), id);
	}

	@Test
	void dryRunModeReportsEventsForAllTestsButDoesNotExecuteThem() {
		var engine = new DemoHierarchicalTestEngine("engine");
		var container = engine.addContainer("container", "Container", null);
		var test = new DemoHierarchicalTestDescriptor(container.getUniqueId().append("test", "test"), "Test",
			(__, ___) -> {
				throw new RuntimeException("boom");
			});
		container.addChild(test);

		var launcher = createLauncher(engine);
		TestExecutionListener listener = mock();

		launcher.execute(request().configurationParameter(DRY_RUN_PROPERTY_NAME, "true").build(), listener);

		var inOrder = inOrder(listener);
		inOrder.verify(listener).testPlanExecutionStarted(any());
		inOrder.verify(listener).executionStarted(TestIdentifier.from(engine.getEngineDescriptor()));
		inOrder.verify(listener).executionStarted(TestIdentifier.from(container));
		inOrder.verify(listener).executionSkipped(TestIdentifier.from(test), "JUnit Platform dry-run mode is enabled");
		inOrder.verify(listener).executionFinished(TestIdentifier.from(container), successful());
		inOrder.verify(listener).executionFinished(TestIdentifier.from(engine.getEngineDescriptor()), successful());
		inOrder.verify(listener).testPlanExecutionFinished(any());
		inOrder.verifyNoMoreInteractions();
	}
}
