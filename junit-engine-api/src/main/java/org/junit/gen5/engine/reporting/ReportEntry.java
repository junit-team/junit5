/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.reporting;

import static java.text.MessageFormat.format;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;

import org.junit.gen5.commons.util.ExceptionUtils;
import org.junit.gen5.commons.util.Preconditions;

/**
 * This class represents a {@code ReportEntry} &mdash;
 * that is a time-stamped map of {@code String}-based key-value pairs
 * to be published to the reporting infrastructure.
 *
 * @since 5.0
 */
public class ReportEntry {

	private final LocalDateTime creationTimestamp = LocalDateTime.now();
	private final Map<String, String> values;

	/**
	 * Provide a new {@code ReportEntry} with the supplied values.
	 *
	 * @param values the values to be published
	 */
	public static ReportEntry from(Map<String, String> values) {
		return new ReportEntry(values);
	}

	/**
	 * Provide a new {@code ReportEntry} with the supplied values.
	 *
	 * @param key the key of the value to be published
	 * @param value the value to be published
	 */
	public static ReportEntry from(String key, String value) {
		return new ReportEntry(key, value);
	}

	private ReportEntry(Map<String, String> values) {
		Preconditions.notNull(values, "values to be reported must not be null");
		this.values = values;
	}

	private ReportEntry(String key, String value) {
		this(Collections.singletonMap(key, value));
	}

	/**
	 * Get the values to be published.
	 *
	 * @return the map of values to be published
	 */
	public Map<String, String> getValues() {
		return values;
	}

	/**
	 * Get the creation date of this {@code ReportEntry}.
	 *
	 * <p>Can be used, for example, to order entries.
	 *
	 * @return the date at which this entry was created
	 */
	public LocalDateTime getCreationTimestamp() {
		return creationTimestamp;
	}

	@Override
	public String toString() {
		//migrate to org.junit.gen5.commons.util.ToStringBuilder?
		return this.values.toString() + " @ " + this.creationTimestamp;
	}

	public void appendDescription(Appendable appendable, String entryTitle) {
		try {
			appendable.append(format("Report Entry #{0} (creation timestamp: {1})\n", entryTitle,
				ISO_LOCAL_DATE_TIME.format(this.getCreationTimestamp())));
			for (Map.Entry<String, String> entry : this.getValues().entrySet()) {
				appendable.append(format("- {0}: {1}\n", entry.getKey(), entry.getValue()));
			}
		}
		catch (IOException cannotHappen) {
			ExceptionUtils.throwAsUncheckedException(cannotHappen);
		}

	}

}
