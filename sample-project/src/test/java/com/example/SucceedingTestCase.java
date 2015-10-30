/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.example;

import static org.junit.gen5.api.Assertions.*;
import static org.junit.gen5.api.Assumptions.assumeTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.gen5.api.After;
import org.junit.gen5.api.Before;
import org.junit.gen5.api.Test;
import org.opentestalliance.TestSkippedException;

/**
 * Named *TestCase so Gradle will not try to run it.
 */
class SucceedingTestCase {

	@Before
	void before() {
		System.out.println(getClass().getName() + " before called");
	}

	@After
	void after() {
		System.out.println(getClass().getName() + " after called");
	}

	@Test(name = "A nice name for test 2")
	void test1() {

	}

	@Test(name = "A test name with umlauts äöüÄÖÜß")
	void test2() {

	}
}
