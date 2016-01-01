/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.example;

import static org.junit.gen5.api.Assertions.fail;

import org.junit.gen5.api.Test;
import org.junit.gen5.junit4runner.JUnit5;
import org.junit.runner.RunWith;

@RunWith(JUnit5.class)
public class AJUnit5TestCaseRunWithJUnit4 {

	@Test
	void aSucceedingTest() {
		/* no-op */
	}

	@Test
	void aFailingTest() {
		fail("Failing for failing's sake.");
	}

}
