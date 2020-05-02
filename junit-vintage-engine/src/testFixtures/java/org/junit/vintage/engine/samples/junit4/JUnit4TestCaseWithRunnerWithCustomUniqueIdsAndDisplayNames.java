/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.samples.junit4;

import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.runner.RunWith;

/**
 * @since 4.12
 */
@DisplayName("(TestClass)")
@RunWith(RunnerWithCustomUniqueIdsAndDisplayNames.class)
public class JUnit4TestCaseWithRunnerWithCustomUniqueIdsAndDisplayNames {

	@Test
	@DisplayName("(TestMethod)")
	public void test() {
		Assert.fail();
	}

}
