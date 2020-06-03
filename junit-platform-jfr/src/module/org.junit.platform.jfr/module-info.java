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
 * Defines the JUnit Platform JAR API.
 *
 * @since 1.7
 */
module org.junit.platform.jfr {
	requires jdk.jfr;
	requires transitive org.apiguardian.api;
	requires org.junit.platform.commons;
	requires transitive org.junit.platform.launcher;

	provides org.junit.platform.launcher.TestExecutionListener with
			org.junit.platform.jfr.FlightRecordingListener;
}
