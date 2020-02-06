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
 * Common APIs and support utilities for the JUnit Platform.
 *
 * @since 1.0
 */
module org.junit.platform.commons {
	requires java.logging;
	requires java.management; // needed by RuntimeUtils to determine input arguments
	requires transitive org.apiguardian.api;

	exports org.junit.platform.commons;
	exports org.junit.platform.commons.annotation;
	exports org.junit.platform.commons.function;
	exports org.junit.platform.commons.logging to
			org.junit.jupiter.api,
			org.junit.jupiter.engine,
			org.junit.jupiter.migrationsupport,
			org.junit.jupiter.params,
			org.junit.platform.console,
			org.junit.platform.engine,
			org.junit.platform.launcher,
			org.junit.platform.reporting,
			org.junit.platform.runner,
			org.junit.platform.suite.api,
			org.junit.platform.testkit,
			org.junit.vintage.engine;
	exports org.junit.platform.commons.support;
	exports org.junit.platform.commons.util to
			org.junit.jupiter.api,
			org.junit.jupiter.engine,
			org.junit.jupiter.migrationsupport,
			org.junit.jupiter.params,
			org.junit.platform.console,
			org.junit.platform.engine,
			org.junit.platform.launcher,
			org.junit.platform.reporting,
			org.junit.platform.runner,
			org.junit.platform.suite.api,
			org.junit.platform.testkit,
			org.junit.vintage.engine;
}
