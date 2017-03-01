/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package example.testrule;

// tag::user_guide[]
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.junit.AssumptionViolatedException;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.Stopwatch;
import org.junit.runner.Description;

public class StopwatchVintageDemo {

	private static final Logger logger = Logger.getLogger("");

	private static void logInfo(Description description, String status, long nanos) {
		String testName = description.getMethodName();
		logger.info(
			String.format("Test %s %s, spent %d microseconds", testName, status, TimeUnit.NANOSECONDS.toMicros(nanos)));
	}

	@Rule
	public Stopwatch stopwatch = new Stopwatch() {
		@Override
		protected void succeeded(long nanos, Description description) {
			logInfo(description, "succeeded", nanos);
		}

		@Override
		protected void failed(long nanos, Throwable e, Description description) {
			logInfo(description, "failed", nanos);
		}

		@Override
		protected void skipped(long nanos, AssumptionViolatedException e, Description description) {
			logInfo(description, "skipped", nanos);
		}

		@Override
		protected void finished(long nanos, Description description) {
			logInfo(description, "finished", nanos);
		}
	};

	@Test
	public void succeeds() {
	}

	@Test
	// end::user_guide[]
	@Ignore
	// tag::user_guide[]
	public void fails() {
		fail();
	}

	@Test
	public void skips() {
		assumeTrue(false);
	}

	@Test
	public void performanceTest() throws InterruptedException {
		long delta = 30;
		Thread.sleep(300L);
		assertEquals(300d, stopwatch.runtime(MILLISECONDS), delta);
		Thread.sleep(500L);
		assertEquals(800d, stopwatch.runtime(MILLISECONDS), delta);
	}

}
// end::user_guide[]
