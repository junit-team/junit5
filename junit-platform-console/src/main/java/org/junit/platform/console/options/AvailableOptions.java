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

import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.platform.engine.discovery.ClassNameFilter;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;
import picocli.CommandLine.ParseResult;
import picocli.CommandLine.Spec;

/**
 * @since 1.0
 */
@Command(name = "ConsoleLauncher", sortOptions = false, description = "Launches the JUnit Platform from the console.")
class AvailableOptions {

	private static final String CP_OPTION = "cp";

	// --- General Purpose -------------------------------------------------

	@Option(names = { "-h", "--help" }, usageHelp = true, description = "Display help information.")
	private boolean helpRequested;

	@Option(names = { "--h", "-help" }, help = true, hidden = true)
	private boolean helpRequested2;

	@Option(names = "--disable-ansi-colors", description = "Disable ANSI colors in output (not supported by all terminals).")
	private boolean disableAnsiColors;

	@Option(names = "-disable-ansi-colors", hidden = true)
	private boolean disableAnsiColors2;

	@Option(names = "--disable-banner", description = "Disable print out of the welcome message.")
	private boolean disableBanner;

	@Option(names = "-disable-banner", hidden = true)
	private boolean disableBanner2;

	@Option(names = "--details", paramLabel = "MODE", description = "Select an output details mode for when tests are executed. " //
			+ "Use one of: ${COMPLETION-CANDIDATES}. If 'none' is selected, " //
			+ "then only the summary and test failures are shown. Default: ${DEFAULT-VALUE}.")
	private Details details = CommandLineOptions.DEFAULT_DETAILS;

	@Option(names = "-details", hidden = true)
	private Details details2 = CommandLineOptions.DEFAULT_DETAILS;

	@Option(names = "--details-theme", paramLabel = "THEME", description = "Select an output details tree theme for when tests are executed. "
			+ "Use one of: ${COMPLETION-CANDIDATES}. Default: ${DEFAULT-VALUE}.")
	private Theme theme = CommandLineOptions.DEFAULT_THEME;

	@Option(names = "-details-theme", hidden = true)
	private Theme theme2 = CommandLineOptions.DEFAULT_THEME;

	@Option(names = { "-cp", "--classpath",
			"--class-path" }, split = ";|:", paramLabel = "PATH", arity = "1", description = "Provide additional classpath entries "
					+ "-- for example, for adding engines and their dependencies. This option can be repeated.")
	private List<Path> additionalClasspathEntries = new ArrayList<>();

	@Option(names = { "--cp", "-classpath", "-class-path" }, split = ";|:", hidden = true)
	private List<Path> additionalClasspathEntries2 = new ArrayList<>();

	@Option(names = "--fail-if-no-tests", description = "Fail and return exit status code 2 if no tests are found.")
	private boolean failIfNoTests; // no single-dash equivalent: was introduced in 5.3-M1

	// --- Reports ---------------------------------------------------------

	@Option(names = "--reports-dir", paramLabel = "DIR", description = "Enable report output into a specified local directory (will be created if it does not exist).")
	private Path reportsDir;

	@Option(names = "-reports-dir", hidden = true)
	private Path reportsDir2;

	// --- Java Platform Module System -------------------------------------

	@Option(names = "--scan-modules", description = "EXPERIMENTAL: Scan all resolved modules for test discovery.")
	private boolean scanModulepath;

	@Option(names = "-scan-modules", hidden = true)
	private boolean scanModulepath2;

	@Option(names = { "-o",
			"--select-module" }, paramLabel = "NAME", arity = "1", description = "EXPERIMENTAL: Select single module for test discovery. This option can be repeated.")
	private List<String> selectedModules = new ArrayList<>();

	@Option(names = { "--o", "-select-module" }, arity = "1", hidden = true)
	private List<String> selectedModules2 = new ArrayList<>();

	// --- Selectors -------------------------------------------------------

	@Option(names = { "--scan-class-path",
			"--scan-classpath" }, split = ";|:", paramLabel = "PATH", arity = "0..1", description = "Scan all directories on the classpath or explicit classpath roots. " //
					+ "Without arguments, only directories on the system classpath as well as additional classpath " //
					+ "entries supplied via -" + CP_OPTION + " (directories and JAR files) are scanned. " //
					+ "Explicit classpath roots that are not on the classpath will be silently ignored. " //
					+ "This option can be repeated.")
	private List<Path> selectedClasspathEntries = new ArrayList<>();

	@Option(names = { "-scan-class-path", "-scan-classpath" }, split = ";|:", arity = "0..1", hidden = true)
	private List<Path> selectedClasspathEntries2 = new ArrayList<>();

	@Option(names = { "-u",
			"--select-uri" }, paramLabel = "URI", arity = "1", description = "Select a URI for test discovery. This option can be repeated.")
	private List<URI> selectedUris = new ArrayList<>();

	@Option(names = { "--u", "-select-uri" }, arity = "1", hidden = true)
	private List<URI> selectedUris2 = new ArrayList<>();

	@Option(names = { "-f",
			"--select-file" }, paramLabel = "FILE", arity = "1", description = "Select a file for test discovery. This option can be repeated.")
	private List<String> selectedFiles = new ArrayList<>();

	@Option(names = { "--f", "-select-file" }, arity = "1", hidden = true)
	private List<String> selectedFiles2 = new ArrayList<>();

	@Option(names = { "-d",
			"--select-directory" }, paramLabel = "DIR", arity = "1", description = "Select a directory for test discovery. This option can be repeated.")
	private List<String> selectedDirectories = new ArrayList<>();

	@Option(names = { "--d", "-select-directory" }, arity = "1", hidden = true)
	private List<String> selectedDirectories2 = new ArrayList<>();

	@Option(names = { "-p",
			"--select-package" }, paramLabel = "PKG", arity = "1", description = "Select a package for test discovery. This option can be repeated.")
	private List<String> selectedPackages = new ArrayList<>();

	@Option(names = { "--p", "-select-package" }, arity = "1", hidden = true)
	private List<String> selectedPackages2 = new ArrayList<>();

	@Option(names = { "-c",
			"--select-class" }, paramLabel = "CLASS", arity = "1", description = "Select a class for test discovery. This option can be repeated.")
	private List<String> selectedClasses = new ArrayList<>();

	@Option(names = { "--c", "-select-class" }, arity = "1", hidden = true)
	private List<String> selectedClasses2 = new ArrayList<>();

	@Option(names = { "-m",
			"--select-method" }, paramLabel = "NAME", arity = "1", description = "Select a method for test discovery. This option can be repeated.")
	private List<String> selectedMethods = new ArrayList<>();

	@Option(names = { "--m", "-select-method" }, arity = "1", hidden = true)
	private List<String> selectedMethods2 = new ArrayList<>();

	@Option(names = { "-r",
			"--select-resource" }, paramLabel = "RESOURCE", arity = "1", description = "Select a classpath resource for test discovery. This option can be repeated.")
	private List<String> selectedClasspathResources = new ArrayList<>();

	@Option(names = { "--r", "-select-resource" }, arity = "1", hidden = true)
	private List<String> selectedClasspathResources2 = new ArrayList<>();

	// --- Filters ---------------------------------------------------------

	@Option(names = { "-n",
			"--include-classname" }, paramLabel = "PATTERN", arity = "1", description = "Provide a regular expression to include only classes whose fully qualified names match. " //
					+ "To avoid loading classes unnecessarily, the default pattern only includes class " //
					+ "names that begin with \"Test\" or end with \"Test\" or \"Tests\". " //
					+ "When this option is repeated, all patterns will be combined using OR semantics. " //
					+ "Default: ${DEFAULT-VALUE}")
	private List<String> includeClassNamePatterns = new ArrayList<>(
		Arrays.asList(ClassNameFilter.STANDARD_INCLUDE_PATTERN));

	@Option(names = { "--n", "-include-classname" }, arity = "1", hidden = true)
	private List<String> includeClassNamePatterns2 = new ArrayList<>();

	@Option(names = { "-N",
			"--exclude-classname" }, paramLabel = "PATTERN", arity = "1", description = "Provide a regular expression to exclude those classes whose fully qualified names match. " //
					+ "When this option is repeated, all patterns will be combined using OR semantics.")
	private List<String> excludeClassNamePatterns = new ArrayList<>();

	@Option(names = { "--N", "-exclude-classname" }, arity = "1", hidden = true)
	private List<String> excludeClassNamePatterns2 = new ArrayList<>();

	@Option(names = {
			"--include-package" }, paramLabel = "PKG", arity = "1", description = "Provide a package to be included in the test run. This option can be repeated.")
	private List<String> includePackages = new ArrayList<>();

	@Option(names = { "-include-package" }, arity = "1", hidden = true)
	private List<String> includePackages2 = new ArrayList<>();

	@Option(names = {
			"--exclude-package" }, paramLabel = "PKG", arity = "1", description = "Provide a package to be excluded from the test run. This option can be repeated.")
	private List<String> excludePackages = new ArrayList<>();

	@Option(names = { "-exclude-package" }, arity = "1", hidden = true)
	private List<String> excludePackages2 = new ArrayList<>();

	@Option(names = { "-t",
			"--include-tag" }, paramLabel = "TAG", arity = "1", description = "Provide a tag or tag expression to include only tests whose tags match. "
					+ //
					"When this option is repeated, all patterns will be combined using OR semantics.")
	private List<String> includedTags = new ArrayList<>();

	@Option(names = { "--t", "-include-tag" }, arity = "1", hidden = true)
	private List<String> includedTags2 = new ArrayList<>();

	@Option(names = { "-T",
			"--exclude-tag" }, paramLabel = "TAG", arity = "1", description = "Provide a tag or tag expression to exclude those tests whose tags match. "
					+ //
					"When this option is repeated, all patterns will be combined using OR semantics.")
	private List<String> excludedTags = new ArrayList<>();

	@Option(names = { "--T", "-exclude-tag" }, arity = "1", hidden = true)
	private List<String> excludedTags2 = new ArrayList<>();

	@Option(names = { "-e",
			"--include-engine" }, paramLabel = "ID", arity = "1", description = "Provide the ID of an engine to be included in the test run. This option can be repeated.")
	private List<String> includedEngines = new ArrayList<>();

	@Option(names = { "--e", "-include-engine" }, arity = "1", hidden = true)
	private List<String> includedEngines2 = new ArrayList<>();

	@Option(names = { "-E",
			"--exclude-engine" }, paramLabel = "ID", arity = "1", description = "Provide the ID of an engine to be excluded from the test run. This option can be repeated.")
	private List<String> excludedEngines = new ArrayList<>();

	@Option(names = { "--E", "-exclude-engine" }, arity = "1", hidden = true)
	private List<String> excludedEngines2 = new ArrayList<>();

	// --- Configuration Parameters ----------------------------------------

	// Implementation note: the @Option annotation is on a setter method to allow validation.
	private Map<String, String> configurationParameters = new LinkedHashMap<>();

	@Spec
	private CommandSpec spec;

	AvailableOptions() {
	}

	/**
	 * Adds the specified key-value pair (or pairs) to the configuration parameters.
	 * A {@code ParameterException} is thrown if the same key is specified multiple times
	 * on the command line.
	 *
	 * @param map the key-value pairs to add
	 * @throws picocli.CommandLine.ParameterException if the map already contains this key
	 * @see <a href="https://github.com/junit-team/junit5/issues/1308">#1308</a>
	 */
	@Option(names = "--config", paramLabel = "KEY=VALUE", arity = "1", description = "Set a configuration parameter for test discovery and execution. This option can be repeated.")
	public void setConfigurationParameters(Map<String, String> map) {
		for (String key : map.keySet()) {
			String newValue = map.get(key);
			validateUnique(key, newValue);
			configurationParameters.put(key, newValue);
		}
	}

	private void validateUnique(String key, String newValue) {
		String existing = configurationParameters.get(key);
		if (existing != null && !existing.equals(newValue)) {
			throw new ParameterException(spec.commandLine(),
				String.format("Duplicate key '%s' for values '%s' and '%s'.", key, existing, newValue));
		}
	}

	@Option(names = { "-config" }, arity = "1", hidden = true)
	public void setConfigurationParameters2(Map<String, String> keyValuePairs) {
		setConfigurationParameters(keyValuePairs);
	}

	CommandLine getParser() {
		CommandLine result = new CommandLine(this);
		result.setUsageHelpWidth(90);
		result.setCaseInsensitiveEnumValuesAllowed(true);
		result.setAtFileCommentChar(null); // for --select-method com.acme.Foo#m()
		return result;
	}

	CommandLineOptions toCommandLineOptions(ParseResult parseResult) {

		CommandLineOptions result = new CommandLineOptions();

		// General Purpose
		result.setDisplayHelp(this.helpRequested || this.helpRequested2);
		result.setAnsiColorOutputDisabled(this.disableAnsiColors || this.disableAnsiColors2);
		result.setBannerDisabled(this.disableBanner || this.disableBanner2);
		result.setDetails(choose(this.details, this.details2, CommandLineOptions.DEFAULT_DETAILS));
		result.setTheme(choose(this.theme, this.theme2, CommandLineOptions.DEFAULT_THEME));
		result.setAdditionalClasspathEntries(merge(this.additionalClasspathEntries, this.additionalClasspathEntries2));
		result.setFailIfNoTests(this.failIfNoTests);

		// Reports
		result.setReportsDir(choose(this.reportsDir, this.reportsDir2, null));

		// Java Platform Module System
		result.setScanModulepath(this.scanModulepath || this.scanModulepath2);
		result.setSelectedModules(merge(this.selectedModules, this.selectedModules2));

		// Selectors
		result.setScanClasspath(parseResult.hasMatchedOption("scan-class-path")); // flag was specified
		result.setSelectedClasspathEntries(merge(this.selectedClasspathEntries, this.selectedClasspathEntries2));
		result.setSelectedUris(merge(this.selectedUris, this.selectedUris2));
		result.setSelectedFiles(merge(this.selectedFiles, this.selectedFiles2));
		result.setSelectedDirectories(merge(this.selectedDirectories, this.selectedDirectories2));
		result.setSelectedPackages(merge(this.selectedPackages, this.selectedPackages2));
		result.setSelectedClasses(merge(this.selectedClasses, this.selectedClasses2));
		result.setSelectedMethods(merge(this.selectedMethods, this.selectedMethods2));
		result.setSelectedClasspathResources(merge(this.selectedClasspathResources, this.selectedClasspathResources2));

		// Filters
		result.setIncludedClassNamePatterns(merge(this.includeClassNamePatterns, this.includeClassNamePatterns2));
		result.setExcludedClassNamePatterns(merge(this.excludeClassNamePatterns, this.excludeClassNamePatterns2));
		result.setIncludedPackages(merge(this.includePackages, this.includePackages2));
		result.setExcludedPackages(merge(this.excludePackages, this.excludePackages2));
		result.setIncludedTagExpressions(merge(this.includedTags, this.includedTags2));
		result.setExcludedTagExpressions(merge(this.excludedTags, this.excludedTags2));
		result.setIncludedEngines(merge(this.includedEngines, this.includedEngines2));
		result.setExcludedEngines(merge(this.excludedEngines, this.excludedEngines2));

		// Configuration Parameters
		result.setConfigurationParameters(this.configurationParameters);

		return result;
	}

	private static <T> List<T> merge(List<T> list1, List<T> list2) {
		List<T> result = new ArrayList<>(list1);
		result.addAll(list2);
		return result;
	}

	private static <T> T choose(T left, T right, T defaultValue) {
		return left == right ? left : (left == defaultValue ? right : left);
	}
}
