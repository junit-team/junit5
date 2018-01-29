/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package example.testinterface;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

//tag::user_guide[]
interface TestInterfaceDynamicTestsDemo {

	@TestFactory
	default Collection<DynamicTest> dynamicTestsFromCollection() {
		// end::user_guide[]
		// @formatter:off
		// tag::user_guide[]
		return Arrays.asList(
			dynamicTest("1st dynamic test in test interface", () -> assertTrue(true)),
			dynamicTest("2nd dynamic test in test interface", () -> assertEquals(4, 2 * 2))
		);
		// end::user_guide[]
		// @formatter:on
		// tag::user_guide[]
	}

}
//end::user_guide[]
