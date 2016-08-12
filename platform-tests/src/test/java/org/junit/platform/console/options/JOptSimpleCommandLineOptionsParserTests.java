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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.expectThrows;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
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
			() -> assertFalse(options.isRunAllTests()),
			() -> assertEquals(Optional.empty(), options.getIncludeClassNamePattern()),
			() -> assertEquals(emptyList(), options.getIncludedTags()),
			() -> assertEquals(emptyList(), options.getAdditionalClasspathEntries()),
			() -> assertEquals(Optional.empty(), options.getXmlReportsDir()),
			() -> assertEquals(emptyList(), options.getArguments())
		);
		// @formatter:on
	}

	@Test
	public void parseSwitches() {
		// @formatter:off
		assertAll(
			() -> assertParses("disable ansi", CommandLineOptions::isAnsiColorOutputDisabled, "-C", "--disable-ansi-colors"),
			() -> assertParses("help", CommandLineOptions::isDisplayHelp, "-h", "--help"),
			() -> assertParses("hide details", CommandLineOptions::isHideDetails, "-D", "--hide-details"),
			() -> assertParses("run all tests", CommandLineOptions::isRunAllTests, "-a", "--all")
		);
		// @formatter:on
	}

	@Test
	public void parseValidIncludeClassNamePatterns() {
		// @formatter:off
		assertAll(
			() -> assertEquals(Optional.of(".*Test"), parseArgLine("-n .*Test").getIncludeClassNamePattern()),
			() -> assertEquals(Optional.of(".*Test"), parseArgLine("--include-classname .*Test").getIncludeClassNamePattern()),
			() -> assertEquals(Optional.of(".*Test"), parseArgLine("--include-classname=.*Test").getIncludeClassNamePattern())
		);
		// @formatter:on
	}

	@Test
	public void defaultIsNoIncludeClassNamePattern() {
		assertEquals(Optional.empty(), parseArgLine("").getIncludeClassNamePattern());
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
		// @formatter:off
		assertAll(
			() -> assertEquals(asList("."), parseArgLine("-cp .").getAdditionalClasspathEntries()),
			() -> assertEquals(asList("."), parseArgLine("--classpath .").getAdditionalClasspathEntries()),
			() -> assertEquals(asList("."), parseArgLine("--classpath=.").getAdditionalClasspathEntries()),
			() -> assertEquals(asList(".", "lib/some.jar"), parseArgLine("-cp . -cp lib/some.jar").getAdditionalClasspathEntries()),
			() -> assertEquals(asList("." + File.pathSeparator + "lib/some.jar"), parseArgLine("-cp ." + File.pathSeparator + "lib/some.jar").getAdditionalClasspathEntries())
		);
		// @formatter:on
	}

	@Test
	public void parseInvalidAdditionalClasspathEntries() {
		assertOptionWithMissingRequiredArgumentThrowsException("-cp", "--classpath");
	}

	@Test
	public void parseValidXmlReportsDirs() {
		// @formatter:off
		assertAll(
			() -> assertEquals(Optional.of("build/test-results"), parseArgLine("-r build/test-results").getXmlReportsDir()),
			() -> assertEquals(Optional.of("build/test-results"), parseArgLine("--xml-reports-dir build/test-results").getXmlReportsDir()),
			() -> assertEquals(Optional.of("build/test-results"), parseArgLine("--xml-reports-dir=build/test-results").getXmlReportsDir())
		);
		// @formatter:on
	}

	@Test
	public void parseInvalidXmlReportsDirs() throws Exception {
		assertOptionWithMissingRequiredArgumentThrowsException("-r", "--xml-reports-dir");
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
		RuntimeException exception = expectThrows(RuntimeException.class, () -> parser.printHelp(writer));

		assertThat(exception).hasCauseInstanceOf(IOException.class);
		assertThat(exception.getCause()).hasMessage("Something went wrong");
	}

	private void assertOptionWithMissingRequiredArgumentThrowsException(String shortOption, String longOption) {
		// @formatter:off
		assertAll(
			() -> assertThrows(OptionException.class, () -> parseArgLine(shortOption)),
			() -> assertThrows(OptionException.class, () -> parseArgLine(longOption))
		);
		// @formatter:on
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
