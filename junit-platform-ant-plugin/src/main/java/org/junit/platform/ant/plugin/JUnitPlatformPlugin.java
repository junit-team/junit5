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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Resource;
import org.junit.platform.console.options.CommandLineOptions;
import org.junit.platform.console.options.CommandLineOptionsParser;
import org.junit.platform.console.options.JOptSimpleCommandLineOptionsParser;
import org.junit.platform.console.tasks.ConsoleTask;
import org.junit.platform.console.tasks.ConsoleTaskExecutor;
import org.junit.platform.console.tasks.DisplayHelpTask;
import org.junit.platform.console.tasks.ExecuteTestsTask;

public class JUnitPlatformPlugin extends Task {

	private static final String classpath = "--classpath";
	private static final String reportsDir = "--reports-dir";

	private static final String selectUris = "--select-uri";
	private static final String selectFiles = "--select-file";
	private static final String selectDirectories = "--select-directory";
	private static final String selectPackages = "--select-package";
	private static final String selectClasses = "--select-class";
	private static final String selectMethods = "--select-method";
	private static final String selectResources = "--select-resource";

	private static final String scanClasspath = "--scan-classpath";
	private static final String includeClassnames = "--include-classname";
	private static final String includePackage = "--include-package";
	private static final String excludePackage = "--exclude-package";
	private static final String includeEngine = "--include-engine";
	private static final String excludeEngine = "--exclude-engine";
	private static final String includeTag = "--include-tag";
	private static final String excludeTag = "--exclude-tag";

	private final CommandLineOptionsParser commandLineOptionsParser;

	private Path path;
	private String reportsPath;
	private Filters filters;
	private Selectors selectors;

	public JUnitPlatformPlugin() {
		super();
		this.commandLineOptionsParser = new JOptSimpleCommandLineOptionsParser();
	}

	// For testing only
	JUnitPlatformPlugin(CommandLineOptionsParser commandLineOPtionsParser) {
		super();
		this.commandLineOptionsParser = commandLineOPtionsParser;
	}

	public void execute() {
		List<String> args = new ArrayList<>();
		buildArguments(args);
		CommandLineOptions options = commandLineOptionsParser.parse(args.toArray(new String[0]));
		ConsoleTask task = new ExecuteTestsTask(options);
		ConsoleTaskExecutor consoleTaskExecutor = new ConsoleTaskExecutor(System.out, System.err);
		consoleTaskExecutor.executeTask(task, this::displayHelp);
	}

	private void buildArguments(List<String> args) {
		addGeneralPurposeArgs(args);
		addSelectors(args);
		addFilters(args);
	}

	private void addGeneralPurposeArgs(List<String> args) {
		if (path != null) {
			args.add(classpath);
			args.add(translatePath(this.path));
		}
		if (reportsPath != null) {
			args.add(reportsDir);
			args.add(reportsPath);
		}
	}

	private String translatePath(Path pathToTranslate) {
		Iterator<Resource> it = pathToTranslate.iterator();
		Iterable<Resource> iterable = () -> it;

		String cp = StreamSupport.stream(iterable.spliterator(), false).map(Resource::toString).collect(
			Collectors.joining(":"));
		return cp;
	}

	private void addSelectors(List<String> args) {
		if (this.selectors != null) {
			if (this.selectors.getClasspath() != null) {
				args.add(scanClasspath);
				args.add(translatePath(this.selectors.getClasspath()));
			}
			populateRepeatingArguments(args, selectUris, selectors.getUris());
			populateRepeatingArguments(args, selectFiles, selectors.getFiles());
			populateRepeatingArguments(args, selectDirectories, selectors.getDirectories());
			populateRepeatingArguments(args, selectPackages, selectors.getPackages());
			populateRepeatingArguments(args, selectClasses, selectors.getClasses());
			populateRepeatingArguments(args, selectMethods, selectors.getMethods());
			populateRepeatingArguments(args, selectResources, selectors.getResources());
		}
	}

	private void addFilters(List<String> args) {
		if (filters != null) {
			populateRepeatingArguments(args, includeClassnames, filters.getIncludeClassNamePatterns());
			if (filters.getPackages() != null) {
				populateRepeatingArguments(args, includePackage, filters.getPackages().getInclude());
				populateRepeatingArguments(args, excludePackage, filters.getPackages().getExclude());
			}
			if (filters.getEngines() != null) {
				populateRepeatingArguments(args, includeEngine, filters.getEngines().getInclude());
				populateRepeatingArguments(args, excludeEngine, filters.getEngines().getExclude());
			}
			if (filters.getTags() != null) {
				populateRepeatingArguments(args, includeTag, filters.getTags().getInclude());
				populateRepeatingArguments(args, excludeTag, filters.getTags().getExclude());
			}
		}
	}

	private void populateRepeatingArguments(List<String> args, String paramName, List<String> repeatableArgument) {
		repeatableArgument.forEach(arg -> {
			args.add(paramName);
			args.add(arg);
		});
	}

	void displayHelp(PrintWriter out) {
		new DisplayHelpTask(commandLineOptionsParser).execute(out);
	}

	public void addClasspath(Path path) {
		this.path = path;
	}

	public void setReportsDir(String reportsDir) {
		this.reportsPath = reportsDir;
	}

	public void addFilters(Filters filters) {
		this.filters = filters;
	}

	public void addSelectors(Selectors selectors) {
		this.selectors = selectors;
	}
}
