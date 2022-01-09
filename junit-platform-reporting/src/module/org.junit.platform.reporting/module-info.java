/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

/**
 * Defines the JUnit Platform Reporting API.
 *
 * @since 1.4
 */
module org.junit.platform.reporting {
	requires java.xml;
	requires static transitive org.apiguardian.api;
	requires org.junit.platform.commons;
	requires transitive org.junit.platform.engine;
	requires transitive org.junit.platform.launcher;

	// exports org.junit.platform.reporting; empty package
	exports org.junit.platform.reporting.legacy;
	exports org.junit.platform.reporting.legacy.xml;
	exports org.junit.platform.reporting.open.xml;

	provides org.junit.platform.launcher.TestExecutionListener
			with org.junit.platform.reporting.open.xml.OpenTestReportGeneratingListener;
}
