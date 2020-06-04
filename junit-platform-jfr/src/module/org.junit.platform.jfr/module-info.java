/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

/**
 * Provides the JUnit Platform Flight Recording Listener.
 *
 * <p>The Flight Recording Listener is a {@link TestExecutionListener} that
 * generates Java Flight Recorder events.
 *
 * @see <a href="https://openjdk.java.net/jeps/328">JEP 328: Flight Recorder</a>
 * @since 1.7
 */
module org.junit.platform.jfr {
	requires jdk.jfr;
	requires org.apiguardian.api;
	requires org.junit.platform.engine;
	requires org.junit.platform.launcher;

	provides org.junit.platform.launcher.TestExecutionListener
			with org.junit.platform.jfr.FlightRecordingListener;
}
