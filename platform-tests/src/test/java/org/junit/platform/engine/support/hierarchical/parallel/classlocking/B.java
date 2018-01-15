package org.junit.platform.engine.support.hierarchical.parallel.classlocking;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.platform.commons.annotation.UseResource;

@UseResource("2")
public class B {
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

	@UseResource("1")
	@Test
	void thirdTest(TestReporter reporter) throws Exception {
		GloballySharedResource.incrementWaitAndCheck(GloballySharedResource.sharedResource, reporter);
	}
}
