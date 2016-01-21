/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.commons.reporting;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.gen5.commons.util.Preconditions;

/**
 * This class represents a {@code ReportEntry} &mdash;
 * that is a map of {@code String}-based key-value pairs
 * to be published to the reporting infrastructure
 *
 * @since 5.0
 */
public class ReportEntry {

	private final LocalDateTime creationTimestamp = LocalDateTime.now();
	private final Map<String, String> values;

	/**
	 * @param values the values to be published
	 */
	public ReportEntry(Map<String, String> values) {
		Preconditions.notNull(values, "values to be reported must not be null");
		this.values = values;
	}

	/**
	 * Get the values to be published
	 *
	 * @return the map of values to be published
	 */
	public Map<String, String> getValues() {
		return values;
	}

	/**
	 * Get the creation date of this {@code ReportEntry}.
	 * Could be used for e.g., ordering entries.
	 *
	 * @return the date at which this entry was created
	 */
	public LocalDateTime getCreationTimestamp() {
		return creationTimestamp;
	}

}
