package org.junit.platform.engine.support.hierarchical.parallel.classlocking;

import org.junit.jupiter.api.TestReporter;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GloballySharedResource {
	static AtomicInteger sharedResource = new AtomicInteger();

	static void incrementWaitAndCheck(AtomicInteger sharedResource, TestReporter reporter) throws InterruptedException {
		int value = sharedResource.incrementAndGet();
		Thread.sleep(1000);
		assertEquals(value, sharedResource.get());
		reporter.publishEntry("value", String.valueOf(value));
	}
}
