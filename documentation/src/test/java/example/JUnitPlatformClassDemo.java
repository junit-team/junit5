/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example;

// tag::user_guide[]
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;

//end::user_guide[]
@SuppressWarnings("deprecation")
//tag::user_guide[]
@RunWith(org.junit.platform.runner.JUnitPlatform.class)
public class JUnitPlatformClassDemo {

	@Test
	void succeedingTest() {
		/* no-op */
	}

	// end::user_guide[]
	@extensions.ExpectToFail
	// tag::user_guide[]
	@Test
	void failingTest() {
		fail("Failing for failing's sake.");
	}

}
// end::user_guide[]
