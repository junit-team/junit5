/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.platform.commons.logging.Logger;

/**
 * @since 4.12
 */
public class RecordCollectingLogger implements Logger {

	private final List<LogRecord> logRecords = new ArrayList<>();

	public List<LogRecord> getLogRecords() {
		return logRecords;
	}

	@Override
	public void error(Supplier<String> messageSupplier) {
		logRecords.add(new LogRecord(Level.SEVERE, messageSupplier.get()));
	}

	@Override
	public void error(Throwable throwable, Supplier<String> messageSupplier) {
		LogRecord logRecord = new LogRecord(Level.SEVERE, messageSupplier.get());
		logRecord.setThrown(throwable);
		logRecords.add(logRecord);
	}

	@Override
	public void warn(Supplier<String> messageSupplier) {
		logRecords.add(new LogRecord(Level.WARNING, messageSupplier.get()));
	}

	@Override
	public void warn(Throwable throwable, Supplier<String> messageSupplier) {
		LogRecord logRecord = new LogRecord(Level.WARNING, messageSupplier.get());
		logRecord.setThrown(throwable);
		logRecords.add(logRecord);
	}

	@Override
	public void info(Supplier<String> messageSupplier) {
		logRecords.add(new LogRecord(Level.INFO, messageSupplier.get()));
	}

	@Override
	public void info(Throwable throwable, Supplier<String> messageSupplier) {
		LogRecord logRecord = new LogRecord(Level.INFO, messageSupplier.get());
		logRecord.setThrown(throwable);
		logRecords.add(logRecord);
	}

	@Override
	public void config(Supplier<String> messageSupplier) {
		logRecords.add(new LogRecord(Level.CONFIG, messageSupplier.get()));
	}

	@Override
	public void config(Throwable throwable, Supplier<String> messageSupplier) {
		LogRecord logRecord = new LogRecord(Level.CONFIG, messageSupplier.get());
		logRecord.setThrown(throwable);
		logRecords.add(logRecord);
	}

	@Override
	public void debug(Supplier<String> messageSupplier) {
		logRecords.add(new LogRecord(Level.FINE, messageSupplier.get()));
	}

	@Override
	public void debug(Throwable throwable, Supplier<String> messageSupplier) {
		LogRecord logRecord = new LogRecord(Level.FINE, messageSupplier.get());
		logRecord.setThrown(throwable);
		logRecords.add(logRecord);
	}

	@Override
	public void trace(Supplier<String> messageSupplier) {
		logRecords.add(new LogRecord(Level.FINER, messageSupplier.get()));
	}

	@Override
	public void trace(Throwable throwable, Supplier<String> messageSupplier) {
		LogRecord logRecord = new LogRecord(Level.FINER, messageSupplier.get());
		logRecord.setThrown(throwable);
		logRecords.add(logRecord);
	}

}
