package org.junit.platform.engine.support.hierarchical;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.platform.commons.annotation.UseResource;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ParallelExecutionTests {

	@Test
	void test() {

	}

	static class FailingTestWithoutLock {

		static AtomicInteger sharedResource = new AtomicInteger();

		@Test
		void firstTest(TestReporter reporter) throws Exception {
			incrementWaitAndCheck(sharedResource, reporter);
		}

		@Test
		void secondTest(TestReporter reporter) throws Exception {
			incrementWaitAndCheck(sharedResource, reporter);
		}

		@Test
		void thirdTest(TestReporter reporter) throws Exception {
			incrementWaitAndCheck(sharedResource, reporter);
		}
	}

	static class SuccessfulTestWithMethodLock {

		static AtomicInteger sharedResource = new AtomicInteger();

		@Test
		@UseResource("sharedResource")
		void firstTest(TestReporter reporter) throws Exception {
			incrementWaitAndCheck(sharedResource, reporter);
		}

		@Test
		@UseResource("sharedResource")
		void secondTest(TestReporter reporter) throws Exception {
			incrementWaitAndCheck(sharedResource, reporter);
		}

		@Test
		@UseResource("sharedResource")
		void thirdTest(TestReporter reporter) throws Exception {
			incrementWaitAndCheck(sharedResource, reporter);
		}
	}

	@UseResource("sharedResource")
	static class FailingTestWithClassLock {

		static AtomicInteger sharedResource = new AtomicInteger();

		@Test
		void firstTest(TestReporter reporter) throws Exception {
			incrementWaitAndCheck(sharedResource, reporter);
		}

		@Test
		void secondTest(TestReporter reporter) throws Exception {
			incrementWaitAndCheck(sharedResource, reporter);
		}

		@Test
		void thirdTest(TestReporter reporter) throws Exception {
			incrementWaitAndCheck(sharedResource, reporter);
		}
	}

	private static void incrementWaitAndCheck(AtomicInteger sharedResource, TestReporter reporter) throws InterruptedException {
		int value = sharedResource.incrementAndGet();
		Thread.sleep(1000);
		assertEquals(value, sharedResource.get());
		reporter.publishEntry("value", String.valueOf(value));
	}

}
