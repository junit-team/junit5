/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.options;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.engine.discovery.ClassNameFilter.STANDARD_INCLUDE_PATTERN;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.platform.commons.JUnitException;

/**
 * @since 1.0
 */
class PicocliCommandLineOptionsParserTests {

	@Test
	void parseNoArguments() {
		String[] noArguments = {};
		var options = createParser().parse(noArguments);

		// @formatter:off
		assertAll(
			() -> assertFalse(options.isAnsiColorOutputDisabled()),
			() -> assertFalse(options.isDisplayHelp()),
			() -> assertEquals(CommandLineOptions.DEFAULT_DETAILS, options.getDetails()),
			() -> assertFalse(options.isScanClasspath()),
			() -> assertEquals(List.of(STANDARD_INCLUDE_PATTERN), options.getIncludedClassNamePatterns()),
			() -> assertEquals(List.of(), options.getExcludedClassNamePatterns()),
			() -> assertEquals(List.of(), options.getIncludedPackages()),
			() -> assertEquals(List.of(), options.getExcludedPackages()),
			() -> assertEquals(List.of(), options.getIncludedTagExpressions()),
			() -> assertEquals(List.of(), options.getExcludedTagExpressions()),
			() -> assertEquals(List.of(), options.getAdditionalClasspathEntries()),
			() -> assertEquals(Optional.empty(), options.getReportsDir()),
			() -> assertEquals(List.of(), options.getSelectedUris()),
			() -> assertEquals(List.of(), options.getSelectedFiles()),
			() -> assertEquals(List.of(), options.getSelectedDirectories()),
			() -> assertEquals(List.of(), options.getSelectedModules()),
			() -> assertEquals(List.of(), options.getSelectedPackages()),
			() -> assertEquals(List.of(), options.getSelectedMethods()),
			() -> assertEquals(List.of(), options.getSelectedClasspathEntries()),
			() -> assertEquals(Map.of(), options.getConfigurationParameters())
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

	@ParameterizedTest
	@EnumSource
	void parseValidDetails(ArgsType type) {
		// @formatter:off
		assertAll(
			() -> assertEquals(Details.VERBOSE, type.parseArgLine("--details verbose").getDetails()),
			() -> assertEquals(Details.TREE, type.parseArgLine("--details tree").getDetails()),
			() -> assertEquals(Details.FLAT, type.parseArgLine("--details flat").getDetails()),
			() -> assertEquals(Details.NONE, type.parseArgLine("--details NONE").getDetails()),
			() -> assertEquals(Details.NONE, type.parseArgLine("--details none").getDetails()),
			() -> assertEquals(Details.NONE, type.parseArgLine("--details None").getDetails())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidDetails() {
		assertOptionWithMissingRequiredArgumentThrowsException("--details");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidDetailsTheme(ArgsType type) {
		// @formatter:off
		assertAll(
			() -> assertEquals(Theme.ASCII, type.parseArgLine("--details-theme ascii").getTheme()),
			() -> assertEquals(Theme.ASCII, type.parseArgLine("--details-theme ASCII").getTheme()),
			() -> assertEquals(Theme.UNICODE, type.parseArgLine("--details-theme unicode").getTheme()),
			() -> assertEquals(Theme.UNICODE, type.parseArgLine("--details-theme UNICODE").getTheme()),
			() -> assertEquals(Theme.UNICODE, type.parseArgLine("--details-theme uniCode").getTheme())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidDetailsTheme() {
		assertOptionWithMissingRequiredArgumentThrowsException("--details-theme");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidIncludeClassNamePatterns(ArgsType type) {
		// @formatter:off
		assertAll(
			() -> assertEquals(List.of(".*Test"), type.parseArgLine("-n .*Test").getIncludedClassNamePatterns()),
			() -> assertEquals(List.of(".*Test", ".*Tests"), type.parseArgLine("--include-classname .*Test --include-classname .*Tests").getIncludedClassNamePatterns()),
			() -> assertEquals(List.of(".*Test"), type.parseArgLine("--include-classname=.*Test").getIncludedClassNamePatterns())
		);
		// @formatter:on
	}

	@ParameterizedTest
	@EnumSource
	void parseValidExcludeClassNamePatterns(ArgsType type) {
		// @formatter:off
		assertAll(
			() -> assertEquals(List.of(".*Test"), type.parseArgLine("-N .*Test").getExcludedClassNamePatterns()),
			() -> assertEquals(List.of(".*Test", ".*Tests"), type.parseArgLine("--exclude-classname .*Test --exclude-classname .*Tests").getExcludedClassNamePatterns()),
			() -> assertEquals(List.of(".*Test"), type.parseArgLine("--exclude-classname=.*Test").getExcludedClassNamePatterns())
		);
		// @formatter:on
	}

	@Test
	void usesDefaultClassNamePatternWithoutExplicitArgument() throws Exception {
		assertEquals(List.of(STANDARD_INCLUDE_PATTERN), ArgsType.args.parseArgLine("").getIncludedClassNamePatterns());
	}

	@Test
	void parseInvalidIncludeClassNamePatterns() {
		assertOptionWithMissingRequiredArgumentThrowsException("-n", "--include-classname");
	}

	@Test
	void parseInvalidExcludeClassNamePatterns() {
		assertOptionWithMissingRequiredArgumentThrowsException("-N", "--exclude-classname");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidIncludedPackages(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertEquals(List.of("org.junit.included"),
						type.parseArgLine("--include-package org.junit.included").getIncludedPackages()),
				() -> assertEquals(List.of("org.junit.included"),
						type.parseArgLine("--include-package=org.junit.included").getIncludedPackages()),
				() -> assertEquals(List.of("org.junit.included1", "org.junit.included2"),
						type.parseArgLine("--include-package org.junit.included1 --include-package org.junit.included2").getIncludedPackages())
		);
		// @formatter:on
	}

	@ParameterizedTest
	@EnumSource
	void parseValidExcludedPackages(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertEquals(List.of("org.junit.excluded"),
						type.parseArgLine("--exclude-package org.junit.excluded").getExcludedPackages()),
				() -> assertEquals(List.of("org.junit.excluded"),
						type.parseArgLine("--exclude-package=org.junit.excluded").getExcludedPackages()),
				() -> assertEquals(List.of("org.junit.excluded1", "org.junit.excluded2"),
						type.parseArgLine("--exclude-package org.junit.excluded1 --exclude-package org.junit.excluded2").getExcludedPackages())
		);
		// @formatter:on
	}

	@ParameterizedTest
	@EnumSource
	void parseValidIncludedTags(ArgsType type) {
		// @formatter:off
		assertAll(
			() -> assertEquals(List.of("fast"), type.parseArgLine("-t fast").getIncludedTagExpressions()),
			() -> assertEquals(List.of("fast"), type.parseArgLine("--include-tag fast").getIncludedTagExpressions()),
			() -> assertEquals(List.of("fast"), type.parseArgLine("--include-tag=fast").getIncludedTagExpressions()),
			() -> assertEquals(List.of("fast", "slow"), type.parseArgLine("-t fast -t slow").getIncludedTagExpressions())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidIncludedTags() {
		assertOptionWithMissingRequiredArgumentThrowsException("-t", "--include-tag");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidExcludedTags(ArgsType type) {
		// @formatter:off
		assertAll(
			() -> assertEquals(List.of("fast"), type.parseArgLine("-T fast").getExcludedTagExpressions()),
			() -> assertEquals(List.of("fast"), type.parseArgLine("--exclude-tag fast").getExcludedTagExpressions()),
			() -> assertEquals(List.of("fast"), type.parseArgLine("--exclude-tag=fast").getExcludedTagExpressions()),
			() -> assertEquals(List.of("fast", "slow"), type.parseArgLine("-T fast -T slow").getExcludedTagExpressions())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidExcludedTags() {
		assertOptionWithMissingRequiredArgumentThrowsException("-T", "--exclude-tag");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidIncludedEngines(ArgsType type) {
		// @formatter:off
		assertAll(
			() -> assertEquals(List.of("junit-jupiter"), type.parseArgLine("-e junit-jupiter").getIncludedEngines()),
			() -> assertEquals(List.of("junit-vintage"), type.parseArgLine("--include-engine junit-vintage").getIncludedEngines()),
			() -> assertEquals(List.of(), type.parseArgLine("").getIncludedEngines())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidIncludedEngines() {
		assertOptionWithMissingRequiredArgumentThrowsException("-e", "--include-engine");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidExcludedEngines(ArgsType type) {
		// @formatter:off
		assertAll(
			() -> assertEquals(List.of("junit-jupiter"), type.parseArgLine("-E junit-jupiter").getExcludedEngines()),
			() -> assertEquals(List.of("junit-vintage"), type.parseArgLine("--exclude-engine junit-vintage").getExcludedEngines()),
			() -> assertEquals(List.of(), type.parseArgLine("").getExcludedEngines())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidExcludedEngines() {
		assertOptionWithMissingRequiredArgumentThrowsException("-E", "--exclude-engine");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidAdditionalClasspathEntries(ArgsType type) {
		var dir = Paths.get(".");
		// @formatter:off
		assertAll(
			() -> assertEquals(List.of(dir), type.parseArgLine("-cp .").getAdditionalClasspathEntries()),
			() -> assertEquals(List.of(dir), type.parseArgLine("--cp .").getAdditionalClasspathEntries()),
			() -> assertEquals(List.of(dir), type.parseArgLine("-classpath .").getAdditionalClasspathEntries()),
			() -> assertEquals(List.of(dir), type.parseArgLine("-classpath=.").getAdditionalClasspathEntries()),
			() -> assertEquals(List.of(dir), type.parseArgLine("--classpath .").getAdditionalClasspathEntries()),
			() -> assertEquals(List.of(dir), type.parseArgLine("--classpath=.").getAdditionalClasspathEntries()),
			() -> assertEquals(List.of(dir), type.parseArgLine("--class-path .").getAdditionalClasspathEntries()),
			() -> assertEquals(List.of(dir), type.parseArgLine("--class-path=.").getAdditionalClasspathEntries()),
			() -> assertEquals(List.of(dir, Paths.get("src", "test", "java")), type.parseArgLine("-cp . -cp src/test/java").getAdditionalClasspathEntries()),
			() -> assertEquals(List.of(dir, Paths.get("src", "test", "java")), type.parseArgLine("-cp ." + File.pathSeparator + "src/test/java").getAdditionalClasspathEntries())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidAdditionalClasspathEntries() {
		assertOptionWithMissingRequiredArgumentThrowsException("-cp", "--classpath", "--class-path");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidXmlReportsDirs(ArgsType type) {
		var dir = Paths.get("build", "test-results");
		// @formatter:off
		assertAll(
			() -> assertEquals(Optional.of(dir), type.parseArgLine("--reports-dir build/test-results").getReportsDir()),
			() -> assertEquals(Optional.of(dir), type.parseArgLine("--reports-dir=build/test-results").getReportsDir())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidXmlReportsDirs() {
		assertOptionWithMissingRequiredArgumentThrowsException("--reports-dir");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidUriSelectors(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertEquals(List.of(new URI("file:///foo.txt")), type.parseArgLine("-u file:///foo.txt").getSelectedUris()),
				() -> assertEquals(List.of(new URI("file:///foo.txt")), type.parseArgLine("--u file:///foo.txt").getSelectedUris()),
				() -> assertEquals(List.of(new URI("file:///foo.txt")), type.parseArgLine("-select-uri file:///foo.txt").getSelectedUris()),
				() -> assertEquals(List.of(new URI("file:///foo.txt")), type.parseArgLine("-select-uri=file:///foo.txt").getSelectedUris()),
				() -> assertEquals(List.of(new URI("file:///foo.txt")), type.parseArgLine("--select-uri file:///foo.txt").getSelectedUris()),
				() -> assertEquals(List.of(new URI("file:///foo.txt")), type.parseArgLine("--select-uri=file:///foo.txt").getSelectedUris()),
				() -> assertEquals(List.of(new URI("file:///foo.txt"), new URI("https://example")), type.parseArgLine("-u file:///foo.txt -u https://example").getSelectedUris())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidUriSelectors() {
		assertOptionWithMissingRequiredArgumentThrowsException("-u", "--select-uri", "-u unknown-scheme:");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidFileSelectors(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertEquals(List.of("foo.txt"), type.parseArgLine("-f foo.txt").getSelectedFiles()),
				() -> assertEquals(List.of("foo.txt"), type.parseArgLine("--f foo.txt").getSelectedFiles()),
				() -> assertEquals(List.of("foo.txt"), type.parseArgLine("-select-file foo.txt").getSelectedFiles()),
				() -> assertEquals(List.of("foo.txt"), type.parseArgLine("-select-file=foo.txt").getSelectedFiles()),
				() -> assertEquals(List.of("foo.txt"), type.parseArgLine("--select-file foo.txt").getSelectedFiles()),
				() -> assertEquals(List.of("foo.txt"), type.parseArgLine("--select-file=foo.txt").getSelectedFiles()),
				() -> assertEquals(List.of("foo.txt", "bar.csv"), type.parseArgLine("-f foo.txt -f bar.csv").getSelectedFiles())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidFileSelectors() {
		assertOptionWithMissingRequiredArgumentThrowsException("-f", "--select-file");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidDirectorySelectors(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertEquals(List.of("foo/bar"), type.parseArgLine("-d foo/bar").getSelectedDirectories()),
				() -> assertEquals(List.of("foo/bar"), type.parseArgLine("--d foo/bar").getSelectedDirectories()),
				() -> assertEquals(List.of("foo/bar"), type.parseArgLine("-select-directory foo/bar").getSelectedDirectories()),
				() -> assertEquals(List.of("foo/bar"), type.parseArgLine("-select-directory=foo/bar").getSelectedDirectories()),
				() -> assertEquals(List.of("foo/bar"), type.parseArgLine("--select-directory foo/bar").getSelectedDirectories()),
				() -> assertEquals(List.of("foo/bar"), type.parseArgLine("--select-directory=foo/bar").getSelectedDirectories()),
				() -> assertEquals(List.of("foo/bar", "bar/qux"), type.parseArgLine("-d foo/bar -d bar/qux").getSelectedDirectories())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidDirectorySelectors() {
		assertOptionWithMissingRequiredArgumentThrowsException("-d", "--select-directory");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidModuleSelectors(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertEquals(List.of("com.acme.foo"), type.parseArgLine("-o com.acme.foo").getSelectedModules()),
				() -> assertEquals(List.of("com.acme.foo"), type.parseArgLine("--o com.acme.foo").getSelectedModules()),
				() -> assertEquals(List.of("com.acme.foo"), type.parseArgLine("-select-module com.acme.foo").getSelectedModules()),
				() -> assertEquals(List.of("com.acme.foo"), type.parseArgLine("-select-module=com.acme.foo").getSelectedModules()),
				() -> assertEquals(List.of("com.acme.foo"), type.parseArgLine("--select-module com.acme.foo").getSelectedModules()),
				() -> assertEquals(List.of("com.acme.foo"), type.parseArgLine("--select-module=com.acme.foo").getSelectedModules()),
				() -> assertEquals(List.of("com.acme.foo", "com.example.bar"), type.parseArgLine("-o com.acme.foo -o com.example.bar").getSelectedModules())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidModuleSelectors() {
		assertOptionWithMissingRequiredArgumentThrowsException("-o", "--select-module");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidPackageSelectors(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertEquals(List.of("com.acme.foo"), type.parseArgLine("-p com.acme.foo").getSelectedPackages()),
				() -> assertEquals(List.of("com.acme.foo"), type.parseArgLine("--p com.acme.foo").getSelectedPackages()),
				() -> assertEquals(List.of("com.acme.foo"), type.parseArgLine("-select-package com.acme.foo").getSelectedPackages()),
				() -> assertEquals(List.of("com.acme.foo"), type.parseArgLine("-select-package=com.acme.foo").getSelectedPackages()),
				() -> assertEquals(List.of("com.acme.foo"), type.parseArgLine("--select-package com.acme.foo").getSelectedPackages()),
				() -> assertEquals(List.of("com.acme.foo"), type.parseArgLine("--select-package=com.acme.foo").getSelectedPackages()),
				() -> assertEquals(List.of("com.acme.foo", "com.example.bar"), type.parseArgLine("-p com.acme.foo -p com.example.bar").getSelectedPackages())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidPackageSelectors() {
		assertOptionWithMissingRequiredArgumentThrowsException("-p", "--select-package");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidClassSelectors(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertEquals(List.of("com.acme.Foo"), type.parseArgLine("-c com.acme.Foo").getSelectedClasses()),
				() -> assertEquals(List.of("com.acme.Foo"), type.parseArgLine("--c com.acme.Foo").getSelectedClasses()),
				() -> assertEquals(List.of("com.acme.Foo"), type.parseArgLine("-select-class com.acme.Foo").getSelectedClasses()),
				() -> assertEquals(List.of("com.acme.Foo"), type.parseArgLine("-select-class=com.acme.Foo").getSelectedClasses()),
				() -> assertEquals(List.of("com.acme.Foo"), type.parseArgLine("--select-class com.acme.Foo").getSelectedClasses()),
				() -> assertEquals(List.of("com.acme.Foo"), type.parseArgLine("--select-class=com.acme.Foo").getSelectedClasses()),
				() -> assertEquals(List.of("com.acme.Foo", "com.example.Bar"), type.parseArgLine("-c com.acme.Foo -c com.example.Bar").getSelectedClasses())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidClassSelectors() {
		assertOptionWithMissingRequiredArgumentThrowsException("-c", "--select-class");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidMethodSelectors(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertEquals(List.of("com.acme.Foo#m()"), type.parseArgLine("-m com.acme.Foo#m()").getSelectedMethods()),
				() -> assertEquals(List.of("com.acme.Foo#m()"), type.parseArgLine("--m com.acme.Foo#m()").getSelectedMethods()),
				() -> assertEquals(List.of("com.acme.Foo#m()"), type.parseArgLine("-select-method com.acme.Foo#m()").getSelectedMethods()),
				() -> assertEquals(List.of("com.acme.Foo#m()"), type.parseArgLine("-select-method=com.acme.Foo#m()").getSelectedMethods()),
				() -> assertEquals(List.of("com.acme.Foo#m()"), type.parseArgLine("--select-method com.acme.Foo#m()").getSelectedMethods()),
				() -> assertEquals(List.of("com.acme.Foo#m()"), type.parseArgLine("--select-method=com.acme.Foo#m()").getSelectedMethods()),
				() -> assertEquals(List.of("com.acme.Foo#m()", "com.example.Bar#method(java.lang.Object)"),
						type.parseArgLine("-m com.acme.Foo#m() -m com.example.Bar#method(java.lang.Object)").getSelectedMethods())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidMethodSelectors() {
		assertOptionWithMissingRequiredArgumentThrowsException("-m", "--select-method");
	}

	@ParameterizedTest
	@EnumSource
	void parseValidClasspathResourceSelectors(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertEquals(List.of("/foo.csv"), type.parseArgLine("-r /foo.csv").getSelectedClasspathResources()),
				() -> assertEquals(List.of("/foo.csv"), type.parseArgLine("--r /foo.csv").getSelectedClasspathResources()),
				() -> assertEquals(List.of("/foo.csv"), type.parseArgLine("-select-resource /foo.csv").getSelectedClasspathResources()),
				() -> assertEquals(List.of("/foo.csv"), type.parseArgLine("-select-resource=/foo.csv").getSelectedClasspathResources()),
				() -> assertEquals(List.of("/foo.csv"), type.parseArgLine("--select-resource /foo.csv").getSelectedClasspathResources()),
				() -> assertEquals(List.of("/foo.csv"), type.parseArgLine("--select-resource=/foo.csv").getSelectedClasspathResources()),
				() -> assertEquals(List.of("/foo.csv", "bar.json"), type.parseArgLine("-r /foo.csv -r bar.json").getSelectedClasspathResources())
		);
		// @formatter:on
	}

	@Test
	void parseInvalidClasspathResourceSelectors() {
		assertOptionWithMissingRequiredArgumentThrowsException("-r", "--select-resource");
	}

	@ParameterizedTest
	@EnumSource
	void parseClasspathScanningEntries(ArgsType type) {
		var dir = Paths.get(".");
		// @formatter:off
		assertAll(
			() -> assertTrue(type.parseArgLine("--scan-class-path").isScanClasspath()),
			() -> assertEquals(List.of(), type.parseArgLine("--scan-class-path").getSelectedClasspathEntries()),
			() -> assertTrue(type.parseArgLine("--scan-classpath").isScanClasspath()),
			() -> assertEquals(List.of(), type.parseArgLine("--scan-classpath").getSelectedClasspathEntries()),
			() -> assertTrue(type.parseArgLine("--scan-class-path .").isScanClasspath()),
			() -> assertEquals(List.of(dir), type.parseArgLine("--scan-class-path .").getSelectedClasspathEntries()),
			() -> assertEquals(List.of(dir), type.parseArgLine("--scan-class-path=.").getSelectedClasspathEntries()),
			() -> assertEquals(List.of(dir), type.parseArgLine("-scan-class-path .").getSelectedClasspathEntries()),
			() -> assertEquals(List.of(dir), type.parseArgLine("-scan-class-path=.").getSelectedClasspathEntries()),
			() -> assertEquals(List.of(dir, Paths.get("src/test/java")), type.parseArgLine("--scan-class-path . --scan-class-path src/test/java").getSelectedClasspathEntries()),
			() -> assertEquals(List.of(dir, Paths.get("src/test/java")), type.parseArgLine("--scan-class-path ." + File.pathSeparator + "src/test/java").getSelectedClasspathEntries())
		);
		// @formatter:on
	}

	@ParameterizedTest
	@EnumSource
	void parseValidConfigurationParameters(ArgsType type) {
		// @formatter:off
		assertAll(
				() -> assertThat(type.parseArgLine("-config foo=bar").getConfigurationParameters())
						.containsOnly(entry("foo", "bar")),
				() -> assertThat(type.parseArgLine("-config=foo=bar").getConfigurationParameters())
						.containsOnly(entry("foo", "bar")),
				() -> assertThat(type.parseArgLine("--config foo=bar").getConfigurationParameters())
						.containsOnly(entry("foo", "bar")),
				() -> assertThat(type.parseArgLine("--config=foo=bar").getConfigurationParameters())
						.containsOnly(entry("foo", "bar")),
				() -> assertThat(type.parseArgLine("--config foo=bar --config baz=qux").getConfigurationParameters())
						.containsExactly(entry("foo", "bar"), entry("baz", "qux"))
		);
		// @formatter:on
	}

	@Test
	void parseInvalidConfigurationParameters() {
		assertOptionWithMissingRequiredArgumentThrowsException("-config", "--config");
	}

	@ParameterizedTest
	@EnumSource
	void parseInvalidConfigurationParametersWithDuplicateKey(ArgsType type) {
		Exception e = assertThrows(JUnitException.class, () -> type.parseArgLine("--config foo=bar --config foo=baz"));

		assertThat(e.getMessage()).isEqualTo(
			"Error parsing command-line arguments: Duplicate key 'foo' for values 'bar' and 'baz'.");
		assertThat(e.getCause().getMessage()).isEqualTo("Duplicate key 'foo' for values 'bar' and 'baz'.");
	}

	@Test
	void printHelpOutputsHelpOption() {
		var writer = new StringWriter();

		createParser().printHelp(writer, true);

		assertThat(writer.toString()).contains("--help");
	}

	@Test
	void printHelpPreservesOriginalIOException() {
		var writer = new Writer() {

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

		var parser = createParser();
		var exception = assertThrows(RuntimeException.class, () -> parser.printHelp(writer, true));

		assertThat(exception).hasCauseInstanceOf(IOException.class);
		assertThat(exception.getCause()).hasMessage("Something went wrong");
	}

	private void assertOptionWithMissingRequiredArgumentThrowsException(String... options) {
		assertAll(Stream.of(options).map(
			opt -> () -> assertThrows(JUnitException.class, () -> ArgsType.args.parseArgLine(opt))));
	}

	private void assertParses(String name, Predicate<CommandLineOptions> property, String... argLines) {
		Stream.of(argLines).forEach(argLine -> {
			CommandLineOptions options = null;
			try {
				options = ArgsType.args.parseArgLine(argLine);
			}
			catch (IOException e) {
				fail(e);
			}
			assertTrue(property.test(options), () -> name + " should be enabled by: " + argLine);
		});
	}

	enum ArgsType {
		args {
			CommandLineOptions parseArgLine(String argLine) {
				return createParser().parse(split(argLine));
			}
		},
		atFile {
			CommandLineOptions parseArgLine(String argLine) throws IOException {
				var atFile = Files.createTempFile("junit-launcher-args", ".txt");
				try {
					Files.write(atFile, List.of(split(argLine)));
					return createParser().parse("@" + atFile);
				}
				finally {
					Files.deleteIfExists(atFile);
				}
			}
		};
		abstract CommandLineOptions parseArgLine(String argLine) throws IOException;

		private static String[] split(String argLine) {
			return "".equals(argLine) ? new String[0] : argLine.split("\\s+");
		}
	}

	private static CommandLineOptionsParser createParser() {
		return new PicocliCommandLineOptionsParser();
	}

}
