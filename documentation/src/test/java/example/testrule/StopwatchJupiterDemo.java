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

//tag::user_guide[]
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(StopwatchExtension.class)
public class StopwatchJupiterDemo {

	@Test
	public void succeeds(TestReporter testReporter) throws InterruptedException {
		assertTrue(true);
	}

	@Test
	// end::user_guide[]
	@extensions.ExpectToFail
	// tag::user_guide[]
	public void fails() {
		fail("Time a failing test");
	}

	@Test
	public void skips() {
		assumeTrue(false);
	}

	@Test
	public void performanceTest(Stopwatch stopwatch) throws InterruptedException {
		long delta = 30;
		Thread.sleep(300L);
		assertEquals(300d, stopwatch.runtime(MILLISECONDS), delta);
		Thread.sleep(500L);
		assertEquals(800d, stopwatch.runtime(MILLISECONDS), delta);
	}

}
// end::user_guide[]
