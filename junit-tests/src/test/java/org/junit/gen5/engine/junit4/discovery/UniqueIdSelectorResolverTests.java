/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.commons.util.CollectionUtils.getOnlyElement;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.gen5.api.Test;
import org.junit.gen5.commons.logging.RecordCollectingLogger;
import org.junit.gen5.engine.discovery.UniqueIdSelector;

class UniqueIdSelectorResolverTests {

	@Test
	void logsWarningOnUnloadableTestClass() {
		String uniqueId = "junit4:foo.bar.UnknownClass";
		RecordCollectingLogger logger = new RecordCollectingLogger();
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(uniqueId);
		TestClassCollector collector = new TestClassCollector();

		new UniqueIdSelectorResolver(logger).resolve(selector, collector);

		Set<TestClassRequest> requests = collector.toRequests(c -> true);
		assertThat(requests).isEmpty();
		assertThat(logger.getLogRecords()).hasSize(1);
		LogRecord logRecord = getOnlyElement(logger.getLogRecords());
		assertEquals(Level.WARNING, logRecord.getLevel());
		assertEquals("Unresolvable Unique ID (" + uniqueId + "): Unknown class foo.bar.UnknownClass",
			logRecord.getMessage());
	}

}
