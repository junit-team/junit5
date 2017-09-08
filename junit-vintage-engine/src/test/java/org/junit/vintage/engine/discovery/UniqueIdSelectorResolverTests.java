/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.vintage.engine.VintageUniqueIdBuilder.engineId;
import static org.junit.vintage.engine.descriptor.VintageTestDescriptor.ENGINE_ID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.vintage.engine.RecordCollectingLogger;
import org.junit.vintage.engine.VintageUniqueIdBuilder;

/**
 * @since 4.12
 */
class UniqueIdSelectorResolverTests {

	private static final Predicate<Class<?>> allClassesPredicate = clazz -> true;
	private RecordCollectingLogger logger = new RecordCollectingLogger();
	private TestClassCollector collector = new TestClassCollector();

	@Test
	void logsWarningOnUnloadableTestClass() {
		UniqueId uniqueId = VintageUniqueIdBuilder.uniqueIdForClass("foo.bar.UnknownClass");
		EngineDiscoveryRequest request = requestWithSelector(selectUniqueId(uniqueId));

		new UniqueIdSelectorResolver(logger).resolve(request, allClassesPredicate, collector);

		assertNoRequests();
		assertLoggedWarning("Unresolvable Unique ID (" + uniqueId + "): Unknown class foo.bar.UnknownClass");
	}

	@Test
	void logsWarningForEngineUniqueId() {
		String uniqueId = engineId().toString();
		EngineDiscoveryRequest request = requestWithSelector(selectUniqueId(uniqueId));

		new UniqueIdSelectorResolver(logger).resolve(request, allClassesPredicate, collector);

		assertNoRequests();
		assertLoggedWarning("Unresolvable Unique ID (" + engineId() + "): Cannot resolve the engine's unique ID");
	}

	@Test
	void ignoresUniqueIdsOfOtherEngines() {
		UniqueId uniqueId = UniqueId.forEngine("someEngine");
		EngineDiscoveryRequest request = requestWithSelector(selectUniqueId(uniqueId));

		new UniqueIdSelectorResolver(logger).resolve(request, allClassesPredicate, collector);

		assertNoRequests();
		assertThat(logger.getLogRecords()).isEmpty();
	}

	@Test
	void logsWarningOnUnexpectedTestDescriptor() {
		UniqueId uniqueId = UniqueId.forEngine(ENGINE_ID).append("wrong-type", "foo:bar");
		EngineDiscoveryRequest request = requestWithSelector(selectUniqueId(uniqueId));

		new UniqueIdSelectorResolver(logger).resolve(request, allClassesPredicate, collector);

		assertNoRequests();
		assertLoggedWarning("Unresolvable Unique ID (" + uniqueId
				+ "): Unique ID segment after engine segment must be of type \"runner\"");
	}

	private void assertLoggedWarning(String expectedMessage) {
		assertThat(logger.getLogRecords()).hasSize(1);
		LogRecord logRecord = getOnlyElement(logger.getLogRecords());
		assertEquals(Level.WARNING, logRecord.getLevel());
		assertEquals(expectedMessage, logRecord.getMessage());
	}

	private void assertNoRequests() {
		Stream<TestClassRequest> requests = collector.toRequests();
		assertThat(requests).isEmpty();
	}

	private EngineDiscoveryRequest requestWithSelector(UniqueIdSelector selector) {
		EngineDiscoveryRequest request = mock(EngineDiscoveryRequest.class);
		when(request.getSelectorsByType(UniqueIdSelector.class)).thenReturn(Collections.singletonList(selector));
		return request;
	}
}
