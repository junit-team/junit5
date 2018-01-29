/*
 * Copyright 2015-2018 the original author or authors.
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
import static org.junit.runner.Description.createTestDescription;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.engine.TrackLogRecords;
import org.junit.platform.commons.logging.LogRecordListener;
import org.junit.runner.Description;

/**
 * Tests for {@link UniqueIdReader}.
 *
 * @since 4.12
 */
@TrackLogRecords
class UniqueIdReaderTests {

	@Test
	void readsUniqueId(LogRecordListener listener) {
		Description description = createTestDescription("ClassName", "methodName", "uniqueId");

		Serializable uniqueId = new UniqueIdReader().apply(description);

		assertEquals("uniqueId", uniqueId);
		assertThat(listener.stream(UniqueIdReader.class)).isEmpty();
	}

	@Test
	void returnsDisplayNameWhenUniqueIdCannotBeRead(LogRecordListener listener) {
		Description description = createTestDescription("ClassName", "methodName", "uniqueId");
		assertEquals("methodName(ClassName)", description.getDisplayName());

		Serializable uniqueId = new UniqueIdReader("wrongFieldName").apply(description);

		assertEquals(description.getDisplayName(), uniqueId);

		// @formatter:off
		assertThat(listener.stream(UniqueIdReader.class, Level.WARNING)
			.map(LogRecord::getMessage)
			.filter(m -> m.equals("Could not read unique ID for Description; using display name instead: "
					+ description.getDisplayName()))
			.count()
		).isEqualTo(1);
		// @formatter:on
	}

}
