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
import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.ExceptionUtils;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ToStringBuilder;

/**
 * {@code ReportEntry} encapsulates a time-stamped map of {@code String}-based
 * key-value pairs to be published to the reporting infrastructure.
 *
 * @since 5.0
 * @see #from(Map)
 * @see #from(String, String)
 */
@API(Experimental)
public final class ReportEntry {

	private final LocalDateTime creationTimestamp = LocalDateTime.now();
	private final Map<String, String> keyValuePairs = new LinkedHashMap<>();

	/**
	 * Factory for creating a new {@code ReportEntry} from a map of key-value pairs.
	 *
	 * @param keyValuePairs the map of key-value pairs to be published; never
	 * {@code null}; keys and values within entries in the map also must not be
	 * {@code null} or empty
	 */
	public static ReportEntry from(Map<String, String> keyValuePairs) {
		Preconditions.notNull(keyValuePairs, "keyValuePairs must not be null");

		ReportEntry reportEntry = new ReportEntry();
		keyValuePairs.forEach(reportEntry::add);
		return reportEntry;
	}

	/**
	 * Factory for creating a new {@code ReportEntry} from a key-value pair.
	 *
	 * @param key the key under which the value should published; never
	 * {@code null} or empty
	 * @param value the value to publish; never {@code null} or empty
	 */
	public static ReportEntry from(String key, String value) {
		ReportEntry reportEntry = new ReportEntry();
		reportEntry.add(key, value);
		return reportEntry;
	}

	private void add(String key, String value) {
		Preconditions.notBlank(key, "key must not be null or empty");
		Preconditions.notBlank(value, "value must not be null or empty");
		this.keyValuePairs.put(key, value);
	}

	/**
	 * Get an unmodifiable copy of the map of key-value pairs to be published.
	 *
	 * @return a copy of the map of key-value pairs; never {@code null}
	 */
	public final Map<String, String> getKeyValuePairs() {
		return Collections.unmodifiableMap(this.keyValuePairs);
	}

	/**
	 * Get the creation timestamp of this {@code ReportEntry}.
	 *
	 * <p>Can be used, for example, to order entries.
	 *
	 * @return when this entry was created; never {@code null}
	 */
	public final LocalDateTime getCreationTimestamp() {
		return this.creationTimestamp;
	}

	/**
	 * {@linkplain #appendDescription(Appendable, String) Append} a description
	 * of this {@code ReportEntry} to the supplied {@link Appendable}.
	 *
	 * @param appendable the {@code Appendable} to append to; never {@code null}
	 * @see #appendDescription(Appendable, String)
	 */
	public void appendDescription(Appendable appendable) {
		Preconditions.notNull(appendable, "appendable must not be null");
		appendDescription(appendable, "");
	}

	/**
	 * Append a description of this {@code ReportEntry} with an optional title
	 * to the supplied {@link Appendable}.
	 *
	 * <p>TODO Document semantics of appendDescription(Appendable, String).
	 *
	 * @param appendable the {@code Appendable} to append to; never {@code null}
	 * @param entryTitle an optional title for this {@code ReportEntry}; never
	 * {@code null} but potentially empty
	 * @see #appendDescription(Appendable)
	 */
	public void appendDescription(Appendable appendable, String entryTitle) {
		Preconditions.notNull(appendable, "appendable must not be null");
		Preconditions.notNull(entryTitle, "entryTitle must not be null");

		// Add left padding
		entryTitle = (entryTitle.length() > 0 ? " " + entryTitle : entryTitle);

		try {
			appendable.append(format("Report Entry{0} (creation timestamp: {1})\n", entryTitle,
				ISO_LOCAL_DATE_TIME.format(this.creationTimestamp)));

			for (Map.Entry<String, String> entry : this.keyValuePairs.entrySet()) {
				appendable.append(format("\t- {0}: {1}\n", entry.getKey(), entry.getValue()));
			}
		}
		catch (IOException cannotHappen) {
			ExceptionUtils.throwAsUncheckedException(cannotHappen);
		}
	}

	@Override
	public String toString() {
		ToStringBuilder builder = new ToStringBuilder(this);
		builder.append("creationTimestamp", this.creationTimestamp);
		this.keyValuePairs.forEach(builder::append);
		return builder.toString();
	}

}
