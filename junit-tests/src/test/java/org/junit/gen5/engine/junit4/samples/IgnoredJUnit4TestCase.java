/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.samples;

import org.junit.Assert;
import org.junit.Ignore;

@Ignore
public class IgnoredJUnit4TestCase {

	@org.junit.Test
	public void test() {
		Assert.fail("this test is not even discovered");
	}

}