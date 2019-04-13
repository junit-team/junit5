/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

module org.junit.platform.reporting {
	requires java.xml;
	requires transitive org.apiguardian.api;
	requires transitive org.junit.platform.launcher;

	// exports org.junit.platform.reporting; empty package
	exports org.junit.platform.reporting.legacy.xml;
}
