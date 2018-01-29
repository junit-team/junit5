/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical.parallel.nestedlocking;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.TestReporter;

public class GloballySharedResource {
	static AtomicInteger sharedResource = new AtomicInteger();

	static void incrementWaitAndCheck(AtomicInteger sharedResource, TestReporter reporter) throws InterruptedException {
		int value = sharedResource.incrementAndGet();
		Thread.sleep(1000);
		assertEquals(value, sharedResource.get());
		reporter.publishEntry("value", String.valueOf(value));
	}
}
