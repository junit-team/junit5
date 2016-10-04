/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console.options;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.function.Predicate;

import joptsimple.OptionException;

import org.junit.jupiter.api.Test;

/**
 * @since 1.0
 */
class JOptSimpleCommandLineOptionsParserTests {

	@Test
	public void parseNoArguments() {
		String[] noArguments = {};
		CommandLineOptions options = createParser().parse(noArguments);

		// @formatter:off
		assertAll(
			() -> assertFalse(options.isAnsiColorOutputDisabled()),
			() -> assertFalse(options.isDisplayHelp()),
			() -> assertFalse(options.isHideDetails()),
			() -> assertFalse(options.isScanClasspath()),
			() -> assertEquals(STANDARD_INCLUDE_PATTERN, options.getIncludeClassNamePattern()),
			() -> assertEquals(emptyList(), options.getIncludedTags()),
			() -> assertEquals(emptyList(), options.getAdditionalClasspathEntries()),
			() -> assertEquals(Optional.empty(), options.getReportsDir()),
			() -> assertEquals(emptyList(), options.getArguments())
		);
		// @formatter:on
	}

	@Test
	public void parseSwitches() {
		// @formatter:off
		assertAll(
				() -> assertParses("disable ansi", CommandLineOptions::isAnsiColorOutputDisabled, "--disable-ansi-colors"),
				() -> assertParses("help", CommandLineOptions::isDisplayHelp, "-h", "--help"),
			() -> assertParses("hide details", CommandLineOptions::isHideDetails, "--hide-details"),
			() -> assertParses("scan class path", CommandLineOptions::isScanClasspath, "--scan-class-path")
		);
		// @formatter:on
	}

	@Test
	public void parseValidIncludeClassNamePatterns() {
		// @formatter:off
		assertAll(
			() -> assertEquals(".*Test", parseArgLine("-n .*Test").getIncludeClassNamePattern()),
			() -> assertEquals(".*Test", parseArgLine("--include-classname .*Test").getIncludeClassNamePattern()),
			() -> assertEquals(".*Test", parseArgLine("--include-classname=.*Test").getIncludeClassNamePattern())
		);
		// @formatter:on
	}

	@Test
	public void usesDefaultClassNamePatternWithoutExplicitArgument() {
		assertEquals(STANDARD_INCLUDE_PATTERN, parseArgLine("").getIncludeClassNamePattern());
	}

	@Test
	public void parseInvalidIncludeClassNamePatterns() throws Exception {
		assertOptionWithMissingRequiredArgumentThrowsException("-n", "--include-classname");
	}

	@Test
	public void parseValidIncludedTags() {
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
	public void parseInvalidIncludedTags() {
		assertOptionWithMissingRequiredArgumentThrowsException("-t", "--include-tag");
	}

	@Test
	public void parseValidExcludedTags() {
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
	public void parseInvalidExcludedTags() {
		assertOptionWithMissingRequiredArgumentThrowsException("-T", "--exclude-tag");
	}

	@Test
	public void parseValidIncludedEngines() {
		// @formatter:off
		assertAll(
			() -> assertEquals(asList("junit-jupiter"), parseArgLine("-e junit-jupiter").getIncludedEngines()),
			() -> assertEquals(asList("junit-vintage"), parseArgLine("--include-engine junit-vintage").getIncludedEngines()),
			() -> assertEquals(emptyList(), parseArgLine("").getIncludedEngines())
		);
		// @formatter:on
	}

	@Test
	public void parseInvalidIncludedEngines() throws Exception {
		assertOptionWithMissingRequiredArgumentThrowsException("-e", "--include-engine");
	}

	@Test
	public void parseValidExcludedEngines() {
		// @formatter:off
		assertAll(
			() -> assertEquals(asList("junit-jupiter"), parseArgLine("-E junit-jupiter").getExcludedEngines()),
			() -> assertEquals(asList("junit-vintage"), parseArgLine("--exclude-engine junit-vintage").getExcludedEngines()),
			() -> assertEquals(emptyList(), parseArgLine("").getExcludedEngines())
		);
		// @formatter:on
	}

	@Test
	public void parseInvalidExcludedEngines() throws Exception {
		assertOptionWithMissingRequiredArgumentThrowsException("-E", "--exclude-engine");
	}

	@Test
	public void parseValidAdditionalClasspathEntries() {
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
	public void parseInvalidAdditionalClasspathEntries() {
		assertOptionWithMissingRequiredArgumentThrowsException("-cp", "--classpath", "--class-path");
	}

	@Test
	public void parseValidXmlReportsDirs() {
		Path dir = Paths.get("build", "test-results");
		// @formatter:off
		assertAll(
			() -> assertEquals(Optional.of(dir), parseArgLine("--reports-dir build/test-results").getReportsDir()),
			() -> assertEquals(Optional.of(dir), parseArgLine("--reports-dir=build/test-results").getReportsDir())
		);
		// @formatter:on
	}

	@Test
	public void parseInvalidXmlReportsDirs() throws Exception {
		assertOptionWithMissingRequiredArgumentThrowsException("--reports-dir");
	}

	@Test
	public void parseExtraArguments() {
		// @formatter:off
		assertAll(
			() -> assertEquals(asList("foo"), parseArgLine("foo").getArguments()),
			() -> assertEquals(asList("foo", "bar"), parseArgLine("-h foo bar").getArguments()),
			() -> assertEquals(asList("foo", "bar"), parseArgLine("-h -- foo bar").getArguments())
		);
		// @formatter:on
	}

	@Test
	public void printHelpOutputsHelpOption() {
		StringWriter writer = new StringWriter();

		createParser().printHelp(writer);

		assertThat(writer.toString()).contains("--help");
	}

	@Test
	public void printHelpPreservesOriginalIOException() {
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
		assertAll(stream(options).map(opt -> () -> assertThrows(OptionException.class, () -> parseArgLine(opt))));
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
