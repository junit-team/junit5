/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

// tag::user_guide[]
class TimeoutDemo {

	@BeforeEach
	@Timeout(5)
	void setUp() {
		// fails if execution time exceeds 5 seconds
	}

	@Test
	@Timeout(value = 100, unit = TimeUnit.MILLISECONDS)
	void failsIfExecutionTimeExceeds100Milliseconds() {
		// fails if execution time exceeds 100 milliseconds
	}

}
// end::user_guide[]
