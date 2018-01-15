package org.junit.platform.engine.support.hierarchical.parallel.nestedlocking;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.platform.commons.annotation.ConcurrentExecution;
import org.junit.platform.commons.annotation.UseResource;

@UseResource("1")
class TestClassWithNestedLocks {

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
