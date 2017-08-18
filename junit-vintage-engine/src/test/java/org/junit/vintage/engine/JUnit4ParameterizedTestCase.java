/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.vintage.engine;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class JUnit4ParameterizedTestCase {

	private final boolean b;
	private final long l;
	private final int i;

	@Parameterized.Parameters(name = "{0} ---> : Test")
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { 1L, false, 21 }, { 2L, true, 11 }, { 3L, false, 21 }, });
	}

	public JUnit4ParameterizedTestCase(long l, boolean b, int i) {
		this.b = b;
		this.l = l;
		this.i = i;
	}

	@Test
	public void test1() {
		fail("this test should fail");
	}

	@Test
	public void endingIn_test1() {
		fail("this test should fail");
	}

	@Test
	public void test1_atTheBeginning() {
		fail("this test should fail");
	}

	@Test
	public void test2() {
		/* always succeed */
	}
}
