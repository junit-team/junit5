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

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @since 5.0
 */
public class PlainJUnit4TestCaseWithSingleTestWhichIsIgnored {

	@Test
	@Ignore("ignored test")
	public void ignoredTest() {
		Assert.fail("this should not be called");
	}

}
