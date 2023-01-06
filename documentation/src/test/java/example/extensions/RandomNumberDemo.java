/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example.extensions;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("RandomNumberExtension has not been implemented")
//tag::user_guide[]
class RandomNumberDemo {

	// use random number field in test methods and @BeforeEach
	// or @AfterEach lifecycle methods
	@Random
	private int randomNumber1;

	RandomNumberDemo(@Random int randomNumber2) {
		// use random number in constructor
	}

	@BeforeEach
	void beforeEach(@Random int randomNumber3) {
		// use random number in @BeforeEach method
	}

	@Test
	void test(@Random int randomNumber4) {
		// use random number in test method
	}

}
//end::user_guide[]
