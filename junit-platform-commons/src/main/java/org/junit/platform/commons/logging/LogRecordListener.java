/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.logging;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.LogRecord;

import org.apiguardian.api.API;

/**
 * {@code LogRecordListener} is only intended for testing purposes within
 * JUnit's own test suite.
 *
 * @since 1.1
 */
@API(status = INTERNAL, since = "1.0")
public class LogRecordListener {

	private final List<LogRecord> logRecords = new ArrayList<>();

	/**
	 * Inform the listener of a {@link LogRecord} that was submitted to JUL for
	 * processing.
	 */
	public void logRecordSubmitted(LogRecord logRecord) {
		this.logRecords.add(logRecord);
	}

	/**
	 * Get the list of {@link LogRecord log records} that have been
	 * {@linkplain #logRecordSubmitted submitted} to this listener.
	 *
	 * <p>As stated in the JavaDoc for {@code LogRecord}, a submitted
	 * {@code LogRecord} should not be updated by the client application. Thus,
	 * the {@code LogRecords} in the returned list should only be inspected for
	 * testing purposes and not modified in any way.
	 */
	public List<LogRecord> getLogRecords() {
		return this.logRecords;
	}

}
