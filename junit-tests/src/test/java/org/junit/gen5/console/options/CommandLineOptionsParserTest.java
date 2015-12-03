/*
 * Copyright 2015 the original author or authors.
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
import static org.junit.gen5.api.Assertions.assertAll;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertFalse;
import static org.junit.gen5.api.Assertions.assertThrows;
import static org.junit.gen5.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.function.Predicate;

import org.junit.Test;

public class CommandLineOptionsParserTest {

	@Test
	public void parseNoArguments() {
		CommandLineOptions options = createParser().parse();

		// @formatter:off
		assertAll(
			() -> assertFalse(options.isAnsiColorOutputDisabled()), 
			() -> assertFalse(options.isDisplayHelp()),
			() -> assertFalse(options.isExitCodeEnabled()),
			() -> assertFalse(options.isHideDetails()),
			() -> assertFalse(options.isRunAllTests()),
			() -> assertEquals(Optional.empty(), options.getClassnameFilter()),
			() -> assertEquals(emptyList(), options.getTagsFilter()),
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
		// @formatter:off
		assertAll(
			() -> assertThrows(Exception.class, () -> parseArgLine("-n")),
			() -> assertThrows(Exception.class, () -> parseArgLine("--filter-classname"))
		);
		// @formatter:on
	}

	@Test
	public void parseValidTagFilters() {
		// @formatter:off
		assertAll(
			() -> assertEquals(asList("fast"), parseArgLine("-t fast").getTagsFilter()),
			() -> assertEquals(asList("fast"), parseArgLine("--filter-tags fast").getTagsFilter()),
			() -> assertEquals(asList("fast"), parseArgLine("--filter-tags=fast").getTagsFilter()),
			() -> assertEquals(asList("fast", "slow"), parseArgLine("-t fast -t slow").getTagsFilter())
		);
		// @formatter:on
	}

	@Test
	public void parseInvalidTagFilter() {
		// @formatter:off
		assertAll(
			() -> assertThrows(Exception.class, () -> parseArgLine("-t")),
			() -> assertThrows(Exception.class, () -> parseArgLine("--filter-tags"))
		);
		// @formatter:on
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
