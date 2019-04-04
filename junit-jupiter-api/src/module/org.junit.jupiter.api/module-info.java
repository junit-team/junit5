/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

module org.junit.jupiter.api {
	requires org.apiguardian.api;
	requires org.opentest4j;

	requires transitive org.junit.platform.commons;

	exports org.junit.jupiter.api;
	exports org.junit.jupiter.api.condition;
	exports org.junit.jupiter.api.extension;
	exports org.junit.jupiter.api.function;
	exports org.junit.jupiter.api.io;
	exports org.junit.jupiter.api.parallel;
}
