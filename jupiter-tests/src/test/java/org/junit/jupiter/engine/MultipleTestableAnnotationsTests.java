/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.fixtures.TrackLogRecords;
import org.junit.platform.commons.logging.LogRecordListener;

/**
 * Integration tests that verify the correct behavior for methods annotated
 * with multiple testable annotations simultaneously.
 *
 * @since 5.0
 */
class MultipleTestableAnnotationsTests extends AbstractJupiterTestEngineTests {

	@Test
	void testAndRepeatedTest(@TrackLogRecords LogRecordListener listener) {
		discoverTests(request().selectors(selectClass(TestCase.class)).build());

		// @formatter:off
		assertTrue(listener.stream(Level.WARNING)
			.map(LogRecord::getMessage)
			.anyMatch(m -> m.matches("Possible configuration error: method .+ resulted in multiple TestDescriptors .+")));
		// @formatter:on
	}

	static class TestCase {

		@Test
		@RepeatedTest(1)
		void testAndRepeatedTest(RepetitionInfo repetitionInfo) {
		}

	}

}
