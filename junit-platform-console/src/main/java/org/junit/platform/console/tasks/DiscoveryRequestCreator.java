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
import static org.junit.platform.engine.discovery.ClassFilter.includeClassNamePattern;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathRoots;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectNames;
import static org.junit.platform.launcher.EngineFilter.excludeEngines;
import static org.junit.platform.launcher.EngineFilter.includeEngines;
import static org.junit.platform.launcher.TagFilter.excludeTags;
import static org.junit.platform.launcher.TagFilter.includeTags;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.console.options.CommandLineOptions;
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
		if (options.isRunAllTests()) {
			return createBuilderForAllTests(options);
		}
		return createNameBasedBuilder(options);
	}

	private LauncherDiscoveryRequestBuilder createBuilderForAllTests(CommandLineOptions options) {
		Set<File> rootDirectoriesToScan = determineClasspathRootDirectories(options);
		return request().selectors(selectClasspathRoots(rootDirectoriesToScan));
	}

	private Set<File> determineClasspathRootDirectories(CommandLineOptions options) {
		if (options.getArguments().isEmpty()) {
			Set<File> rootDirs = new LinkedHashSet<>(ReflectionUtils.getAllClasspathRootDirectories());
			if (!options.getAdditionalClasspathEntries().isEmpty()) {
				rootDirs.addAll(new ClasspathEntriesParser().toDirectories(options.getAdditionalClasspathEntries()));
			}
			return rootDirs;
		}
		return options.getArguments().stream().map(File::new).collect(toCollection(LinkedHashSet::new));
	}

	@SuppressWarnings("deprecation")
	private LauncherDiscoveryRequestBuilder createNameBasedBuilder(CommandLineOptions options) {
		Preconditions.notEmpty(options.getArguments(), "No arguments were supplied to the ConsoleLauncher");
		return request().selectors(selectNames(options.getArguments()));
	}

	private void addFilters(LauncherDiscoveryRequestBuilder requestBuilder, CommandLineOptions options) {
		options.getIncludeClassNamePattern().ifPresent(
			pattern -> requestBuilder.filters(includeClassNamePattern(pattern)));

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
