/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
module org.junit.platform.commons {

	requires static java.compiler; // usage of `javax.lang.model.SourceVersion` in `PackageUtils`
	requires java.logging;
	requires org.apiguardian.api;

	//
	// Public API of this module.
	//
	exports org.junit.platform.commons;
	exports org.junit.platform.commons.annotation;
	exports org.junit.platform.commons.support;

	//
	// Make internal packages only visible to other JUnit modules.
	//
	exports org.junit.platform.commons.logging to
			org.junit.platform.engine,
			org.junit.platform.console,
			org.junit.platform.launcher,
			org.junit.jupiter.engine,
			org.junit.vintage.engine;

	exports org.junit.platform.commons.util to
			org.junit.platform.engine,
			org.junit.platform.console,
			org.junit.platform.launcher,
			org.junit.jupiter.api,
			org.junit.jupiter.engine,
			org.junit.vintage.engine;
}
