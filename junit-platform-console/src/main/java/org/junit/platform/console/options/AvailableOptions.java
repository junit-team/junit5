/*
 * Copyright 2015-2017 the original author or authors.
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
import static java.util.stream.Collectors.toList;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.util.PathConverter;

import org.junit.platform.engine.discovery.ClassNameFilter;

/**
 * @since 1.0
 */
class AvailableOptions {

	private static final String CP_OPTION = "cp";

	private final OptionParser parser = new OptionParser();

	// General Purpose
	private final OptionSpec<Void> help;
	private final OptionSpec<Void> disableAnsiColors;
	private final OptionSpec<Details> details;
	private final OptionSpec<Path> additionalClasspathEntries;

	// Reports
	private final OptionSpec<Path> reportsDir;

	// Selectors
	private final OptionSpec<Path> selectedClasspathEntries;
	private final OptionSpec<URI> selectedUris;
	private final OptionSpec<String> selectedFiles;
	private final OptionSpec<String> selectedDirectories;
	private final OptionSpec<String> selectedPackages;
	private final OptionSpec<String> selectedClasses;
	private final OptionSpec<String> selectedMethods;
	private final OptionSpec<String> selectedClasspathResources;

	// Filters
	private final OptionSpec<String> includeClassNamePattern;
	private final OptionSpec<String> includePackage;
	private final OptionSpec<String> excludePackage;
	private final OptionSpec<String> includeTag;
	private final OptionSpec<String> excludeTag;
	private final OptionSpec<String> includeEngine;
	private final OptionSpec<String> excludeEngine;

	AvailableOptions() {

		// --- General Purpose -------------------------------------------------

		help = parser.acceptsAll(asList("h", "help"), //
			"Display help information.");

		disableAnsiColors = parser.accepts("disable-ansi-colors",
			"Disable ANSI colors in output (not supported by all terminals).");

		// TODO Remove "hide-details" before final release. Only here to make "legacy" Gradle plugin happy.
		// If removed, an old Gradle:junitPlatformTest will report:
		//   Exception in thread "main" joptsimple.UnrecognizedOptionException: hide-details is not a recognized option
		parser.accepts("hide-details", "@Deprecated. Use '--details none' instead.");

		List<String> names = stream(Details.values()).map(d -> d.name().toLowerCase()).collect(toList());
		details = parser.accepts("details",
			"Select output details mode while tests are executed. Use one of: " + names
					+ ". If 'none' is selected, only the summary and test failures are shown.") //
				.withRequiredArg() //
				.ofType(Details.class) //
				.defaultsTo(CommandLineOptions.DEFAULT_DETAILS);

		additionalClasspathEntries = parser.acceptsAll(asList(CP_OPTION, "classpath", "class-path"), //
			"Provide additional classpath entries -- for example, for adding engines and their dependencies. "
					+ "This option can be repeated.") //
				.withRequiredArg() //
				.withValuesConvertedBy(new PathConverter()) //
				.withValuesSeparatedBy(File.pathSeparatorChar) //
				.describedAs("path1" + File.pathSeparator + "path2" + File.pathSeparator + "...");

		// --- Reports ---------------------------------------------------------

		reportsDir = parser.accepts("reports-dir", //
			"Enable report output into a specified local directory (will be created if it does not exist).") //
				.withRequiredArg() //
				.withValuesConvertedBy(new PathConverter());

		// --- Selectors -------------------------------------------------------

		selectedClasspathEntries = parser.acceptsAll(asList("scan-class-path", "scan-classpath"), //
			"Scan all directories on the classpath or explicit classpath roots. " //
					+ "Without arguments, only directories on the system classpath as well as additional classpath " //
					+ "entries supplied via -" + CP_OPTION + " (directories and JAR files) are scanned. " //
					+ "Explicit classpath roots that are not on the classpath will be silently ignored. " //
					+ "This option can be repeated.") //
				.withOptionalArg() //
				.withValuesConvertedBy(new PathConverter()) //
				.withValuesSeparatedBy(File.pathSeparatorChar) //
				.describedAs("path1" + File.pathSeparator + "path2" + File.pathSeparator + "...");

		selectedUris = parser.acceptsAll(asList("u", "select-uri"), //
			"Select a URI for test discovery. This option can be repeated.") //
				.withRequiredArg() //
				.withValuesConvertedBy(new URIConverter());

		selectedFiles = parser.acceptsAll(asList("f", "select-file"), //
			"Select a file for test discovery. This option can be repeated.") //
				.withRequiredArg();

		selectedDirectories = parser.acceptsAll(asList("d", "select-directory"), //
			"Select a directory for test discovery. This option can be repeated.") //
				.withRequiredArg();

		selectedPackages = parser.acceptsAll(asList("p", "select-package"), //
			"Select a package for test discovery. This option can be repeated.") //
				.withRequiredArg();

		selectedClasses = parser.acceptsAll(asList("c", "select-class"), //
			"Select a class for test discovery. This option can be repeated.") //
				.withRequiredArg();

		selectedMethods = parser.acceptsAll(asList("m", "select-method"), //
			"Select a method for test discovery. This option can be repeated.") //
				.withRequiredArg();

		selectedClasspathResources = parser.acceptsAll(asList("r", "select-resource"), //
			"Select a classpath resource for test discovery. This option can be repeated.") //
				.withRequiredArg();

		// --- Filters ---------------------------------------------------------

		includeClassNamePattern = parser.acceptsAll(asList("n", "include-classname"),
			"Provide a regular expression to include only classes whose fully qualified names match. " //
					+ "To avoid loading classes unnecessarily, the default pattern only includes class " //
					+ "names that end with \"Test\" or \"Tests\". " //
					+ "When this option is repeated, all patterns will be combined using OR semantics.") //
				.withRequiredArg() //
				.defaultsTo(ClassNameFilter.STANDARD_INCLUDE_PATTERN);

		includePackage = parser.accepts("include-package",
			"Provide a package to be included in the test run. This option can be repeated.") //
				.withRequiredArg();
		excludePackage = parser.accepts("exclude-package",
			"Provide a package to be excluded from the test run. This option can be repeated.") //
				.withRequiredArg();

		includeTag = parser.acceptsAll(asList("t", "include-tag"),
			"Provide a tag to be included in the test run. This option can be repeated.") //
				.withRequiredArg();
		excludeTag = parser.acceptsAll(asList("T", "exclude-tag"),
			"Provide a tag to be excluded from the test run. This option can be repeated.") //
				.withRequiredArg();

		includeEngine = parser.acceptsAll(asList("e", "include-engine"),
			"Provide the ID of an engine to be included in the test run. This option can be repeated.") //
				.withRequiredArg();
		excludeEngine = parser.acceptsAll(asList("E", "exclude-engine"),
			"Provide the ID of an engine to be excluded from the test run. This option can be repeated.") //
				.withRequiredArg();
	}

	OptionParser getParser() {
		return parser;
	}

	CommandLineOptions toCommandLineOptions(OptionSet detectedOptions) {

		CommandLineOptions result = new CommandLineOptions();

		// General Purpose
		result.setDisplayHelp(detectedOptions.has(this.help));
		result.setAnsiColorOutputDisabled(detectedOptions.has(this.disableAnsiColors));
		result.setDetails(detectedOptions.valueOf(this.details));
		result.setAdditionalClasspathEntries(detectedOptions.valuesOf(this.additionalClasspathEntries));

		// Reports
		result.setReportsDir(detectedOptions.valueOf(this.reportsDir));

		// Selectors
		result.setScanClasspath(detectedOptions.has(this.selectedClasspathEntries));
		result.setSelectedClasspathEntries(detectedOptions.valuesOf(this.selectedClasspathEntries));
		result.setSelectedUris(detectedOptions.valuesOf(this.selectedUris));
		result.setSelectedFiles(detectedOptions.valuesOf(this.selectedFiles));
		result.setSelectedDirectories(detectedOptions.valuesOf(this.selectedDirectories));
		result.setSelectedPackages(detectedOptions.valuesOf(this.selectedPackages));
		result.setSelectedClasses(detectedOptions.valuesOf(this.selectedClasses));
		result.setSelectedMethods(detectedOptions.valuesOf(this.selectedMethods));
		result.setSelectedClasspathResources(detectedOptions.valuesOf(this.selectedClasspathResources));

		// Filters
		result.setIncludedClassNamePatterns(detectedOptions.valuesOf(this.includeClassNamePattern));
		result.setIncludedPackages(detectedOptions.valuesOf(this.includePackage));
		result.setExcludedPackages(detectedOptions.valuesOf(this.excludePackage));
		result.setIncludedTags(detectedOptions.valuesOf(this.includeTag));
		result.setExcludedTags(detectedOptions.valuesOf(this.excludeTag));
		result.setIncludedEngines(detectedOptions.valuesOf(this.includeEngine));
		result.setExcludedEngines(detectedOptions.valuesOf(this.excludeEngine));

		return result;
	}

}
