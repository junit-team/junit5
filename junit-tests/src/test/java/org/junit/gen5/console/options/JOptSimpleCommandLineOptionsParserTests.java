/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console.options;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.api.Assertions.assertAll;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertFalse;
import static org.junit.gen5.api.Assertions.assertThrows;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.junit.gen5.api.Assertions.expectThrows;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.gen5.api.Test;

public class JOptSimpleCommandLineOptionsParserTests {

	@Test
	public void parseNoArguments() {
		String[] noArguments = {};
		CommandLineOptions options = createParser().parse(noArguments);

		// @formatter:off
		assertAll(
			() -> assertFalse(options.isAnsiColorOutputDisabled()),
			() -> assertFalse(options.isDisplayHelp()),
			() -> assertFalse(options.isExitCodeEnabled()),
			() -> assertFalse(options.isHideDetails()),
			() -> assertFalse(options.isRunAllTests()),
			() -> assertEquals(Optional.empty(), options.getClassnameFilter()),
			() -> assertEquals(emptyList(), options.getRequiredTagsFilter()),
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
			() -> assertParses("exit code", CommandLineOptions::isExitCodeEnabled, "-x", "--enable-exit-code"),
			() -> assertParses("hide details", CommandLineOptions::isHideDetails, "-D", "--hide-details"),
			() -> assertParses("run all tests", CommandLineOptions::isRunAllTests, "-a", "--all")
		);
		// @formatter:on
	}

	@Test
	public void parseValidClassnameFilters() {
		// @formatter:off
		assertAll(
			() -> assertEquals(Optional.of(".*Test"), parseArgLine("-n .*Test").getClassnameFilter()),
			() -> assertEquals(Optional.of(".*Test"), parseArgLine("--filter-classname .*Test").getClassnameFilter()),
			() -> assertEquals(Optional.of(".*Test"), parseArgLine("--filter-classname=.*Test").getClassnameFilter())
		);
		// @formatter:on
	}

	@Test
	public void parseInvalidClassnameFilter() throws Exception {
		assertOptionWithRequiredArgumentThrowsExceptionWithoutArgument("-n", "--filter-classname");
	}

	@Test
	public void parseValidTagFilters() {
		// @formatter:off
		assertAll(
			() -> assertEquals(asList("fast"), parseArgLine("-t fast").getRequiredTagsFilter()),
			() -> assertEquals(asList("fast"), parseArgLine("--require-tag fast").getRequiredTagsFilter()),
			() -> assertEquals(asList("fast"), parseArgLine("--require-tag=fast").getRequiredTagsFilter()),
			() -> assertEquals(asList("fast", "slow"), parseArgLine("-t fast -t slow").getRequiredTagsFilter())
		);
		// @formatter:on
	}

	@Test
	public void parseInvalidTagFilter() {
		assertOptionWithRequiredArgumentThrowsExceptionWithoutArgument("-t", "--filter-tags");
	}

	@Test
	public void parseValidAdditionalClasspathEntries() {
		// @formatter:off
		assertAll(
			() -> assertEquals(asList("."), parseArgLine("-p .").getAdditionalClasspathEntries()),
			() -> assertEquals(asList("."), parseArgLine("--classpath .").getAdditionalClasspathEntries()),
			() -> assertEquals(asList("."), parseArgLine("--classpath=.").getAdditionalClasspathEntries()),
			() -> assertEquals(asList(".", "lib/some.jar"), parseArgLine("-p . -p lib/some.jar").getAdditionalClasspathEntries()),
			() -> assertEquals(asList("." + File.pathSeparator + "lib/some.jar"), parseArgLine("-p ." + File.pathSeparator + "lib/some.jar").getAdditionalClasspathEntries())
		);
		// @formatter:on
	}

	@Test
	public void parseInvalidAdditionalClasspathEntries() {
		assertOptionWithRequiredArgumentThrowsExceptionWithoutArgument("-p", "--classpath");
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
		assertOptionWithRequiredArgumentThrowsExceptionWithoutArgument("-r", "--xml-reports-dir");
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

	private void assertOptionWithRequiredArgumentThrowsExceptionWithoutArgument(String shortOption, String longOption) {
		// @formatter:off
		assertAll(
			() -> assertThrows(Exception.class, () -> parseArgLine(shortOption)),
			() -> assertThrows(Exception.class, () -> parseArgLine(longOption))
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
