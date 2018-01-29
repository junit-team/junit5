/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical.parallel.classlocking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.platform.commons.annotation.ConcurrentExecution;
import org.junit.platform.commons.annotation.UseResource;

@UseResource("1")
public class A {
	@UseResource("2")
	@Test
	void firstTest(TestReporter reporter) throws Exception {
		GloballySharedResource.incrementWaitAndCheck(GloballySharedResource.sharedResource, reporter);
	}

	@ConcurrentExecution
	@UseResource("2")
	@Test
	void secondTest(TestReporter reporter) throws Exception {
		GloballySharedResource.incrementWaitAndCheck(GloballySharedResource.sharedResource, reporter);
	}

	@UseResource("2")
	@Test
	void thirdTest(TestReporter reporter) throws Exception {
		GloballySharedResource.incrementWaitAndCheck(GloballySharedResource.sharedResource, reporter);
	}
}
