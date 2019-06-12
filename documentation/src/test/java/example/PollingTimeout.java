/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example;

import org.junit.jupiter.api.Timeout;

class PollingTimeout {
	// tag::user_guide[]
	@Timeout(5) // 5s
	void waitUntil() throws InterruptedException {
		while (!isConditionTrue()) {
			Thread.sleep(250); // use some adapted retry duration
		}
		// if needed asserts on the result of the awaited condition
	}
	// end::user_guide[]

	private boolean isConditionTrue() {
		return true;
	}
}
