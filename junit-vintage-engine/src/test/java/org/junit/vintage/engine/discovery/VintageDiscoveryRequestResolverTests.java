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
import static org.junit.platform.engine.FilterResult.includedIf;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;
import static org.junit.vintage.engine.VintageUniqueIdBuilder.engineId;

import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.vintage.engine.RecordCollectingLogger;

/**
 * @since 4.12
 */
class VintageDiscoveryRequestResolverTests {

	@Test
	void logsWarningWhenFilterExcludesClass() {
		EngineDescriptor engineDescriptor = new EngineDescriptor(engineId(), "JUnit Vintage");
		RecordCollectingLogger logger = new RecordCollectingLogger();

		ClassNameFilter filter = className -> includedIf(Foo.class.getName().equals(className), () -> "match",
			() -> "no match");
		// @formatter:off
		EngineDiscoveryRequest request = request()
				.selectors(selectClass(Foo.class), selectClass(Bar.class))
				.filters(filter)
				.build();
		// @formatter:on

		JUnit4DiscoveryRequestResolver resolver = new JUnit4DiscoveryRequestResolver(engineDescriptor, logger);
		resolver.resolve(request);

		assertThat(engineDescriptor.getChildren()).hasSize(1);

		assertThat(logger.getLogRecords()).hasSize(1);
		LogRecord logRecord = getOnlyElement(logger.getLogRecords());
		assertEquals(Level.FINE, logRecord.getLevel());
		assertEquals("Class " + Bar.class.getName() + " was excluded by a class filter: no match",
			logRecord.getMessage());
	}

	public static class Foo {

		@org.junit.Test
		public void test() {
		}
	}

	public static class Bar {

		@org.junit.Test
		public void test() {
		}

	}

}
