/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.options;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.ClassNameFilter.STANDARD_INCLUDE_PATTERN;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.JUnitException;

/**
 * @since 1.0
 */
class JOptSimpleCommandLineOptionsParserTests {

	@Test
	void parseNoArguments() {
		String[] noArguments = {};
		CommandLineOptions options = createParser().parse(noArguments);

		// @formatter:off
		assertAll(
			() -> assertFalse(options.isAnsiColorOutputDisabled()),
			() -> assertFalse(options.isDisplayHelp()),
			() -> assertEquals(CommandLineOptions.DEFAULT_DETAILS, options.getDetails()),
			() -> assertFalse(options.isScanClasspath()),
			() -> assertEquals(singletonList(STANDARD_INCLUDE_PATTERN), options.getIncludedClassNamePatterns()),
			() -> assertEquals(emptyList(), options.getExcludedClassNamePatterns()),
			() -> assertEquals(emptyList(), options.getIncludedPackages()),
			() -> assertEquals(emptyList(), options.getExcludedPackages()),
			() -> assertEquals(emptyList(), options.getIncludedTags()),
			() -> assertEquals(emptyList(), options.getExcludedTags()),
			() -> assertEquals(emptyList(), options.getAdditionalClasspathEntries()),
			() -> assertEquals(Optional.empty(), options.getReportsDir()),
			() -> assertEquals(emptyList(), options.getSelectedUris()),
			() -> assertEquals(emptyList(), options.getSelectedFiles()),
			() -> assertEquals(emptyList(), options.getSelectedModules()),
			() -> assertEquals(emptyList(), options.getSelectedPackages()),
			() -> assertEquals(emptyList(), options.getSelectedMethods()),
			() -> assertEquals(emptyList(), options.getSelectedDirectories()),
			() -> assertEquals(emptyList(), options.getSelectedClasspathEntries()),
			() -> assertEquals(emptyMap(), options.getConfigurationParameters())
		);
		// @formatter:on
	}

	@Test
	void parseSwitches() {
		// @formatter:off
		assertAll(
			() -> assertParses("disable ansi", CommandLineOptions::isAnsiColorOutputDisabled, "--disable-ansi-colors"),
			() -> assertParses("help", CommandLineOptions::isDisplayHelp, "-h", "--help"),
			() -> assertParses("scan class path", CommandLineOptions::isScanClasspath, "--scan-class-path")
		);
		// @formatter:on
	}

	@Test
	void parseValidDetails() {
		// @formatter:off
		assertAll(
			() -> assertEquals(Details.VERBOSE, parseArgLine("--details verbose").getDetails()),
			() -> assertEquals(Details.TREE, parseArgLine("--details tree").getDetails()),
			() -> assertEquals(Details.FLAT, parseArgLine("--details flat").getDetails()),
			() -> assertEquals(Details.NONE, parseArgLine("--details NONE").getDetails()),
			() -> assertEquals(Details.NONE, parseArgLine("--details none").getDetails()),
			() -> assertEquals(Details.NONE, parseArgLine("--details None").getDetails())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidDetails() throws Exception {
		assertOptionWithMissingRequiredArgumentThrowsException("--details");
	}

	@Test
	void parseValidDetailsTheme() {
		// @formatter:off
		assertAll(
			() -> assertEquals(Theme.ASCII, parseArgLine("--details-theme ascii").getTheme()),
			() -> assertEquals(Theme.ASCII, parseArgLine("--details-theme ASCII").getTheme()),
			() -> assertEquals(Theme.UNICODE, parseArgLine("--details-theme unicode").getTheme()),
			() -> assertEquals(Theme.UNICODE, parseArgLine("--details-theme UNICODE").getTheme()),
			() -> assertEquals(Theme.UNICODE, parseArgLine("--details-theme uniCode").getTheme())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidDetailsTheme() throws Exception {
		assertOptionWithMissingRequiredArgumentThrowsException("--details-theme");
	}

	@Test
	void parseValidIncludeClassNamePatterns() {
		// @formatter:off
		assertAll(
			() -> assertEquals(singletonList(".*Test"), parseArgLine("-n .*Test").getIncludedClassNamePatterns()),
			() -> assertEquals(asList(".*Test", ".*Tests"), parseArgLine("--include-classname .*Test --include-classname .*Tests").getIncludedClassNamePatterns()),
			() -> assertEquals(singletonList(".*Test"), parseArgLine("--include-classname=.*Test").getIncludedClassNamePatterns())
		);
		// @formatter:on
	}

	@Test
	void parseValidExcludeClassNamePatterns() {
		// @formatter:off
		assertAll(
			() -> assertEquals(singletonList(".*Test"), parseArgLine("-N .*Test").getExcludedClassNamePatterns()),
			() -> assertEquals(asList(".*Test", ".*Tests"), parseArgLine("--exclude-classname .*Test --exclude-classname .*Tests").getExcludedClassNamePatterns()),
			() -> assertEquals(singletonList(".*Test"), parseArgLine("--exclude-classname=.*Test").getExcludedClassNamePatterns())
		);
		// @formatter:on
	}

	@Test
	void usesDefaultClassNamePatternWithoutExplicitArgument() {
		assertEquals(singletonList(STANDARD_INCLUDE_PATTERN), parseArgLine("").getIncludedClassNamePatterns());
	}

	@Test
	void parseInvalidIncludeClassNamePatterns() throws Exception {
		assertOptionWithMissingRequiredArgumentThrowsException("-n", "--include-classname");
	}

	@Test
	void parseInvalidExcludeClassNamePatterns() throws Exception {
		assertOptionWithMissingRequiredArgumentThrowsException("-N", "--exclude-classname");
	}

	@Test
	void parseValidIncludedPackages() {
		// @formatter:off
		assertAll(
				() -> assertEquals(asList("org.junit.included"),
						parseArgLine("--include-package org.junit.included").getIncludedPackages()),
				() -> assertEquals(asList("org.junit.included"),
						parseArgLine("--include-package=org.junit.included").getIncludedPackages()),
				() -> assertEquals(asList("org.junit.included1", "org.junit.included2"),
						parseArgLine("--include-package org.junit.included1 --include-package org.junit.included2").getIncludedPackages())
		);
		// @formatter:on
	}

	@Test
	void parseValidExcludedPackages() {
		// @formatter:off
		assertAll(
				() -> assertEquals(asList("org.junit.excluded"),
						parseArgLine("--exclude-package org.junit.excluded").getExcludedPackages()),
				() -> assertEquals(asList("org.junit.excluded"),
						parseArgLine("--exclude-package=org.junit.excluded").getExcludedPackages()),
				() -> assertEquals(asList("org.junit.excluded1", "org.junit.excluded2"),
						parseArgLine("--exclude-package org.junit.excluded1 --exclude-package org.junit.excluded2").getExcludedPackages())
		);
		// @formatter:on
	}

	@Test
	void parseValidIncludedTags() {
		// @formatter:off
		assertAll(
			() -> assertEquals(asList("fast"), parseArgLine("-t fast").getIncludedTags()),
			() -> assertEquals(asList("fast"), parseArgLine("--include-tag fast").getIncludedTags()),
			() -> assertEquals(asList("fast"), parseArgLine("--include-tag=fast").getIncludedTags()),
			() -> assertEquals(asList("fast", "slow"), parseArgLine("-t fast -t slow").getIncludedTags())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidIncludedTags() {
		assertOptionWithMissingRequiredArgumentThrowsException("-t", "--include-tag");
	}

	@Test
	void parseValidExcludedTags() {
		// @formatter:off
		assertAll(
			() -> assertEquals(asList("fast"), parseArgLine("-T fast").getExcludedTags()),
			() -> assertEquals(asList("fast"), parseArgLine("--exclude-tag fast").getExcludedTags()),
			() -> assertEquals(asList("fast"), parseArgLine("--exclude-tag=fast").getExcludedTags()),
			() -> assertEquals(asList("fast", "slow"), parseArgLine("-T fast -T slow").getExcludedTags())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidExcludedTags() {
		assertOptionWithMissingRequiredArgumentThrowsException("-T", "--exclude-tag");
	}

	@Test
	void parseValidIncludedEngines() {
		// @formatter:off
		assertAll(
			() -> assertEquals(asList("junit-jupiter"), parseArgLine("-e junit-jupiter").getIncludedEngines()),
			() -> assertEquals(asList("junit-vintage"), parseArgLine("--include-engine junit-vintage").getIncludedEngines()),
			() -> assertEquals(emptyList(), parseArgLine("").getIncludedEngines())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidIncludedEngines() throws Exception {
		assertOptionWithMissingRequiredArgumentThrowsException("-e", "--include-engine");
	}

	@Test
	void parseValidExcludedEngines() {
		// @formatter:off
		assertAll(
			() -> assertEquals(asList("junit-jupiter"), parseArgLine("-E junit-jupiter").getExcludedEngines()),
			() -> assertEquals(asList("junit-vintage"), parseArgLine("--exclude-engine junit-vintage").getExcludedEngines()),
			() -> assertEquals(emptyList(), parseArgLine("").getExcludedEngines())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidExcludedEngines() throws Exception {
		assertOptionWithMissingRequiredArgumentThrowsException("-E", "--exclude-engine");
	}

	@Test
	void parseValidAdditionalClasspathEntries() {
		Path dir = Paths.get(".");
		// @formatter:off
		assertAll(
			() -> assertEquals(singletonList(dir), parseArgLine("-cp .").getAdditionalClasspathEntries()),
			() -> assertEquals(singletonList(dir), parseArgLine("--cp .").getAdditionalClasspathEntries()),
			() -> assertEquals(singletonList(dir), parseArgLine("-classpath .").getAdditionalClasspathEntries()),
			() -> assertEquals(singletonList(dir), parseArgLine("-classpath=.").getAdditionalClasspathEntries()),
			() -> assertEquals(singletonList(dir), parseArgLine("--classpath .").getAdditionalClasspathEntries()),
			() -> assertEquals(singletonList(dir), parseArgLine("--classpath=.").getAdditionalClasspathEntries()),
			() -> assertEquals(singletonList(dir), parseArgLine("--class-path .").getAdditionalClasspathEntries()),
			() -> assertEquals(singletonList(dir), parseArgLine("--class-path=.").getAdditionalClasspathEntries()),
			() -> assertEquals(asList(dir, Paths.get("src", "test", "java")), parseArgLine("-cp . -cp src/test/java").getAdditionalClasspathEntries()),
			() -> assertEquals(asList(dir, Paths.get("src", "test", "java")), parseArgLine("-cp ." + File.pathSeparator + "src/test/java").getAdditionalClasspathEntries())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidAdditionalClasspathEntries() {
		assertOptionWithMissingRequiredArgumentThrowsException("-cp", "--classpath", "--class-path");
	}

	@Test
	void parseValidXmlReportsDirs() {
		Path dir = Paths.get("build", "test-results");
		// @formatter:off
		assertAll(
			() -> assertEquals(Optional.of(dir), parseArgLine("--reports-dir build/test-results").getReportsDir()),
			() -> assertEquals(Optional.of(dir), parseArgLine("--reports-dir=build/test-results").getReportsDir())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidXmlReportsDirs() throws Exception {
		assertOptionWithMissingRequiredArgumentThrowsException("--reports-dir");
	}

	@Test
	void parseValidUriSelectors() {
		// @formatter:off
		assertAll(
				() -> assertEquals(singletonList(new URI("file:///foo.txt")), parseArgLine("-u file:///foo.txt").getSelectedUris()),
				() -> assertEquals(singletonList(new URI("file:///foo.txt")), parseArgLine("--u file:///foo.txt").getSelectedUris()),
				() -> assertEquals(singletonList(new URI("file:///foo.txt")), parseArgLine("-select-uri file:///foo.txt").getSelectedUris()),
				() -> assertEquals(singletonList(new URI("file:///foo.txt")), parseArgLine("-select-uri=file:///foo.txt").getSelectedUris()),
				() -> assertEquals(singletonList(new URI("file:///foo.txt")), parseArgLine("--select-uri file:///foo.txt").getSelectedUris()),
				() -> assertEquals(singletonList(new URI("file:///foo.txt")), parseArgLine("--select-uri=file:///foo.txt").getSelectedUris()),
				() -> assertEquals(asList(new URI("file:///foo.txt"), new URI("http://localhost")), parseArgLine("-u file:///foo.txt -u http://localhost").getSelectedUris())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidUriSelectors() {
		assertOptionWithMissingRequiredArgumentThrowsException("-u", "--select-uri", "-u unknown-scheme:");
	}

	@Test
	void parseValidFileSelectors() {
		// @formatter:off
		assertAll(
				() -> assertEquals(singletonList("foo.txt"), parseArgLine("-f foo.txt").getSelectedFiles()),
				() -> assertEquals(singletonList("foo.txt"), parseArgLine("--f foo.txt").getSelectedFiles()),
				() -> assertEquals(singletonList("foo.txt"), parseArgLine("-select-file foo.txt").getSelectedFiles()),
				() -> assertEquals(singletonList("foo.txt"), parseArgLine("-select-file=foo.txt").getSelectedFiles()),
				() -> assertEquals(singletonList("foo.txt"), parseArgLine("--select-file foo.txt").getSelectedFiles()),
				() -> assertEquals(singletonList("foo.txt"), parseArgLine("--select-file=foo.txt").getSelectedFiles()),
				() -> assertEquals(asList("foo.txt", "bar.csv"), parseArgLine("-f foo.txt -f bar.csv").getSelectedFiles())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidFileSelectors() {
		assertOptionWithMissingRequiredArgumentThrowsException("-f", "--select-file");
	}

	@Test
	void parseValidDirectorySelectors() {
		// @formatter:off
		assertAll(
				() -> assertEquals(singletonList("foo/bar"), parseArgLine("-d foo/bar").getSelectedDirectories()),
				() -> assertEquals(singletonList("foo/bar"), parseArgLine("--d foo/bar").getSelectedDirectories()),
				() -> assertEquals(singletonList("foo/bar"), parseArgLine("-select-directory foo/bar").getSelectedDirectories()),
				() -> assertEquals(singletonList("foo/bar"), parseArgLine("-select-directory=foo/bar").getSelectedDirectories()),
				() -> assertEquals(singletonList("foo/bar"), parseArgLine("--select-directory foo/bar").getSelectedDirectories()),
				() -> assertEquals(singletonList("foo/bar"), parseArgLine("--select-directory=foo/bar").getSelectedDirectories()),
				() -> assertEquals(asList("foo/bar", "bar/qux"), parseArgLine("-d foo/bar -d bar/qux").getSelectedDirectories())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidDirectorySelectors() {
		assertOptionWithMissingRequiredArgumentThrowsException("-d", "--select-directory");
	}

	@Test
	void parseValidModuleSelectors() {
		// @formatter:off
		assertAll(
				() -> assertEquals(singletonList("com.acme.foo"), parseArgLine("-o com.acme.foo").getSelectedModules()),
				() -> assertEquals(singletonList("com.acme.foo"), parseArgLine("--o com.acme.foo").getSelectedModules()),
				() -> assertEquals(singletonList("com.acme.foo"), parseArgLine("-select-module com.acme.foo").getSelectedModules()),
				() -> assertEquals(singletonList("com.acme.foo"), parseArgLine("-select-module=com.acme.foo").getSelectedModules()),
				() -> assertEquals(singletonList("com.acme.foo"), parseArgLine("--select-module com.acme.foo").getSelectedModules()),
				() -> assertEquals(singletonList("com.acme.foo"), parseArgLine("--select-module=com.acme.foo").getSelectedModules()),
				() -> assertEquals(asList("com.acme.foo", "com.example.bar"), parseArgLine("-o com.acme.foo -o com.example.bar").getSelectedModules())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidModuleSelectors() {
		assertOptionWithMissingRequiredArgumentThrowsException("-o", "--select-module");
	}

	@Test
	void parseValidPackageSelectors() {
		// @formatter:off
		assertAll(
				() -> assertEquals(singletonList("com.acme.foo"), parseArgLine("-p com.acme.foo").getSelectedPackages()),
				() -> assertEquals(singletonList("com.acme.foo"), parseArgLine("--p com.acme.foo").getSelectedPackages()),
				() -> assertEquals(singletonList("com.acme.foo"), parseArgLine("-select-package com.acme.foo").getSelectedPackages()),
				() -> assertEquals(singletonList("com.acme.foo"), parseArgLine("-select-package=com.acme.foo").getSelectedPackages()),
				() -> assertEquals(singletonList("com.acme.foo"), parseArgLine("--select-package com.acme.foo").getSelectedPackages()),
				() -> assertEquals(singletonList("com.acme.foo"), parseArgLine("--select-package=com.acme.foo").getSelectedPackages()),
				() -> assertEquals(asList("com.acme.foo", "com.example.bar"), parseArgLine("-p com.acme.foo -p com.example.bar").getSelectedPackages())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidPackageSelectors() {
		assertOptionWithMissingRequiredArgumentThrowsException("-p", "--select-package");
	}

	@Test
	void parseValidClassSelectors() {
		// @formatter:off
		assertAll(
				() -> assertEquals(singletonList("com.acme.Foo"), parseArgLine("-c com.acme.Foo").getSelectedClasses()),
				() -> assertEquals(singletonList("com.acme.Foo"), parseArgLine("--c com.acme.Foo").getSelectedClasses()),
				() -> assertEquals(singletonList("com.acme.Foo"), parseArgLine("-select-class com.acme.Foo").getSelectedClasses()),
				() -> assertEquals(singletonList("com.acme.Foo"), parseArgLine("-select-class=com.acme.Foo").getSelectedClasses()),
				() -> assertEquals(singletonList("com.acme.Foo"), parseArgLine("--select-class com.acme.Foo").getSelectedClasses()),
				() -> assertEquals(singletonList("com.acme.Foo"), parseArgLine("--select-class=com.acme.Foo").getSelectedClasses()),
				() -> assertEquals(asList("com.acme.Foo", "com.example.Bar"), parseArgLine("-c com.acme.Foo -c com.example.Bar").getSelectedClasses())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidClassSelectors() {
		assertOptionWithMissingRequiredArgumentThrowsException("-c", "--select-class");
	}

	@Test
	void parseValidMethodSelectors() {
		// @formatter:off
		assertAll(
				() -> assertEquals(singletonList("com.acme.Foo#m()"), parseArgLine("-m com.acme.Foo#m()").getSelectedMethods()),
				() -> assertEquals(singletonList("com.acme.Foo#m()"), parseArgLine("--m com.acme.Foo#m()").getSelectedMethods()),
				() -> assertEquals(singletonList("com.acme.Foo#m()"), parseArgLine("-select-method com.acme.Foo#m()").getSelectedMethods()),
				() -> assertEquals(singletonList("com.acme.Foo#m()"), parseArgLine("-select-method=com.acme.Foo#m()").getSelectedMethods()),
				() -> assertEquals(singletonList("com.acme.Foo#m()"), parseArgLine("--select-method com.acme.Foo#m()").getSelectedMethods()),
				() -> assertEquals(singletonList("com.acme.Foo#m()"), parseArgLine("--select-method=com.acme.Foo#m()").getSelectedMethods()),
				() -> assertEquals(asList("com.acme.Foo#m()", "com.example.Bar#method(java.lang.Object)"),
						parseArgLine("-m com.acme.Foo#m() -m com.example.Bar#method(java.lang.Object)").getSelectedMethods())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidMethodSelectors() {
		assertOptionWithMissingRequiredArgumentThrowsException("-m", "--select-method");
	}

	@Test
	void parseValidClasspathResourceSelectors() {
		// @formatter:off
		assertAll(
				() -> assertEquals(singletonList("/foo.csv"), parseArgLine("-r /foo.csv").getSelectedClasspathResources()),
				() -> assertEquals(singletonList("/foo.csv"), parseArgLine("--r /foo.csv").getSelectedClasspathResources()),
				() -> assertEquals(singletonList("/foo.csv"), parseArgLine("-select-resource /foo.csv").getSelectedClasspathResources()),
				() -> assertEquals(singletonList("/foo.csv"), parseArgLine("-select-resource=/foo.csv").getSelectedClasspathResources()),
				() -> assertEquals(singletonList("/foo.csv"), parseArgLine("--select-resource /foo.csv").getSelectedClasspathResources()),
				() -> assertEquals(singletonList("/foo.csv"), parseArgLine("--select-resource=/foo.csv").getSelectedClasspathResources()),
				() -> assertEquals(asList("/foo.csv", "bar.json"), parseArgLine("-r /foo.csv -r bar.json").getSelectedClasspathResources())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidClasspathResourceSelectors() {
		assertOptionWithMissingRequiredArgumentThrowsException("-r", "--select-resource");
	}

	@Test
	void parseClasspathScanningEntries() {
		Path dir = Paths.get(".");
		// @formatter:off
		assertAll(
			() -> assertTrue(parseArgLine("--scan-class-path").isScanClasspath()),
			() -> assertEquals(emptyList(), parseArgLine("--scan-class-path").getSelectedClasspathEntries()),
			() -> assertTrue(parseArgLine("--scan-classpath").isScanClasspath()),
			() -> assertEquals(emptyList(), parseArgLine("--scan-classpath").getSelectedClasspathEntries()),
			() -> assertTrue(parseArgLine("--scan-class-path .").isScanClasspath()),
			() -> assertEquals(singletonList(dir), parseArgLine("--scan-class-path .").getSelectedClasspathEntries()),
			() -> assertEquals(singletonList(dir), parseArgLine("--scan-class-path=.").getSelectedClasspathEntries()),
			() -> assertEquals(singletonList(dir), parseArgLine("-scan-class-path .").getSelectedClasspathEntries()),
			() -> assertEquals(singletonList(dir), parseArgLine("-scan-class-path=.").getSelectedClasspathEntries()),
			() -> assertEquals(asList(dir, Paths.get("src/test/java")), parseArgLine("--scan-class-path . --scan-class-path src/test/java").getSelectedClasspathEntries()),
			() -> assertEquals(asList(dir, Paths.get("src/test/java")), parseArgLine("--scan-class-path ." + File.pathSeparator + "src/test/java").getSelectedClasspathEntries())
		);
		// @formatter:on
	}

	@Test
	void parseValidConfigurationParameters() {
		// @formatter:off
		assertAll(
				() -> assertThat(parseArgLine("-config foo=bar").getConfigurationParameters())
						.containsOnly(entry("foo", "bar")),
				() -> assertThat(parseArgLine("-config=foo=bar").getConfigurationParameters())
						.containsOnly(entry("foo", "bar")),
				() -> assertThat(parseArgLine("--config foo=bar").getConfigurationParameters())
						.containsOnly(entry("foo", "bar")),
				() -> assertThat(parseArgLine("--config=foo=bar").getConfigurationParameters())
						.containsOnly(entry("foo", "bar")),
				() -> assertThat(parseArgLine("--config foo=bar --config baz=qux").getConfigurationParameters())
						.containsExactly(entry("foo", "bar"), entry("baz", "qux"))
		);
		// @formatter:on
	}

	@Test
	void parseInvalidConfigurationParameters() {
		assertOptionWithMissingRequiredArgumentThrowsException("-config", "--config");
	}

	@Test
	void printHelpOutputsHelpOption() {
		StringWriter writer = new StringWriter();

		createParser().printHelp(writer);

		assertThat(writer.toString()).contains("--help");
	}

	@Test
	void printHelpPreservesOriginalIOException() {
		Writer writer = new Writer() {

			@Override
			public void write(char[] cbuf, int off, int len) throws IOException {
				throw new IOException("Something went wrong");
			}

			@Override
			public void flush() {
			}

			@Override
			public void close() {
			}
		};

		CommandLineOptionsParser parser = createParser();
		RuntimeException exception = assertThrows(RuntimeException.class, () -> parser.printHelp(writer));

		assertThat(exception).hasCauseInstanceOf(IOException.class);
		assertThat(exception.getCause()).hasMessage("Something went wrong");
	}

	private void assertOptionWithMissingRequiredArgumentThrowsException(String... options) {
		assertAll(stream(options).map(opt -> () -> assertThrows(JUnitException.class, () -> parseArgLine(opt))));
	}

	private void assertParses(String name, Predicate<CommandLineOptions> property, String... argLines) {
		stream(argLines).forEach(argLine -> {
			CommandLineOptions options = parseArgLine(argLine);
			assertTrue(property.test(options), () -> name + " should be enabled by: " + argLine);
		});
	}

	private CommandLineOptions parseArgLine(String argLine) {
		String[] arguments = argLine.split("\\s+");
		return createParser().parse(arguments);
	}

	private CommandLineOptionsParser createParser() {
		return new JOptSimpleCommandLineOptionsParser();
	}

}
