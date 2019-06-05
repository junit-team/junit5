/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example;

// tag::user_guide[]

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.File;
import java.net.URI;
import java.nio.file.Paths;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

// end::user_guide[]
// @formatter:off
// tag::user_guide[]
public class TestSourceUrisDemo {

	@TestFactory
	DynamicTest classpathResourceSource() {
		return dynamicTest(
				"class path resource source uri",
				URI.create("classpath:/two-column.csv"), // test source uri
				() -> assertTrue(true)); // for demo purpose
	}

	@TestFactory
	DynamicTest directorySource() {
		File directory = Paths.get("src/test/resources").toFile();
		return dynamicTest("directory source uri",
				directory.toURI(), // test source uri
				() -> assertTrue(true)); // for demo purpose
	}

	// Inspired by https://sormuras.github.io/blog/2018-09-05-junit-5.3-dynamic-test-source.html
	@TestFactory
	DynamicTest fileSource() throws Exception {
		File file = Paths.get("src/test/resources/two-column.csv").toFile();
		return dynamicTest(file.getName(),
				file.toURI(), // test source uri
				() -> assertTrue(true)); // for demo purpose
	}

	@TestFactory
	DynamicTest methodSource() {
		return dynamicTest("method source uri",
				URI.create("method:example.TestSourceUrisDemo#method()"), // test source uri
				() -> assertTrue(true)); // for demo purpose
	}

	private void method() {
	}
}
// end::user_guide[]
