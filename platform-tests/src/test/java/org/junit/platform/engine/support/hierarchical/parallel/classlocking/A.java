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
