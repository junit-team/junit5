/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console.tasks;

import static java.util.stream.Collectors.toCollection;
import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathRoots;
import static org.junit.platform.launcher.EngineFilter.excludeEngines;
import static org.junit.platform.launcher.EngineFilter.includeEngines;
import static org.junit.platform.launcher.TagFilter.excludeTags;
import static org.junit.platform.launcher.TagFilter.includeTags;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.console.options.CommandLineOptions;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;

/**
 * @since 1.0
 */
class DiscoveryRequestCreator {

	LauncherDiscoveryRequest toDiscoveryRequest(CommandLineOptions options) {
		LauncherDiscoveryRequestBuilder requestBuilder = createRequestBuilder(options);
		addFilters(requestBuilder, options);
		return requestBuilder.build();
	}

	private LauncherDiscoveryRequestBuilder createRequestBuilder(CommandLineOptions options) {
		if (options.isScanClasspath()) {
			Preconditions.condition(!options.hasExplicitSelectors(),
				"Scanning the classpath and using explicit selectors at the same time is not supported");
			return createBuilderForClasspathScanning(options);
		}
		return createBuilderForExplicitSelectors(options);
	}

	private LauncherDiscoveryRequestBuilder createBuilderForClasspathScanning(CommandLineOptions options) {
		Set<Path> rootDirectoriesToScan = determineClasspathRootDirectories(options);
		return request().selectors(selectClasspathRoots(rootDirectoriesToScan));
	}

	private Set<Path> determineClasspathRootDirectories(CommandLineOptions options) {
		if (options.getArguments().isEmpty()) {
			Set<Path> rootDirs = new LinkedHashSet<>(ReflectionUtils.getAllClasspathRootDirectories());
			rootDirs.addAll(options.getAdditionalClasspathEntries());
			return rootDirs;
		}
		return options.getArguments().stream().map(Paths::get).collect(toCollection(LinkedHashSet::new));
	}

	private LauncherDiscoveryRequestBuilder createBuilderForExplicitSelectors(CommandLineOptions options) {
		List<DiscoverySelector> selectors = new LinkedList<>();
		options.getSelectedUris().stream().map(DiscoverySelectors::selectUri).forEach(selectors::add);
		options.getSelectedFiles().stream().map(DiscoverySelectors::selectFile).forEach(selectors::add);
		options.getSelectedDirectories().stream().map(DiscoverySelectors::selectDirectory).forEach(selectors::add);
		options.getSelectedPackages().stream().map(DiscoverySelectors::selectPackage).forEach(selectors::add);
		options.getSelectedClasses().stream().map(DiscoverySelectors::selectClass).forEach(selectors::add);
		options.getSelectedMethods().stream().map(DiscoverySelectors::selectMethod).forEach(selectors::add);
		options.getSelectedClasspathResources().stream().map(DiscoverySelectors::selectClasspathResource).forEach(
			selectors::add);
		Preconditions.notEmpty(selectors, "No arguments were supplied to the ConsoleLauncher");
		return request().selectors(selectors);
	}

	private void addFilters(LauncherDiscoveryRequestBuilder requestBuilder, CommandLineOptions options) {
		requestBuilder.filters(includeClassNamePatterns(options.getIncludedClassNamePatterns().toArray(new String[0])));

		if (!options.getIncludedTags().isEmpty()) {
			requestBuilder.filters(includeTags(options.getIncludedTags()));
		}

		if (!options.getExcludedTags().isEmpty()) {
			requestBuilder.filters(excludeTags(options.getExcludedTags()));
		}

		if (!options.getIncludedEngines().isEmpty()) {
			requestBuilder.filters(includeEngines(options.getIncludedEngines()));
		}

		if (!options.getExcludedEngines().isEmpty()) {
			requestBuilder.filters(excludeEngines(options.getExcludedEngines()));
		}
	}

}
