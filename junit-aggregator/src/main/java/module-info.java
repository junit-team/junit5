/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

/**
 * JUnit aggregator module for writing and running tests.
 */
module org.junit.aggregator {

	requires static transitive org.apiguardian.api;
	requires static transitive org.jspecify;

	requires transitive org.junit.jupiter;
	requires transitive org.junit.platform.launcher;
	requires org.junit.platform.console;

	exports org.junit.aggregator;

	uses java.util.spi.ToolProvider;

}
