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
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.vintage.engine.VintageUniqueIdBuilder.engineId;
import static org.junit.vintage.engine.descriptor.VintageTestDescriptor.ENGINE_ID;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.TrackLogRecords;
import org.junit.platform.commons.logging.LogRecordListener;
import org.junit.platform.commons.util.ClassFilter;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.UniqueIdSelector;
import org.junit.vintage.engine.VintageUniqueIdBuilder;

/**
 * Tests for {@link UniqueIdSelectorResolver}.
 *
 * @since 4.12
 */
@TrackLogRecords
class UniqueIdSelectorResolverTests {

	private static final ClassFilter allClassesPredicate = ClassFilter.of(clazz -> true);

	private final TestClassCollector collector = new TestClassCollector();

	@Test
	void logsWarningOnUnloadableTestClass(LogRecordListener listener) {
		UniqueId uniqueId = VintageUniqueIdBuilder.uniqueIdForClass("foo.bar.UnknownClass");
		EngineDiscoveryRequest request = requestWithSelector(selectUniqueId(uniqueId));

		new UniqueIdSelectorResolver().resolve(request, allClassesPredicate, collector);

		assertNoRequests();
		assertLoggedWarning(listener, "Unresolvable Unique ID (" + uniqueId + "): Unknown class foo.bar.UnknownClass");
	}

	@Test
	void logsWarningForEngineUniqueId(LogRecordListener listener) {
		String uniqueId = engineId().toString();
		EngineDiscoveryRequest request = requestWithSelector(selectUniqueId(uniqueId));

		new UniqueIdSelectorResolver().resolve(request, allClassesPredicate, collector);

		assertNoRequests();
		assertLoggedWarning(listener,
			"Unresolvable Unique ID (" + engineId() + "): Cannot resolve the engine's unique ID");
	}

	@Test
	void ignoresUniqueIdsOfOtherEngines(LogRecordListener listener) {
		UniqueId uniqueId = UniqueId.forEngine("someEngine");
		EngineDiscoveryRequest request = requestWithSelector(selectUniqueId(uniqueId));

		new UniqueIdSelectorResolver().resolve(request, allClassesPredicate, collector);

		assertNoRequests();
		assertThat(listener.getLogRecords(UniqueIdSelectorResolver.class)).isEmpty();
	}

	@Test
	void logsWarningOnUnexpectedTestDescriptor(LogRecordListener listener) {
		UniqueId uniqueId = UniqueId.forEngine(ENGINE_ID).append("wrong-type", "foo:bar");
		EngineDiscoveryRequest request = requestWithSelector(selectUniqueId(uniqueId));

		new UniqueIdSelectorResolver().resolve(request, allClassesPredicate, collector);

		assertNoRequests();
		assertLoggedWarning(listener, "Unresolvable Unique ID (" + uniqueId
				+ "): Unique ID segment after engine segment must be of type \"runner\"");
	}

	private void assertLoggedWarning(LogRecordListener listener, String expectedMessage) {
		// @formatter:off
		assertThat(listener.getLogRecords(UniqueIdSelectorResolver.class, Level.WARNING)
			.map(LogRecord::getMessage)
			.filter(m -> m.equals(expectedMessage))
			.count()
		).isEqualTo(1);
		// @formatter:on
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
