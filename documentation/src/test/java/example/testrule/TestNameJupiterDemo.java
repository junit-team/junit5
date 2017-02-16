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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

/**
 * @see http://junit.org/junit4/javadoc/latest/org/junit/rules/TestName.html
 * @see http://junit.org/junit5/docs/current/api/org/junit/jupiter/api/TestInfo.html
 */
public class TestNameJupiterDemo {

	@Test
	public void testA(TestInfo testInfo) {
		assertEquals("testA(TestInfo)", testInfo.getDisplayName());
		assertTrue(testInfo.getDisplayName().startsWith("testA"));
	}

	@Test
	public void testB(TestInfo testInfo) {
		assertEquals("testB(TestInfo)", testInfo.getDisplayName());
		assertTrue(testInfo.getDisplayName().startsWith("testB"));
	}

}
//end::user_guide[]

