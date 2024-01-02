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

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.LauncherConstants.DEACTIVATE_LISTENERS_PATTERN_PROPERTY_NAME;
import static org.junit.platform.launcher.LauncherConstants.ENABLE_LAUNCHER_INTERCEPTORS;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.fakes.TestEngineSpy;
import org.junit.platform.launcher.InterceptedTestEngine;
import org.junit.platform.launcher.InterceptorInjectedLauncherSessionListener;
import org.junit.platform.launcher.LauncherConstants;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.LauncherSessionListener;
import org.junit.platform.launcher.TagFilter;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestLauncherDiscoveryListener;
import org.junit.platform.launcher.TestLauncherInterceptor1;
import org.junit.platform.launcher.TestLauncherInterceptor2;
import org.junit.platform.launcher.TestLauncherSessionListener;
import org.junit.platform.launcher.listeners.AnotherUnusedTestExecutionListener;
import org.junit.platform.launcher.listeners.NoopTestExecutionListener;
import org.junit.platform.launcher.listeners.UnusedTestExecutionListener;

/**
 * @since 1.0
 */
class LauncherFactoryTests {

	@Test
	void preconditions() {
		assertThrows(PreconditionViolationException.class, () -> LauncherFactory.create(null));
	}

	@Test
	void testExecutionListenerIsLoadedViaServiceApi() {
		withTestServices(() -> {
			var config = LauncherConfig.builder() //
					.addTestEngines(new TestEngineSpy()) //
					.enableTestEngineAutoRegistration(false) //
					.build();
			var launcher = LauncherFactory.create(config);

			NoopTestExecutionListener.called = false;

			launcher.execute(request().build());

			assertTrue(NoopTestExecutionListener.called);
		});
	}

	@Test
	void testExecutionListenersExcludedViaConfigParametersIsNotLoadedViaServiceApi() {
		withTestServices(() -> {
			var value = "org.junit.*.launcher.listeners.Unused*,org.junit.*.launcher.listeners.AnotherUnused*";
			withSystemProperty(DEACTIVATE_LISTENERS_PATTERN_PROPERTY_NAME, value, () -> {
				var config = LauncherConfig.builder() //
						.addTestEngines(new TestEngineSpy()) //
						.enableTestEngineAutoRegistration(false) //
						.build();
				var launcher = LauncherFactory.create(config);

				UnusedTestExecutionListener.called = false;
				AnotherUnusedTestExecutionListener.called = false;

				launcher.execute(request().build());

				assertFalse(UnusedTestExecutionListener.called);
				assertFalse(AnotherUnusedTestExecutionListener.called);
			});
		});
	}

	@Test
	void create() {
		var discoveryRequest = createLauncherDiscoveryRequestForBothStandardEngineExampleClasses();

		var testPlan = LauncherFactory.create().discover(discoveryRequest);
		var roots = testPlan.getRoots();
		assertThat(roots).hasSize(3);

		// @formatter:off
		var ids = roots.stream()
				.map(TestIdentifier::getUniqueId)
				.collect(toList());
		// @formatter:on

		assertThat(ids).containsOnly("[engine:junit-vintage]", "[engine:junit-jupiter]",
			"[engine:junit-platform-suite]");
	}

	@Test
	void createWithConfig() {
		var discoveryRequest = createLauncherDiscoveryRequestForBothStandardEngineExampleClasses();

		var config = LauncherConfig.builder()//
				.enableTestEngineAutoRegistration(false)//
				.addTestEngines(new JupiterTestEngine())//
				.build();

		var testPlan = LauncherFactory.create(config).discover(discoveryRequest);
		var roots = testPlan.getRoots();
		assertThat(roots).hasSize(1);

		// @formatter:off
		var ids = roots.stream()
				.map(TestIdentifier::getUniqueId)
				.collect(toList());
		// @formatter:on

		assertThat(ids).containsOnly("[engine:junit-jupiter]");
	}

	@Test
	void createWithPostDiscoveryFilters() {
		var discoveryRequest = createLauncherDiscoveryRequestForBothStandardEngineExampleClasses();

		var config = LauncherConfig.builder()//
				.addPostDiscoveryFilters(TagFilter.includeTags("test-post-discovery")).build();

		var testPlan = LauncherFactory.create(config).discover(discoveryRequest);
		final var vintage = testPlan.getChildren(UniqueId.parse("[engine:junit-vintage]"));
		assertThat(vintage).isEmpty();

		final var jupiter = testPlan.getChildren(UniqueId.parse("[engine:junit-jupiter]"));
		assertThat(jupiter).hasSize(1);
	}

	@Test
	void applyPostDiscoveryFiltersViaServiceApi() {
		withTestServices(() -> {
			var discoveryRequest = createLauncherDiscoveryRequestForBothStandardEngineExampleClasses();

			var config = LauncherConfig.builder()//
					.build();

			var testPlan = LauncherFactory.create(config).discover(discoveryRequest);
			final var vintage = testPlan.getChildren(UniqueId.parse("[engine:junit-vintage]"));
			assertThat(vintage).isEmpty();

			final var jupiter = testPlan.getChildren(UniqueId.parse("[engine:junit-jupiter]"));
			assertThat(jupiter).hasSize(1);
		});
	}

	@Test
	void notApplyIfDisabledPostDiscoveryFiltersViaServiceApi() {
		withTestServices(() -> {
			var discoveryRequest = createLauncherDiscoveryRequestForBothStandardEngineExampleClasses();

			var config = LauncherConfig.builder()//
					.enablePostDiscoveryFilterAutoRegistration(false).build();

			var testPlan = LauncherFactory.create(config).discover(discoveryRequest);
			final var vintage = testPlan.getChildren(UniqueId.parse("[engine:junit-vintage]"));
			assertThat(vintage).hasSize(1);

			final var jupiter = testPlan.getChildren(UniqueId.parse("[engine:junit-jupiter]"));
			assertThat(jupiter).hasSize(1);
		});
	}

	@Test
	void doesNotDiscoverLauncherDiscoverRequestListenerViaServiceApiWhenDisabled() {
		withTestServices(() -> {
			var config = LauncherConfig.builder() //
					.enableLauncherDiscoveryListenerAutoRegistration(false) //
					.build();
			var launcher = LauncherFactory.create(config);
			TestLauncherDiscoveryListener.called = false;

			launcher.discover(request().build());

			assertFalse(TestLauncherDiscoveryListener.called);
		});
	}

	@Test
	void discoversLauncherDiscoverRequestListenerViaServiceApiByDefault() {
		withTestServices(() -> {
			var launcher = LauncherFactory.create();
			TestLauncherDiscoveryListener.called = false;

			launcher.discover(request().build());

			assertTrue(TestLauncherDiscoveryListener.called);
		});
	}

	@Test
	void doesNotDiscoverLauncherSessionListenerViaServiceApiWhenDisabled() {
		withTestServices(() -> {
			try (var session = (DefaultLauncherSession) LauncherFactory.openSession(
				LauncherConfig.builder().enableLauncherSessionListenerAutoRegistration(false).build())) {

				assertThat(session.getListener()).isSameAs(LauncherSessionListener.NOOP);
			}
		});
	}

	@Test
	void discoversLauncherSessionListenerViaServiceApiByDefault() {
		withTestServices(() -> {
			try (var session = (DefaultLauncherSession) LauncherFactory.openSession()) {
				assertThat(session.getListener()).isEqualTo(new TestLauncherSessionListener());
			}
		});
	}

	@Test
	void createsLauncherInterceptorsBeforeDiscoveringTestEngines() {
		withTestServices(() -> withSystemProperty(ENABLE_LAUNCHER_INTERCEPTORS, "true", () -> {
			var config = LauncherConfig.builder() //
					.enableTestEngineAutoRegistration(true) //
					.build();
			var request = request().build();

			var testPlan = LauncherFactory.create(config).discover(request);

			assertThat(testPlan.getRoots()) //
					.map(TestIdentifier::getUniqueIdObject) //
					.map(UniqueId::getLastSegment) //
					.map(UniqueId.Segment::getValue) //
					.describedAs(
						"Intercepted test engine is added by class loader created by TestLauncherInterceptor1").contains(
							InterceptedTestEngine.ID);
		}));
	}

	@Test
	void appliesLauncherInterceptorsToTestDiscovery() {
		InterceptorInjectedLauncherSessionListener.CALLS = 0;
		withTestServices(() -> withSystemProperty(ENABLE_LAUNCHER_INTERCEPTORS, "true", () -> {
			var engine = new TestEngineSpy() {
				@Override
				public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
					throw new RuntimeException("from discovery");
				}
			};
			var config = LauncherConfig.builder() //
					.enableTestEngineAutoRegistration(false) //
					.addTestEngines(engine) //
					.build();
			var launcher = LauncherFactory.create(config);
			var request = request().build();

			var exception = assertThrows(RuntimeException.class, () -> launcher.discover(request));

			assertThat(exception) //
					.hasRootCauseMessage("from discovery") //
					.hasStackTraceContaining(TestLauncherInterceptor1.class.getName() + ".intercept(") //
					.hasStackTraceContaining(TestLauncherInterceptor2.class.getName() + ".intercept(");
			assertThat(InterceptorInjectedLauncherSessionListener.CALLS).isEqualTo(1);
		}));
	}

	@Test
	void appliesLauncherInterceptorsToTestExecution() {
		InterceptorInjectedLauncherSessionListener.CALLS = 0;
		withTestServices(() -> withSystemProperty(ENABLE_LAUNCHER_INTERCEPTORS, "true", () -> {
			var engine = new TestEngineSpy() {
				@Override
				public void execute(ExecutionRequest request) {
					throw new RuntimeException("from execution");
				}
			};
			var config = LauncherConfig.builder() //
					.enableTestEngineAutoRegistration(false) //
					.addTestEngines(engine) //
					.build();
			var launcher = LauncherFactory.create(config);
			var request = request().configurationParameter(LauncherConstants.STACKTRACE_PRUNING_ENABLED_PROPERTY_NAME,
				"false").build();

			AtomicReference<TestExecutionResult> result = new AtomicReference<>();
			launcher.execute(request, new TestExecutionListener() {
				@Override
				public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
					if (testIdentifier.getParentId().isEmpty()) {
						result.set(testExecutionResult);
					}
				}
			});

			assertThat(result.get().getThrowable().orElseThrow()) //
					.hasRootCauseMessage("from execution") //
					.hasStackTraceContaining(TestLauncherInterceptor1.class.getName() + ".intercept(") //
					.hasStackTraceContaining(TestLauncherInterceptor2.class.getName() + ".intercept(");
			assertThat(InterceptorInjectedLauncherSessionListener.CALLS).isEqualTo(1);
		}));
	}

	@SuppressWarnings("SameParameterValue")
	private static void withSystemProperty(String key, String value, Runnable runnable) {
		var oldValue = System.getProperty(key);
		System.setProperty(key, value);
		try {
			runnable.run();
		}
		finally {
			if (oldValue == null) {
				System.clearProperty(key);
			}
			else {
				System.setProperty(key, oldValue);
			}
		}
	}

	private static void withTestServices(Runnable runnable) {
		var current = Thread.currentThread().getContextClassLoader();
		var url = LauncherFactoryTests.class.getClassLoader().getResource("testservices/");
		try (var classLoader = new URLClassLoader(new URL[] { url }, current)) {
			Thread.currentThread().setContextClassLoader(classLoader);
			runnable.run();
		}
		catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		finally {
			Thread.currentThread().setContextClassLoader(current);
		}
	}

	private LauncherDiscoveryRequest createLauncherDiscoveryRequestForBothStandardEngineExampleClasses() {
		// @formatter:off
		return request()
				.selectors(selectClass(JUnit4Example.class))
				.selectors(selectClass(JUnit5Example.class))
				.build();
		// @formatter:on
	}

	@SuppressWarnings("NewClassNamingConvention")
	public static class JUnit4Example {

		@org.junit.Test
		public void testJ4() {
		}

	}

	@SuppressWarnings("NewClassNamingConvention")
	static class JUnit5Example {

		@Tag("test-post-discovery")
		@Test
		void testJ5() {
		}

	}
}
