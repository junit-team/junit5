/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package example;

import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertTrue;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.DisplayName;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.TestInfo;
import org.junit.gen5.junit4.runner.JUnit5;
import org.junit.runner.RunWith;

/**
 * @since 5.0
 */
@RunWith(JUnit5.class)
public
//tag::user_guide[]
class TestInfoTests {

	@BeforeEach
	void init(TestInfo testInfo) {
		String displayName = testInfo.getDisplayName();
		assertTrue(displayName.equals("TEST 1") || displayName.equals("test2"));
	}

	@Test
	@DisplayName("TEST 1")
	void test1(TestInfo testInfo) {
		assertEquals("TEST 1", testInfo.getDisplayName());
	}

	@Test
	void test2() {
	}

}
//end::user_guide[]
