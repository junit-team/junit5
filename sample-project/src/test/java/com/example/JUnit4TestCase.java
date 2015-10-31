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

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;

public class JUnit4TestCase {

	@Test
	public void succeedingTest() {
		// no-op
	}

	@Test
	public void failingTest() {
		Assert.fail("this should fail");
	}

	@Test
	@Ignore
	public void skippedTest() {
		// no-op
	}

	@Test
	public void abortedTest() {
		Assume.assumeTrue("this should be aborted", false);
	}

}
