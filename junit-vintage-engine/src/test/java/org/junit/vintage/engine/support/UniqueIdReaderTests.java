/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.support;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.runner.Description.createTestDescription;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.Test;
import org.junit.runner.Description;
import org.junit.vintage.engine.RecordCollectingLogger;

/**
 * @since 4.12
 */
class UniqueIdReaderTests {

	@Test
	void readsUniqueId() {
		RecordCollectingLogger logger = new RecordCollectingLogger();
		Description description = createTestDescription("ClassName", "methodName", "uniqueId");

		Serializable uniqueId = new UniqueIdReader(logger).apply(description);

		assertEquals("uniqueId", uniqueId);
		assertThat(logger.getLogRecords()).isEmpty();
	}

	@Test
	void returnsDisplayNameWhenUniqueIdCannotBeRead() {
		RecordCollectingLogger logger = new RecordCollectingLogger();
		Description description = createTestDescription("ClassName", "methodName", "uniqueId");
		assertEquals("methodName(ClassName)", description.getDisplayName());

		Serializable uniqueId = new UniqueIdReader(logger, "wrongFieldName").apply(description);

		assertEquals(description.getDisplayName(), uniqueId);
		assertThat(logger.getLogRecords()).hasSize(1);
		LogRecord logRecord = getOnlyElement(logger.getLogRecords());
		assertEquals(Level.WARNING, logRecord.getLevel());
		assertEquals(
			"Could not read unique ID for Description; using display name instead: " + description.getDisplayName(),
			logRecord.getMessage());
	}

}
