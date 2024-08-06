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
import org.junit.jupiter.api.parallel.ResourceLocksFrom;
import org.junit.jupiter.api.parallel.ResourceLocksProvider;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Event;

/**
 * @since 5.12
 */
class ResourceLocksProviderIntegrationTests {

	@Test
	void provideLocksForClassAndMethod() {
		var events = execute(ProvideForClassAndMethodTestCase.class);
		assertThat(events.filter(event(test(), finishedSuccessfully())::matches)).hasSize(2);
	}

	@Test
	void provideLocksForNestedClassAndMethod() {
		var events = execute(ProvideForNestedClassAndMethodTestCase.class);
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

	@ResourceLocksFrom(ProvideForClassAndMethodTestCase.Provider.class)
	static class ProvideForClassAndMethodTestCase {

		@Test
		void classMethod() {
		}

		@Nested
		class NestedClass {
			@Test
			void nestedClassMethod() {
			}
		}

		static final class Provider implements ResourceLocksProvider {

			@Override
			public Set<Lock> provideForClass(Class<?> testClass) {
				assertEquals(ProvideForClassAndMethodTestCase.class, testClass);
				return emptySet();
			}

			@Override
			public Set<Lock> provideForNestedClass(Class<?> testClass) {
				fail("'provideForNestedClass' should not be called");
				return emptySet();
			}

			@Override
			public Set<Lock> provideForMethod(Class<?> testClass, Method testMethod) {
				assertEquals(ProvideForClassAndMethodTestCase.class, testClass);
				assertEquals("classMethod", testMethod.getName());
				return emptySet();
			}
		}
	}

	static class ProvideForNestedClassAndMethodTestCase {

		@Test
		void classMethod() {
		}

		@ResourceLocksFrom(ProvideForNestedClassAndMethodTestCase.Provider.class)
		@Nested
		class NestedClass {
			@Test
			void nestedClassMethod() {
			}
		}

		static final class Provider implements ResourceLocksProvider {

			@Override
			public Set<Lock> provideForClass(Class<?> testClass) {
				fail("'provideForClass' should not be called");
				return emptySet();
			}

			@Override
			public Set<Lock> provideForNestedClass(Class<?> testClass) {
				assertEquals(NestedClass.class, testClass);
				return emptySet();
			}

			@Override
			public Set<Lock> provideForMethod(Class<?> testClass, Method testMethod) {
				assertEquals(NestedClass.class, testClass);
				assertEquals("nestedClassMethod", testMethod.getName());
				return emptySet();
			}
		}
	}
}
