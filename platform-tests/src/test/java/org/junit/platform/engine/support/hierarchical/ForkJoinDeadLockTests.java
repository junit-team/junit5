/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static org.junit.jupiter.api.parallel.ResourceAccessMode.READ;
import static org.junit.jupiter.api.parallel.ResourceAccessMode.READ_WRITE;
import static org.junit.jupiter.api.parallel.Resources.SYSTEM_PROPERTIES;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import java.util.concurrent.CountDownLatch;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.engine.Constants;
import org.junit.platform.engine.discovery.ClassSelector;
import org.junit.platform.testkit.engine.EngineTestKit;

// https://github.com/junit-team/junit5/issues/3945
public class ForkJoinDeadLockTests {

	@Test
	@Timeout(10)
	void forkJoinExecutionDoesNotLeadToDeadLock() {
		run(selectClass(FirstTestCase.class), selectClass(IsolatedTestCase.class),
				selectClass(Isolated2TestCase.class));
	}

	@Test
	@Timeout(10)
	void nestedResourceLocksShouldStillWork() {
		ClassSelector classSelector = selectClass(SharedResourceTestCase.class);
		run(classSelector);
	}

	@Test
	@Timeout(10)
	void multiLevelLocks() {
		ClassSelector classSelector = selectClass(ClassLevelTestCase.class);
		run(classSelector);
	}

	private static void run(ClassSelector... classSelector) {
		EngineTestKit.engine("junit-jupiter").selectors(classSelector) //
				.configurationParameter(Constants.PARALLEL_EXECUTION_ENABLED_PROPERTY_NAME, "true") //
				.configurationParameter(Constants.DEFAULT_PARALLEL_EXECUTION_MODE, "concurrent") //
				.configurationParameter(Constants.DEFAULT_CLASSES_EXECUTION_MODE_PROPERTY_NAME, "concurrent") //
				.configurationParameter(Constants.PARALLEL_CONFIG_STRATEGY_PROPERTY_NAME, "fixed") //
				.configurationParameter(Constants.PARALLEL_CONFIG_FIXED_MAX_POOL_SIZE_PROPERTY_NAME, "3") //
				.configurationParameter(Constants.PARALLEL_CONFIG_FIXED_PARALLELISM_PROPERTY_NAME, "3") //
				.execute();
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	public static class FirstTestCase {
		public static CountDownLatch countDownLatch = new CountDownLatch(2);

		@BeforeAll
		static void beforeAll() {
			// System.out.println("forkJoinWorkerThread = " + Thread.currentThread());
		}

		@Test
		void test1() throws InterruptedException {
			// System.out.println("FirstTestCase.test1 Thread.currentThread() = " + Thread.currentThread());
			await();
		}

		@Test
		void test2() throws InterruptedException {
			// System.out.println("FirstTestCase.test2 Thread.currentThread() = " + Thread.currentThread());
			await();
		}

		private void await() throws InterruptedException {
			countDownLatch.countDown();
			countDownLatch.await();
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@Isolated
	public static class IsolatedTestCase {
		@Test
		void test1() {
			// System.out.println("Isolated Thread.currentThread() = " + Thread.currentThread());
		}
	}
	@SuppressWarnings("JUnitMalformedDeclaration")
	@Isolated
	public static class Isolated2TestCase {
		@Test
		void test1() {
			// System.out.println("Isolated2 Thread.currentThread() = " + Thread.currentThread());
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	public static class SharedResourceTestCase {

		@Test
		@ResourceLock(value = SYSTEM_PROPERTIES, mode = READ)
		void customPropertyIsNotSetByDefault() {

		}

		@Test
		@ResourceLock(value = SYSTEM_PROPERTIES, mode = READ_WRITE)
		void canSetCustomPropertyToApple() {
		}

		@Test
		@ResourceLock(value = SYSTEM_PROPERTIES, mode = READ_WRITE)
		void canSetCustomPropertyToBanana() {
		}
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	@ResourceLock(value = "foo", mode = READ_WRITE)
	public static class ClassLevelTestCase {

		@Test
		@ResourceLock(value = SYSTEM_PROPERTIES, mode = READ)
		void customPropertyIsNotSetByDefault() {

		}

		@Test
		@ResourceLock(value = SYSTEM_PROPERTIES, mode = READ_WRITE)
		void canSetCustomPropertyToApple() {
		}

		@Test
		@ResourceLock(value = SYSTEM_PROPERTIES, mode = READ_WRITE)
		void canSetCustomPropertyToBanana() {
		}
	}
}
