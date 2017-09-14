/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * Integration tests that verify the correct behavior for methods annotated
 * with multiple testable annotations simultaneously.
 *
 * <h3>Logging Configuration</h3>
 *
 * <p>In order for our log4j2 configuration to be used in an IDE, you must
 * set the following system property before running any tests &mdash; for
 * example, in <em>Run Configurations</em> in Eclipse.
 *
 * <pre class="code">
 * -Djava.util.logging.manager=org.apache.logging.log4j.jul.LogManager
 * </pre>
 *
 * @since 5.0
 */
@FullLogging(classNames = MultipleTestableAnnotationsTests.RESOLVER)
class MultipleTestableAnnotationsTests extends AbstractJupiterTestEngineTests {

	static final String RESOLVER = "org.junit.jupiter.engine.discovery.JavaElementsResolver";

	@Test
	void testAndRepeatedTest() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(TestCase.class)).build();
		Logger logger = Logger.getLogger(RESOLVER);

		if (logger.getClass().getName().startsWith("org.apache.logging.log4j.jul")) {
			assertionsForLog4j(request, logger);
		}
		else {
			assertionsForJavaUtilLogging(request, logger);
		}
	}

	private void assertionsForLog4j(LauncherDiscoveryRequest request, Logger logger) {
		// org.apache.logging.log4j.jul.CoreLogger
		// TODO Set up record collecting Appender for Log4j
		// discoverTests(request);
		// TODO Assert that log record message contains "Possible configuration error", "method", and "resulted in multiple TestDescriptors"
	}

	private void assertionsForJavaUtilLogging(LauncherDiscoveryRequest request, Logger logger) {
		RecordCollectingHandler handler = new RecordCollectingHandler();
		logger.addHandler(handler);

		discoverTests(request);

		List<LogRecord> logRecords = handler.getLogRecords();
		assertEquals(1, logRecords.size());
		assertThat(logRecords.get(0).getMessage()).contains("Possible configuration error", "method",
			"resulted in multiple TestDescriptors");
	}

	static class TestCase {

		@Test
		@RepeatedTest(1)
		void testAndRepeatedTest(RepetitionInfo repetitionInfo) {
		}

	}

	private static final class RecordCollectingHandler extends Handler {

		private final List<LogRecord> logRecords = new ArrayList<>();

		@Override
		public void publish(LogRecord record) {
			this.logRecords.add(record);
		}

		public List<LogRecord> getLogRecords() {
			return this.logRecords;
		}

		@Override
		public void flush() {
		}

		@Override
		public void close() throws SecurityException {
		}

	}

}
