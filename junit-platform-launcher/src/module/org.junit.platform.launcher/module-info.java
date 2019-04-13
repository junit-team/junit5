/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

module org.junit.platform.launcher {
	requires transitive org.apiguardian.api;
	requires transitive org.junit.platform.engine;

	exports org.junit.platform.launcher;
	exports org.junit.platform.launcher.core;
	exports org.junit.platform.launcher.listeners;

	uses org.junit.platform.engine.TestEngine;
	uses org.junit.platform.launcher.TestExecutionListener;
}
