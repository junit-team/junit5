/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.ant.plugin;

import static java.util.Optional.empty;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.junit.platform.ant.plugin.Filters.FilterSet;
import org.junit.platform.console.options.CommandLineOptions;
import org.junit.platform.console.options.CommandLineOptionsParser;
import org.junit.platform.console.options.JOptSimpleCommandLineOptionsParser;
import org.junit.platform.console.tasks.ConsoleTask;
import org.junit.platform.console.tasks.ConsoleTaskExecutor;
import org.junit.platform.console.tasks.DisplayHelpTask;
import org.junit.platform.console.tasks.ExecuteTestsTask;

public class JUnitPlatformPlugin extends Task {

	private static final String JAVA_CLASS_PATH = "java.class.path";
	
	private static final String HELP = "help";
	private static final String DISABLE_ANSI_COLORS = "disable-ansi-colors";
	private static final String HIDE_DETAILS = "hide-details";
	private static final String REPORTS_DIR = "reports-dir";

	private static final String SELECT_URIS = "select-uri";
	private static final String SELECT_FILES = "select-file";
	private static final String SELECT_DIRECTORIES = "select-directory";
	private static final String SELECT_PACKAGE = "select-package";
	private static final String SELECT_CLASSES = "select-class";
	private static final String SELECT_METHODS = "select-method";
	private static final String SELECT_RESOURCES = "select-resource";

	private static final String SCAN_CLASSPATH = "scan-classpath";
	private static final String INCLUDE_CLASSNAMES = "include-classname";
	private static final String INCLUDE_PACKAGE = "include-package";
	private static final String EXCLUDE_PACKAGE = "exclude-package";
	private static final String INCLUDE_ENGINE = "include-engine";
	private static final String EXCLUDE_ENGINE = "exclude-engine";
	private static final String INCLUDE_TAG = "include-tag";
	private static final String EXCLUDE_TAG = "exclude-tag";

	private final CommandLineOptionsParser commandLineOptionsParser;
	private final ConsoleTaskExecutor consoleTaskExecutor;

	private boolean printHelp = false;
	private boolean ansiColorsDisabled = false;
	private boolean detailsHidden = false;
	private String reportsPath;
	private Optional<Path> scanClasspath = empty();
	private Optional<Filters> filters = empty();
	private Optional<Selectors> selectors = empty();

	public JUnitPlatformPlugin() {
		super();
		this.commandLineOptionsParser = new JOptSimpleCommandLineOptionsParser();
		this.consoleTaskExecutor = new ConsoleTaskExecutor(System.out, System.err);
	}

	// For testing only
	JUnitPlatformPlugin(CommandLineOptionsParser commandLineOPtionsParser, ConsoleTaskExecutor consoleTaskExecutor) {
		super();
		this.commandLineOptionsParser = commandLineOPtionsParser;
		this.consoleTaskExecutor = consoleTaskExecutor;
	}

	@Override
	public void execute() {
		List<String> args = new ArrayList<>();
		buildArguments(args);
		CommandLineOptions options = commandLineOptionsParser.parse(args.toArray(new String[0]));
		ConsoleTask task = printHelp ? new DisplayHelpTask(commandLineOptionsParser) : new ExecuteTestsTask(options);
		consoleTaskExecutor.executeTask(task, this::displayHelp);
	}
	
	void displayHelp(PrintWriter out) {
		new DisplayHelpTask(commandLineOptionsParser).execute(out);
	}

	public void setHelp(boolean help) {
		this.printHelp = help;
	}
	
	public void setDisableAnsiColors(boolean disableAnsiColors) {
		this.ansiColorsDisabled = disableAnsiColors;
	}

	public void setHideDetails(boolean hideDetails) {
		this.detailsHidden = hideDetails;
	}

	public void addScanClasspath(Path scanClasspath) {
		this.scanClasspath = Optional.of(scanClasspath);
	}

	public void setReportsDir(String reportsDir) {
		this.reportsPath = reportsDir;
	}

	public void addFilters(Filters filters) {
		this.filters = Optional.of(filters);
	}

	public void addSelectors(Selectors selectors) {
		this.selectors = Optional.of(selectors);
	}

	private void buildArguments(List<String> args) {
		addGeneralPurposeArgs(args);
		addSelectors(args);
		addFilters(args);
	}

	private void addGeneralPurposeArgs(List<String> args) {
		if (printHelp) {
			args.add("--" + HELP);
		}
		if (ansiColorsDisabled) {
			args.add("--" + DISABLE_ANSI_COLORS);
		}
		if (detailsHidden) {
			args.add("--" + HIDE_DETAILS);
		}
		if (reportsPath != null) {
			args.add("--" + REPORTS_DIR);
			args.add(reportsPath);
		}
	}

	private void addSelectors(List<String> args) {
		selectors.ifPresent(selectors -> {
			populateRepeatingArguments(args, SELECT_URIS, selectors.getUris());
			populateRepeatingArguments(args, SELECT_FILES, selectors.getFiles());
			populateRepeatingArguments(args, SELECT_DIRECTORIES, selectors.getDirectories());
			populateRepeatingArguments(args, SELECT_PACKAGE, selectors.getPackages());
			populateRepeatingArguments(args, SELECT_CLASSES, selectors.getClasses());
			populateRepeatingArguments(args, SELECT_METHODS, selectors.getMethods());
			populateRepeatingArguments(args, SELECT_RESOURCES, selectors.getResources());
		});
		
		if (!selectors.isPresent() && scanClasspath.isPresent()) {
			args.add("--" + SCAN_CLASSPATH);
			args.add(String.join(":", this.scanClasspath.get().list()));
		} else if (!selectors.isPresent() && !scanClasspath.isPresent() && 
				getProject().getProperty(JAVA_CLASS_PATH) != null) {
			args.add("--" + SCAN_CLASSPATH);
			args.add(getProject().getProperty(JAVA_CLASS_PATH));
		}
	}

	private void addFilters(List<String> args) {
		filters.ifPresent(filters -> {
			populateRepeatingArguments(args, INCLUDE_CLASSNAMES, filters.getIncludeClassNamePatterns());
			FilterSet packages = filters.getPackages();
			if (packages != null) {
				populateRepeatingArguments(args, INCLUDE_PACKAGE, packages.getInclude());
				populateRepeatingArguments(args, EXCLUDE_PACKAGE, packages.getExclude());
			}
			FilterSet engines = filters.getEngines();
			if (engines != null) {
				populateRepeatingArguments(args, INCLUDE_ENGINE, engines.getInclude());
				populateRepeatingArguments(args, EXCLUDE_ENGINE, engines.getExclude());
			}
			FilterSet tags = filters.getTags();
			if (tags != null) {
				populateRepeatingArguments(args, INCLUDE_TAG, tags.getInclude());
				populateRepeatingArguments(args, EXCLUDE_TAG, tags.getExclude());
			}
		});
	}

	private void populateRepeatingArguments(List<String> args, String paramName, List<String> repeatableArgument) {
		repeatableArgument.forEach(arg -> {
			args.add("--" + paramName);
			args.add(arg);
		});
	}
}
