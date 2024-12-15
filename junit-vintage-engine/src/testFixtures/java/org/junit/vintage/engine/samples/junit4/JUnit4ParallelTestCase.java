/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.samples.junit4;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;

@RunWith(Enclosed.class)
public class JUnit4ParallelTestCase {

	public static class SuccessfulParallelTestCase {
		static AtomicInteger sharedResource;
		static CountDownLatch countDownLatch;

		@BeforeClass
		public static void initialize() {
			sharedResource = new AtomicInteger();
			countDownLatch = new CountDownLatch(3);
		}

		@Test
		public void firstTest() throws Exception {
			incrementAndBlock(sharedResource, countDownLatch);
		}

		@Test
		public void secondTest() throws Exception {
			incrementAndBlock(sharedResource, countDownLatch);
		}

		@Test
		public void thirdTest() throws Exception {
			incrementAndBlock(sharedResource, countDownLatch);
		}
	}

	public static class FailingParallelTestCase {
		@Test
		public void firstTest() {
			fail("failing test");
		}

		@Test
		public void secondTest() {
			fail("failing test");
		}

		@Test
		public void thirdTest() {
			fail("failing test");
		}
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private static int incrementAndBlock(AtomicInteger sharedResource, CountDownLatch countDownLatch)
			throws InterruptedException {
		var value = sharedResource.incrementAndGet();
		countDownLatch.countDown();
		countDownLatch.await(estimateSimulatedTestDurationInMiliseconds(), MILLISECONDS);
		return value;
	}

	private static long estimateSimulatedTestDurationInMiliseconds() {
		var runningInCi = Boolean.parseBoolean(System.getenv("CI"));
		return runningInCi ? 1000 : 100;
	}
}
