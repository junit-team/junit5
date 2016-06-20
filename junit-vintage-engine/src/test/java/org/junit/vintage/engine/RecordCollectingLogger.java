/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.vintage.engine;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * @since 5.0
 */
public class RecordCollectingLogger extends Logger {

	private final List<LogRecord> logRecords = new LinkedList<>();

	public RecordCollectingLogger() {
		super("RecordCollectingLogger", null);
	}

	@Override
	public void log(LogRecord record) {
		logRecords.add(record);
	}

	public List<LogRecord> getLogRecords() {
		return logRecords;
	}

}
