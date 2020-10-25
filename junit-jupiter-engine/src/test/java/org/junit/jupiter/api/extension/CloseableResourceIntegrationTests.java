/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import static org.junit.platform.testkit.engine.EventConditions.reportEntry;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;

public class CloseableResourceIntegrationTests extends AbstractJupiterTestEngineTests {

	@Test
	void closesCloseableResourcesInReverseInsertOrder() {
		executeTestsForClass(TestCase.class).allEvents().reportingEntryPublished() //
				.assertEventsMatchExactly( //
					reportEntry(Map.of("3", "closed")), //
					reportEntry(Map.of("2", "closed")), //
					reportEntry(Map.of("1", "closed")));
	}

	@ExtendWith(ExtensionContextParameterResolver.class)
	static class TestCase {
		@Test
		void closesCloseableResourcesInExtensionContext(ExtensionContext extensionContext) {
			ExtensionContext.Store store = extensionContext.getStore(ExtensionContext.Namespace.GLOBAL);
			store.put("foo", reportEntryOnClose(extensionContext, "1"));
			store.put("bar", reportEntryOnClose(extensionContext, "2"));
			store.put("baz", reportEntryOnClose(extensionContext, "3"));
		}

		private ExtensionContext.Store.CloseableResource reportEntryOnClose(ExtensionContext extensionContext,
				String key) {
			return () -> extensionContext.publishReportEntry(Map.of(key, "closed"));
		}
	}
}
