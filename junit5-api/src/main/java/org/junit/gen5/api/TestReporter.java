/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api;

import java.util.Map;

import org.junit.gen5.commons.reporting.ReportEntry;

/**
 * Parameters of type {@code TestReporter} can be injected into methods of
 * test classes annotated with {@link BeforeEach @BeforeEach},
 * {@link AfterEach @AfterEach}, and {@link Test @Test}.
 *
 * <p>Within such methods a {@code TestReporter} can be used to publish
 * {@link ReportEntry} instances.
 *
 * @since 5.0
 * @see ReportEntry
 */
@FunctionalInterface
public interface TestReporter {

	/**
	 * Publish the supplied {@code ReportEntry}.
	 *
	 * @param entry the entry to publish
	 */
	void publishEntry(ReportEntry entry);

	default void publishEntry(String key, String value) {
		this.publishEntry(ReportEntry.from(key, value));
	}

	default void publishEntry(Map<String, String> values) {
		this.publishEntry(ReportEntry.from(values));
	}

}
