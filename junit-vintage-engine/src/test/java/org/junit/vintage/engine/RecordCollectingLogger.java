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

import static java.util.logging.Level.CONFIG;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

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
		return this.logRecords;
	}

	@Override
	public void error(Supplier<String> messageSupplier) {
		log(SEVERE, messageSupplier);
	}

	@Override
	public void error(Throwable throwable, Supplier<String> messageSupplier) {
		log(SEVERE, throwable, messageSupplier);
	}

	@Override
	public void warn(Supplier<String> messageSupplier) {
		log(WARNING, messageSupplier);
	}

	@Override
	public void warn(Throwable throwable, Supplier<String> messageSupplier) {
		log(WARNING, throwable, messageSupplier);
	}

	@Override
	public void info(Supplier<String> messageSupplier) {
		log(INFO, messageSupplier);
	}

	@Override
	public void info(Throwable throwable, Supplier<String> messageSupplier) {
		log(INFO, throwable, messageSupplier);
	}

	@Override
	public void config(Supplier<String> messageSupplier) {
		log(CONFIG, messageSupplier);
	}

	@Override
	public void config(Throwable throwable, Supplier<String> messageSupplier) {
		log(CONFIG, throwable, messageSupplier);
	}

	@Override
	public void debug(Supplier<String> messageSupplier) {
		log(FINE, messageSupplier);
	}

	@Override
	public void debug(Throwable throwable, Supplier<String> messageSupplier) {
		log(FINE, throwable, messageSupplier);
	}

	@Override
	public void trace(Supplier<String> messageSupplier) {
		log(FINER, messageSupplier);
	}

	@Override
	public void trace(Throwable throwable, Supplier<String> messageSupplier) {
		log(FINER, throwable, messageSupplier);
	}

	private void log(Level level, Supplier<String> messageSupplier) {
		log(level, null, messageSupplier);
	}

	private void log(Level level, Throwable throwable, Supplier<String> messageSupplier) {
		LogRecord logRecord = new LogRecord(level, nullSafeGet(messageSupplier));
		logRecord.setLoggerName(getClass().getName());
		logRecord.setThrown(throwable);

		this.logRecords.add(logRecord);
	}

	private static String nullSafeGet(Supplier<String> messageSupplier) {
		return (messageSupplier != null ? messageSupplier.get() : null);
	}

}
