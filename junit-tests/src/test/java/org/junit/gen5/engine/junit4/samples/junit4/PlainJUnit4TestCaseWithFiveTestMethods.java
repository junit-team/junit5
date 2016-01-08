/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.samples.junit4;

import static org.junit.Assert.*;
import static org.junit.Assume.assumeFalse;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;

@FixMethodOrder(NAME_ASCENDING)
public class PlainJUnit4TestCaseWithFiveTestMethods {

	@Test
	public void abortedTest() {
		assumeFalse("this test should be aborted", true);
	}

	@Test
	public void failingTest() {
		fail("this test should fail");
	}

	@Test
	@Ignore
	public void ignoredTest1_withoutReason() {
		fail("this should never be called");
	}

	@Test
	@Ignore("a custom reason")
	public void ignoredTest2_withReason() {
		fail("this should never be called");
	}

	@Test
	public void successfulTest() {
		assertEquals(3, 1 + 2);
	}

}