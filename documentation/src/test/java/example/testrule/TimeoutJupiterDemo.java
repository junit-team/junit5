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
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

import java.time.Duration;

import org.junit.jupiter.api.Test;

/**
 * @see http://junit.org/junit4/javadoc/latest/org/junit/rules/Timeout.html
 * @see http://junit.org/junit5/docs/current/api/org/junit/jupiter/api/Assertions.html#assertTimeoutPreemptively-java.time.Duration-org.junit.jupiter.api.function.Executable-
 */
public class TimeoutJupiterDemo {

	@Test
	//end::user_guide[]
	@extensions.ExpectToFail
	//tag::user_guide[]
	public void run1() throws InterruptedException {
		assertTimeoutPreemptively(Duration.ofMillis(20), () -> Thread.sleep(100));
	}

	@Test
	//end::user_guide[]
	@extensions.ExpectToFail
	//tag::user_guide[]
	public void infiniteLoop() {
		assertTimeoutPreemptively(Duration.ofMillis(20), () -> {
			while (true) {
			}
		});
	}

}
//end::user_guide[]
