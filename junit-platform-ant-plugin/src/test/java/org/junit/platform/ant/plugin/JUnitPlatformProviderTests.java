/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.ant.plugin;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.types.Path;
import org.junit.jupiter.api.Test;
import org.junit.platform.ant.plugin.Filters.FilterSet;
import org.junit.platform.console.options.CommandLineOptionsParser;
import org.mockito.ArgumentCaptor;

/**
 * Unit tests for {@link JUnitPlatformPlugin}.
 *
 * @since 1.0
 */
class JUnitPlatformProviderTests {

	@Test
	void generalPurposeArguments() throws Exception {
		CommandLineOptionsParser commandLineOptionsParser = mock(CommandLineOptionsParser.class);
		JUnitPlatformPlugin junitAntTask = new JUnitPlatformPlugin(commandLineOptionsParser);

		Path classpath = new Path(new Project());
		classpath.setPath("dummy/path");
		junitAntTask.addClasspath(classpath);
		junitAntTask.setReportsDir("reportsDirectory");
		junitAntTask.execute();

		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		verify(commandLineOptionsParser).parse(argument.capture());
		List<String> actualListValues = argument.getAllValues();

		assertAll(() -> assertEquals("--classpath", actualListValues.get(0)),
			() -> assertEquals(classpath.toString(), actualListValues.get(1)),
			() -> assertEquals("--reports-dir", actualListValues.get(2)),
			() -> assertEquals("reportsDirectory", actualListValues.get(3)));
	}

	@Test
	void selectorSingleValue() throws Exception {
		CommandLineOptionsParser commandLineOptionsParser = mock(CommandLineOptionsParser.class);
		JUnitPlatformPlugin junitAntTask = new JUnitPlatformPlugin(commandLineOptionsParser);

		Selectors selectors = new Selectors();

		Path scanClasspath = new Path(new Project());
		scanClasspath.setPath("dummy/path1");
		selectors.addClasspath(scanClasspath);

		selectors.setUri("u:foo");
		selectors.setFile("qux.json");
		selectors.setDirectory("qux/bar");
		selectors.setPackage("com.acme.foo");
		selectors.setClass("com.acme.foo.FooTestCase");
		selectors.setMethod("com.acme.foo.FooTestCase#alwaysEquals");
		selectors.setResource("/com/acme/my.properties");

		junitAntTask.addSelectors(selectors);
		junitAntTask.execute();

		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		verify(commandLineOptionsParser).parse(argument.capture());
		List<String> actualListValues = argument.getAllValues();

		assertAll(() -> assertEquals("--scan-classpath", actualListValues.get(0)),
			() -> assertEquals(scanClasspath.toString(), actualListValues.get(1)),
			() -> assertEquals("--select-uri", actualListValues.get(2)),
			() -> assertEquals("u:foo", actualListValues.get(3)),
			() -> assertEquals("--select-file", actualListValues.get(4)),
			() -> assertEquals("qux.json", actualListValues.get(5)),
			() -> assertEquals("--select-directory", actualListValues.get(6)),
			() -> assertEquals("qux/bar", actualListValues.get(7)),
			() -> assertEquals("--select-package", actualListValues.get(8)),
			() -> assertEquals("com.acme.foo", actualListValues.get(9)),
			() -> assertEquals("--select-class", actualListValues.get(10)),
			() -> assertEquals("com.acme.foo.FooTestCase", actualListValues.get(11)),
			() -> assertEquals("--select-method", actualListValues.get(12)),
			() -> assertEquals("com.acme.foo.FooTestCase#alwaysEquals", actualListValues.get(13)),
			() -> assertEquals("--select-resource", actualListValues.get(14)),
			() -> assertEquals("/com/acme/my.properties", actualListValues.get(15)));
	}

	@Test
	void selectorMultipleValues() throws Exception {
		CommandLineOptionsParser commandLineOptionsParser = mock(CommandLineOptionsParser.class);
		JUnitPlatformPlugin junitAntTask = new JUnitPlatformPlugin(commandLineOptionsParser);

		Selectors selectors = new Selectors();

		Path scanClasspath = new Path(new Project());
		scanClasspath.setPath("dummy/path1:dummy/path2");
		selectors.addClasspath(scanClasspath);

		selectors.setUris("u:foo, u:bar");
		selectors.setFile("foo.txt,bar.csv");
		selectors.setDirectories("foo/bar,bar/qux");
		selectors.setPackages("com.acme.foo,com.acme.bar");
		selectors.setClasses("com.acme.foo.FooTestCase, com.acme.bar.BarTestCase");
		selectors.setMethods("com.acme.foo.FooTestCase#alwaysEquals , " + "com.acme.bar.BarTestCase#alwaysNotEquals");
		selectors.setResources("/bar.csv,/foo/input.json");

		junitAntTask.addSelectors(selectors);
		junitAntTask.execute();

		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		verify(commandLineOptionsParser).parse(argument.capture());
		List<String> actualListValues = argument.getAllValues();

		assertAll(() -> assertEquals("--scan-classpath", actualListValues.get(0)),
			() -> assertEquals(scanClasspath.toString(), actualListValues.get(1)),
			() -> assertEquals("--select-uri", actualListValues.get(2)),
			() -> assertEquals("u:foo", actualListValues.get(3)),
			() -> assertEquals("--select-uri", actualListValues.get(4)),
			() -> assertEquals("u:bar", actualListValues.get(5)),
			() -> assertEquals("--select-file", actualListValues.get(6)),
			() -> assertEquals("foo.txt", actualListValues.get(7)),
			() -> assertEquals("--select-file", actualListValues.get(8)),
			() -> assertEquals("bar.csv", actualListValues.get(9)),
			() -> assertEquals("--select-directory", actualListValues.get(10)),
			() -> assertEquals("foo/bar", actualListValues.get(11)),
			() -> assertEquals("--select-directory", actualListValues.get(12)),
			() -> assertEquals("bar/qux", actualListValues.get(13)),
			() -> assertEquals("--select-package", actualListValues.get(14)),
			() -> assertEquals("com.acme.foo", actualListValues.get(15)),
			() -> assertEquals("--select-package", actualListValues.get(16)),
			() -> assertEquals("com.acme.bar", actualListValues.get(17)),
			() -> assertEquals("--select-class", actualListValues.get(18)),
			() -> assertEquals("com.acme.foo.FooTestCase", actualListValues.get(19)),
			() -> assertEquals("--select-class", actualListValues.get(20)),
			() -> assertEquals("com.acme.bar.BarTestCase", actualListValues.get(21)),
			() -> assertEquals("--select-method", actualListValues.get(22)),
			() -> assertEquals("com.acme.foo.FooTestCase#alwaysEquals", actualListValues.get(23)),
			() -> assertEquals("--select-method", actualListValues.get(24)),
			() -> assertEquals("com.acme.bar.BarTestCase#alwaysNotEquals", actualListValues.get(25)),
			() -> assertEquals("--select-resource", actualListValues.get(26)),
			() -> assertEquals("/bar.csv", actualListValues.get(27)),
			() -> assertEquals("--select-resource", actualListValues.get(28)),
			() -> assertEquals("/foo/input.json", actualListValues.get(29)));
	}

	@Test
	void filterSingleValue() throws Exception {
		CommandLineOptionsParser commandLineOptionsParser = mock(CommandLineOptionsParser.class);
		JUnitPlatformPlugin junitAntTask = new JUnitPlatformPlugin(commandLineOptionsParser);

		Filters filters = new Filters();
		filters.setIncludeClassNamePatterns(".*TestCase");
		FilterSet packages = filters.createPackages();
		packages.setInclude("testpackage.included.p1");
		packages.setExclude("testpackage.excluded.p1");
		FilterSet engines = filters.createEngines();
		engines.setInclude("foo");
		engines.setExclude("bar");
		FilterSet tags = filters.createTags();
		tags.setInclude("fast");
		tags.setExclude("slow");

		junitAntTask.addFilters(filters);
		junitAntTask.execute();

		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		verify(commandLineOptionsParser).parse(argument.capture());
		List<String> actualListValues = argument.getAllValues();

		assertAll(() -> assertEquals("--include-classname", actualListValues.get(0)),
			() -> assertEquals(".*TestCase", actualListValues.get(1)),
			() -> assertEquals("--include-package", actualListValues.get(2)),
			() -> assertEquals("testpackage.included.p1", actualListValues.get(3)),
			() -> assertEquals("--exclude-package", actualListValues.get(4)),
			() -> assertEquals("testpackage.excluded.p1", actualListValues.get(5)),
			() -> assertEquals("--include-engine", actualListValues.get(6)),
			() -> assertEquals("foo", actualListValues.get(7)),
			() -> assertEquals("--exclude-engine", actualListValues.get(8)),
			() -> assertEquals("bar", actualListValues.get(9)),
			() -> assertEquals("--include-tag", actualListValues.get(10)),
			() -> assertEquals("fast", actualListValues.get(11)),
			() -> assertEquals("--exclude-tag", actualListValues.get(12)),
			() -> assertEquals("slow", actualListValues.get(13)));
	}

	@Test
	void filterMultipleValue() throws Exception {
		CommandLineOptionsParser commandLineOptionsParser = mock(CommandLineOptionsParser.class);
		JUnitPlatformPlugin junitAntTask = new JUnitPlatformPlugin(commandLineOptionsParser);

		Filters filters = new Filters();
		filters.setIncludeClassNamePatterns(".*TestCase, .*Test");

		FilterSet packages = filters.createPackages();
		packages.setInclude("testpackage.included.p1,testpackage.included.p2");
		packages.setExclude("testpackage.excluded.p1,testpackage.excluded.p2");
		FilterSet engines = filters.createEngines();
		engines.setInclude("foo1,foo2");
		engines.setExclude("bar1,bar2");
		FilterSet tags = filters.createTags();
		tags.setInclude("fast1,fast2");
		tags.setExclude("slow1,slow2");

		junitAntTask.addFilters(filters);
		junitAntTask.execute();

		ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
		verify(commandLineOptionsParser).parse(argument.capture());
		List<String> actualListValues = argument.getAllValues();

		assertAll(() -> assertEquals("--include-classname", actualListValues.get(0)),
			() -> assertEquals(".*TestCase", actualListValues.get(1)),
			() -> assertEquals("--include-classname", actualListValues.get(2)),
			() -> assertEquals(".*Test", actualListValues.get(3)),
			() -> assertEquals("--include-package", actualListValues.get(4)),
			() -> assertEquals("testpackage.included.p1", actualListValues.get(5)),
			() -> assertEquals("--include-package", actualListValues.get(6)),
			() -> assertEquals("testpackage.included.p2", actualListValues.get(7)),
			() -> assertEquals("--exclude-package", actualListValues.get(8)),
			() -> assertEquals("testpackage.excluded.p1", actualListValues.get(9)),
			() -> assertEquals("--exclude-package", actualListValues.get(10)),
			() -> assertEquals("testpackage.excluded.p2", actualListValues.get(11)),
			() -> assertEquals("--include-engine", actualListValues.get(12)),
			() -> assertEquals("foo1", actualListValues.get(13)),
			() -> assertEquals("--include-engine", actualListValues.get(14)),
			() -> assertEquals("foo2", actualListValues.get(15)),
			() -> assertEquals("--exclude-engine", actualListValues.get(16)),
			() -> assertEquals("bar1", actualListValues.get(17)),
			() -> assertEquals("--exclude-engine", actualListValues.get(18)),
			() -> assertEquals("bar2", actualListValues.get(19)),
			() -> assertEquals("--include-tag", actualListValues.get(20)),
			() -> assertEquals("fast1", actualListValues.get(21)),
			() -> assertEquals("--include-tag", actualListValues.get(22)),
			() -> assertEquals("fast2", actualListValues.get(23)),
			() -> assertEquals("--exclude-tag", actualListValues.get(24)),
			() -> assertEquals("slow1", actualListValues.get(25)),
			() -> assertEquals("--exclude-tag", actualListValues.get(26)),
			() -> assertEquals("slow2", actualListValues.get(27)));
	}
}
