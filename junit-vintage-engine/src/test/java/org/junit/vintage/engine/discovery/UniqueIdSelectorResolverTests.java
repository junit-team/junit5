/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.vintage.engine.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.vintage.engine.JUnit4UniqueIdBuilder.engineId;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.vintage.engine.JUnit4UniqueIdBuilder;
import org.junit.vintage.engine.RecordCollectingLogger;

/**
 * @since 5.0
 */
class UniqueIdSelectorResolverTests {

	@Test
	void logsWarningOnUnloadableTestClass() {
		UniqueId uniqueId = JUnit4UniqueIdBuilder.uniqueIdForClass("foo.bar.UnknownClass");
		RecordCollectingLogger logger = new RecordCollectingLogger();
		UniqueIdSelector selector = UniqueIdSelector.selectUniqueId(uniqueId);
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

	@Test
	void logsWarningForEngineUniqueId() {
		String uniqueId = engineId().toString();
		RecordCollectingLogger logger = new RecordCollectingLogger();
		UniqueIdSelector selector = UniqueIdSelector.selectUniqueId(uniqueId);
		TestClassCollector collector = new TestClassCollector();

		new UniqueIdSelectorResolver(logger).resolve(selector, collector);

		Set<TestClassRequest> requests = collector.toRequests(c -> true);
		assertThat(requests).isEmpty();
		assertThat(logger.getLogRecords()).hasSize(1);
		LogRecord logRecord = getOnlyElement(logger.getLogRecords());
		assertEquals(Level.WARNING, logRecord.getLevel());
		assertEquals("Unresolvable Unique ID (" + engineId() + "): Cannot resolve the engine's unique ID",
			logRecord.getMessage());
	}

	@Test
	void ignoresUniqueIdsOfOtherEngines() {
		UniqueId uniqueId = UniqueId.forEngine("someEngine");
		RecordCollectingLogger logger = new RecordCollectingLogger();
		UniqueIdSelector selector = UniqueIdSelector.selectUniqueId(uniqueId);
		TestClassCollector collector = new TestClassCollector();

		new UniqueIdSelectorResolver(logger).resolve(selector, collector);

		Set<TestClassRequest> requests = collector.toRequests(c -> true);
		assertThat(requests).isEmpty();
		assertThat(logger.getLogRecords()).isEmpty();
	}

}
