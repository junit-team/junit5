/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstancePreCreateCallback;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;

/**
 * Integration tests that verify support for {@link TestInstancePreCreateCallback}.
 *
 * @since 5.9.0
 */
class TestInstancePreCreateCallbackTests extends AbstractJupiterTestEngineTests {
	private static final List<String> callSequence = new ArrayList<>();

	@BeforeEach
	void resetCallSequence() {
		callSequence.clear();
	}

	static class CallSequenceRecordingTest {
		protected static void record(String event) {
			callSequence.add(event);
		}
	}

	@ExtendWith(InstancePreCreateCallbackRecordingFoo.class)
	static class InstancePreCreate extends CallSequenceRecordingTest {
		public InstancePreCreate() {
			record("constructor");
		}

		@BeforeAll
		static void beforeAll() {
			record("beforeAll");
		}

		@BeforeEach
		void beforeEach() {
			record("beforeEach");
		}

		@Test
		void test1() {
			record("test1");
		}

		@Test
		void test2() {
			record("test2");
		}

		@AfterEach
		void afterEach() {
			record("afterEach");
		}

		@AfterAll
		static void afterAll() {
			record("afterAll");
		}
	}

	@Test
	void instancePreCreate() {
		executeTestsForClass(InstancePreCreate.class).testEvents().assertStatistics(
			stats -> stats.started(2).succeeded(2));

		// @formatter:off
		assertThat(callSequence).containsExactly(
				"beforeAll",

				"PreCreateCallback: name=foo, testClass=InstancePreCreate, outerInstance: null",
				"constructor",
				"beforeEach",
				"test1",
				"afterEach",

				"PreCreateCallback: name=foo, testClass=InstancePreCreate, outerInstance: null",
				"constructor",
				"beforeEach",
				"test2",
				"afterEach",

				"afterAll"
		);
		// @formatter:on
	}

	@ExtendWith(InstancePreCreateCallbackRecordingFoo.class)
	static class FactoryPreCreate extends CallSequenceRecordingTest {
		@RegisterExtension
		static final TestInstanceFactory factory = (factoryContext, extensionContext) -> {
			record("testInstanceFactory");
			return new FactoryPreCreate();
		};

		public FactoryPreCreate() {
			record("constructor");
		}

		@BeforeAll
		static void beforeAll() {
			record("beforeAll");
		}

		@BeforeEach
		void beforeEach() {
			record("beforeEach");
		}

		@Test
		void test1() {
			record("test1");
		}

		@Test
		void test2() {
			record("test2");
		}

		@AfterEach
		void afterEach() {
			record("afterEach");
		}

		@AfterAll
		static void afterAll() {
			record("afterAll");
		}
	}

	@Test
	void factoryPreCreate() {
		executeTestsForClass(FactoryPreCreate.class).testEvents().assertStatistics(
			stats -> stats.started(2).succeeded(2));

		// @formatter:off
		assertThat(callSequence).containsExactly(
				"beforeAll",

				"PreCreateCallback: name=foo, testClass=FactoryPreCreate, outerInstance: null",
				"testInstanceFactory",
				"constructor",
				"beforeEach",
				"test1",
				"afterEach",

				"PreCreateCallback: name=foo, testClass=FactoryPreCreate, outerInstance: null",
				"testInstanceFactory",
				"constructor",
				"beforeEach",
				"test2",
				"afterEach",

				"afterAll"
		);
		// @formatter:on
	}

	@ExtendWith(InstancePreCreateCallbackRecordingFoo.class)
	static class PreCreateInNested extends CallSequenceRecordingTest {
		static AtomicInteger instanceCounter = new AtomicInteger();
		private final String instanceId;

		public PreCreateInNested() {
			record("constructor");
			instanceId = "#" + instanceCounter.incrementAndGet();
		}

		@BeforeAll
		static void beforeAll() {
			instanceCounter.set(0);
			record("beforeAll");
		}

		@BeforeEach
		void beforeEach() {
			record("beforeEach");
		}

		@Test
		void outerTest1() {
			record("outerTest1");
		}

		@Test
		void outerTest2() {
			record("outerTest2");
		}

		@AfterEach
		void afterEach() {
			record("afterEach");
		}

		@AfterAll
		static void afterAll() {
			record("afterAll");
		}

		@Override
		public String toString() {
			return instanceId;
		}

		@ExtendWith(InstancePreCreateCallbackRecordingBar.class)
		abstract class InnerParent extends CallSequenceRecordingTest {
		}

		@Nested
		@ExtendWith(InstancePreCreateCallbackRecordingBaz.class)
		class Inner extends InnerParent {
			Inner() {
				record("constructorInner");
			}

			@BeforeEach
			void beforeEachInner() {
				record("beforeEachInner");
			}

			@Test
			void innerTest1() {
				record("innerTest1");
			}

			@AfterEach
			void afterEachInner() {
				record("afterEachInner");
			}
		}
	}

	@Test
	void preCreateInNested() {
		executeTestsForClass(PreCreateInNested.class).testEvents().assertStatistics(
			stats -> stats.started(3).succeeded(3));

		// @formatter:off
		assertThat(callSequence).containsExactly(
				"beforeAll",

				"PreCreateCallback: name=foo, testClass=PreCreateInNested, outerInstance: null",
				"constructor",
				"beforeEach",
				"outerTest1",
				"afterEach",

				"PreCreateCallback: name=foo, testClass=PreCreateInNested, outerInstance: null",
				"constructor",
				"beforeEach",
				"outerTest2",
				"afterEach",

				"PreCreateCallback: name=foo, testClass=PreCreateInNested, outerInstance: null",
				"constructor",

				"PreCreateCallback: name=foo, testClass=Inner, outerInstance: #3",
				"PreCreateCallback: name=bar, testClass=Inner, outerInstance: #3",
				"PreCreateCallback: name=baz, testClass=Inner, outerInstance: #3",
				"constructorInner",
				"beforeEach",
				"beforeEachInner",
				"innerTest1",
				"afterEachInner",
				"afterEach",

				"afterAll"
		);
		// @formatter:on
	}

	static class PreCreateOnMethod extends CallSequenceRecordingTest {
		PreCreateOnMethod() {
			record("constructor");
		}

		@BeforeEach
		void beforeEach() {
			record("beforeEach");
		}

		@ExtendWith(InstancePreCreateCallbackRecordingFoo.class)
		@Test
		void test1() {
			record("test1");
		}

		@Test
		void test2() {
			record("test2");
		}

		@AfterEach
		void afterEach() {
			record("afterEach");
		}
	}

	@Test
	void preCreateOnMethod() {
		executeTestsForClass(PreCreateOnMethod.class).testEvents().assertStatistics(
			stats -> stats.started(2).succeeded(2));

		// @formatter:off
		assertThat(callSequence).containsExactly(
				"PreCreateCallback: name=foo, testClass=PreCreateOnMethod, outerInstance: null",
				"constructor",
				"beforeEach",
				"test1",
				"afterEach",

				"constructor",
				"beforeEach",
				"test2",
				"afterEach"
		);
		// @formatter:on
	}

	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@ExtendWith(InstancePreCreateCallbackRecordingFoo.class)
	@ExtendWith(InstancePreCreateCallbackRecordingBar.class)
	static class PreCreateWithClassLifecycle extends CallSequenceRecordingTest {
		PreCreateWithClassLifecycle() {
			record("constructor");
		}

		@BeforeEach
		void beforeEach() {
			record("beforeEach");
		}

		@Test
		void test1() {
			callSequence.add("test1");
		}

		@Test
		void test2() {
			callSequence.add("test2");
		}
	}

	@Test
	void preCreateWithClassLifecycle() {
		executeTestsForClass(PreCreateWithClassLifecycle.class).testEvents().assertStatistics(
			stats -> stats.started(2).succeeded(2));

		// @formatter:off
		assertThat(callSequence).containsExactly(
				"PreCreateCallback: name=foo, testClass=PreCreateWithClassLifecycle, outerInstance: null",
				"PreCreateCallback: name=bar, testClass=PreCreateWithClassLifecycle, outerInstance: null",
				"constructor",
				"beforeEach",
				"test1",
				"beforeEach",
				"test2"
		);
		// @formatter:on
	}

	static abstract class AbstractTestInstancePreCreateCallback implements TestInstancePreCreateCallback {
		private final String name;

		AbstractTestInstancePreCreateCallback(String name) {
			this.name = name;
		}

		@Override
		public void preCreateTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext context) {
			assertThat(context.getTestInstance()).isNotPresent();
			assertThat(context.getTestClass()).isPresent();
			assertThat(factoryContext.getTestClass()).isSameAs(context.getTestClass().get());
			callSequence.add(
				"PreCreateCallback: name=" + name + ", testClass=" + factoryContext.getTestClass().getSimpleName()
						+ ", outerInstance: " + factoryContext.getOuterInstance().orElse(null));
		}
	}

	static class InstancePreCreateCallbackRecordingFoo extends AbstractTestInstancePreCreateCallback {
		InstancePreCreateCallbackRecordingFoo() {
			super("foo");
		}
	}

	static class InstancePreCreateCallbackRecordingBar extends AbstractTestInstancePreCreateCallback {
		InstancePreCreateCallbackRecordingBar() {
			super("bar");
		}
	}

	static class InstancePreCreateCallbackRecordingBaz extends AbstractTestInstancePreCreateCallback {
		InstancePreCreateCallbackRecordingBaz() {
			super("baz");
		}
	}
}
