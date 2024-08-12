/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.descriptor;

import static java.util.Collections.emptySet;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;
import static org.junit.platform.testkit.engine.EventConditions.event;
import static org.junit.platform.testkit.engine.EventConditions.finishedSuccessfully;
import static org.junit.platform.testkit.engine.EventConditions.test;

import java.lang.reflect.Method;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.ResourceLocksProvider;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;

/**
 * @since 5.12
 */
class ResourceLocksProviderIntegrationTests {

	@Test
	void provideForClassAndProvideForMethodCalledWithCorrectArguments() {
		var events = execute(ProvideForClassAndProvideForMethodTestCase.class);
		assertThat(events.filter(event(test(), finishedSuccessfully())::matches)).hasSize(2);
	}

	@Test
	void provideForNestedClassAndProvideForMethodCalledWithCorrectArguments() {
		var events = execute(ProvideForNestedClassAndProvideForMethodTestCase.class);
		assertThat(events.filter(event(test(), finishedSuccessfully())::matches)).hasSize(2);
	}

	@Test
	void provideForMethodCalledWithCorrectArguments() {
		var events = execute(ProvideForMethodTestCase.class);
		assertThat(events.filter(event(test(), finishedSuccessfully())::matches)).hasSize(2);
	}

	private Stream<Event> execute(Class<?> testCase) {
		// @formatter:off
		var discoveryRequest = request()
				.selectors(Stream.of(testCase).map(DiscoverySelectors::selectClass).toList())
				.build();
		return EngineTestKit.execute("junit-jupiter", discoveryRequest)
				.allEvents()
				.stream();
		// @formatter:on
	}

	// -------------------------------------------------------------------------

	@ResourceLock(providers = ProvideForClassAndProvideForMethodTestCase.Provider.class)
	static class ProvideForClassAndProvideForMethodTestCase {

		@Test
		void test() {
			assertTrue(Provider.isProvideForClassCalled, "'provideForClass' was not called");
			Provider.isProvideForClassCalled = false;

			assertTrue(Provider.isProvideForMethodCalled, "'provideForMethod' was not called");
			Provider.isProvideForMethodCalled = false;
		}

		@Nested
		class NestedClass {

			@Test
			void nestedTest() {
			}
		}

		static class Provider implements ResourceLocksProvider {

			private static boolean isProvideForClassCalled = false;

			private static boolean isProvideForMethodCalled = false;

			@Override
			public Set<Lock> provideForClass(Class<?> testClass) {
				isProvideForClassCalled = true;
				assertEquals(ProvideForClassAndProvideForMethodTestCase.class, testClass);
				return emptySet();
			}

			@Override
			public Set<Lock> provideForNestedClass(Class<?> testClass) {
				fail("'provideForNestedClass' should not be called");
				return emptySet();
			}

			@Override
			public Set<Lock> provideForMethod(Class<?> testClass, Method testMethod) {
				isProvideForMethodCalled = true;
				assertEquals(ProvideForClassAndProvideForMethodTestCase.class, testClass);
				assertEquals("test", testMethod.getName());
				return emptySet();
			}
		}
	}

	static class ProvideForNestedClassAndProvideForMethodTestCase {

		@Test
		void test() {
		}

		@Nested
		@ResourceLock(providers = ProvideForNestedClassAndProvideForMethodTestCase.Provider.class)
		class NestedClass {

			@Test
			void nestedTest() {
				assertTrue(Provider.isProvideForNestedClassCalled, "'provideForNestedClass' was not called");
				Provider.isProvideForNestedClassCalled = false;

				assertTrue(Provider.isProvideForMethodCalled, "'provideForMethod' was not called");
				Provider.isProvideForMethodCalled = false;
			}
		}

		static class Provider implements ResourceLocksProvider {

			private static boolean isProvideForNestedClassCalled = false;

			private static boolean isProvideForMethodCalled = false;

			@Override
			public Set<Lock> provideForClass(Class<?> testClass) {
				fail("'provideForClass' should not be called");
				return emptySet();
			}

			@Override
			public Set<Lock> provideForNestedClass(Class<?> testClass) {
				isProvideForNestedClassCalled = true;
				assertEquals(NestedClass.class, testClass);
				return emptySet();
			}

			@Override
			public Set<Lock> provideForMethod(Class<?> testClass, Method testMethod) {
				isProvideForMethodCalled = true;
				assertEquals(ProvideForNestedClassAndProvideForMethodTestCase.NestedClass.class, testClass);
				assertEquals("nestedTest", testMethod.getName());
				return emptySet();
			}
		}
	}

	static class ProvideForMethodTestCase {

		@Test
		@ResourceLock(providers = ProvideForMethodTestCase.Provider.class)
		void test() {
			assertTrue(Provider.isProvideForMethodCalled, "'provideForMethod' was not called");
			Provider.isProvideForMethodCalled = false;
		}

		@Nested
		class NestedClass {

			@Test
			void nestedTest() {
			}
		}

		static class Provider implements ResourceLocksProvider {

			private static boolean isProvideForMethodCalled = false;

			@Override
			public Set<Lock> provideForClass(Class<?> testClass) {
				fail("'provideForClass' should not be called");
				return emptySet();
			}

			@Override
			public Set<Lock> provideForNestedClass(Class<?> testClass) {
				fail("'provideForNestedClass' should not be called");
				return emptySet();
			}

			@Override
			public Set<Lock> provideForMethod(Class<?> testClass, Method testMethod) {
				isProvideForMethodCalled = true;
				assertEquals(ProvideForMethodTestCase.class, testClass);
				assertEquals("test", testMethod.getName());
				return emptySet();
			}
		}
	}
}
