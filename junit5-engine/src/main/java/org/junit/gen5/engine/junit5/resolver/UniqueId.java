/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.resolver;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.LinkedList;

import org.junit.gen5.commons.util.StringUtils;

public final class UniqueId {
	public static UniqueId empty() {
		return EMPTY_UNIQUE_ID;
	}

	public static UniqueId from(String uniqueIdString) {
		if (StringUtils.isBlank(uniqueIdString)) {
			return empty();
		}

		UniqueId uniqueId = empty();
		for (String segment : uniqueIdString.split(DELIMITER)) {
			uniqueId = uniqueId.append(extractKey(segment), extractValue(segment));
		}
		return uniqueId;
	}

	public static UniqueId from(String key, String value) {
		if (StringUtils.isBlank(key) || StringUtils.isBlank(value)) {
			return empty();
		}
		else {
			String entry = String.format("%s%s%s%s%s", BEGINNING, key, SPLITTER, value, ENDING);
			return new UniqueId(entry);
		}
	}

	private static final UniqueId EMPTY_UNIQUE_ID = new UniqueId("");
	private static final String BEGINNING = "[";
	private static final String SPLITTER = ":";
	private static final String ENDING = "]";
	private static final String DELIMITER = "/";

	private String[] uniqueIdSegments;

	private UniqueId(String uniqueId) {
		this(uniqueId == null ? new String[] { "" } : uniqueId.split(DELIMITER));
	}

	private UniqueId(String[] uniqueIdSegments) {
		this.uniqueIdSegments = uniqueIdSegments;
	}

	public boolean isEmpty() {
		return this.uniqueIdSegments.length == 1 && StringUtils.isBlank(this.uniqueIdSegments[0]);
	}

	public UniqueId getRemainder() {
		return new UniqueId(stream(this.uniqueIdSegments).skip(1).collect(joining(DELIMITER)));
	}

	@Override
	public String toString() {
		return stream(this.uniqueIdSegments).collect(joining(DELIMITER));
	}

	public String currentKey() {
		if (isEmpty()) {
			return "";
		}
		return extractKey(this.uniqueIdSegments[0]);
	}

	public String currentValue() {
		if (isEmpty()) {
			return "";
		}

		String part = this.uniqueIdSegments[0];
		return extractValue(part);
	}

	public UniqueId append(String key, String value) {
		UniqueId appender = from(key, value);
		return this.append(appender);
	}

	public UniqueId append(UniqueId appender) {
		if (this.isEmpty()) {
			return appender;
		}
		else {
			LinkedList<String> segments = new LinkedList<>();
			segments.addAll(Arrays.asList(this.uniqueIdSegments));
			segments.addAll(Arrays.asList(appender.uniqueIdSegments));
			return new UniqueId(segments.toArray(new String[segments.size()]));
		}
	}

	public int numberOfSegments() {
		return isEmpty() ? 0 : uniqueIdSegments.length;
	}

	private static String extractKey(String part) {
		String key = part.split(SPLITTER)[0];
		return key.substring(BEGINNING.length());
	}

	private static String extractValue(String part) {
		String value = part.split(SPLITTER)[1];
		return value.substring(0, value.length() - ENDING.length());
	}
}
