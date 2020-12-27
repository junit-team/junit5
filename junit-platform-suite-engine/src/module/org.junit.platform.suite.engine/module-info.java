/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

module org.junit.platform.suite.engine {
	requires org.apiguardian.api;
	requires org.junit.platform.suite.api;
	requires org.junit.platform.commons;
	requires org.junit.platform.engine;
	requires org.junit.platform.launcher;

	provides org.junit.platform.engine.TestEngine
			with org.junit.platform.suite.engine.SuiteTestEngine;
}
