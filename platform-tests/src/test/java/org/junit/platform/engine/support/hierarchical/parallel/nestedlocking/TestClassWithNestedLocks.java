/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical.parallel.nestedlocking;

import static org.junit.jupiter.api.parallel.Execution.Mode.Concurrent;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.UseResource;

@UseResource("1")
class TestClassWithNestedLocks {

	@UseResource("2")
	@Test
	void firstTest(TestReporter reporter) throws Exception {
		GloballySharedResource.incrementWaitAndCheck(GloballySharedResource.sharedResource, reporter);
	}

	@Execution(Concurrent)
	@UseResource("2")
	@Test
	void secondTest(TestReporter reporter) throws Exception {
		GloballySharedResource.incrementWaitAndCheck(GloballySharedResource.sharedResource, reporter);
	}

	@Test
	void thirdTest(TestReporter reporter) throws Exception {
		GloballySharedResource.incrementWaitAndCheck(GloballySharedResource.sharedResource, reporter);
	}

	@Nested
	@UseResource("2")
	class B {

		@UseResource("1")
		@Test
		void firstTest(TestReporter reporter) throws Exception {
			GloballySharedResource.incrementWaitAndCheck(GloballySharedResource.sharedResource, reporter);
		}

		@UseResource("1")
		@Test
		void secondTest(TestReporter reporter) throws Exception {
			GloballySharedResource.incrementWaitAndCheck(GloballySharedResource.sharedResource, reporter);
		}

		@Test
		void thirdTest(TestReporter reporter) throws Exception {
			GloballySharedResource.incrementWaitAndCheck(GloballySharedResource.sharedResource, reporter);
		}
	}
}
