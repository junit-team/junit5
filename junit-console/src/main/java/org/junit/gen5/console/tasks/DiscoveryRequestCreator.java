/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console.tasks;

import static java.util.stream.Collectors.toCollection;
import static org.junit.gen5.engine.discovery.ClasspathSelector.selectClasspathRoots;
import static org.junit.gen5.engine.discovery.NameBasedSelector.selectNames;
import static org.junit.gen5.launcher.EngineIdFilter.includeEngineId;
import static org.junit.gen5.launcher.TagFilter.excludeTags;
import static org.junit.gen5.launcher.TagFilter.requireTags;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.console.options.CommandLineOptions;
import org.junit.gen5.engine.discovery.ClassFilter;
import org.junit.gen5.launcher.TestDiscoveryRequest;

/**
 * @since 5.0
 */
class DiscoveryRequestCreator {

	TestDiscoveryRequest toDiscoveryRequest(CommandLineOptions options) {
		TestDiscoveryRequest discoveryRequest = buildDiscoveryRequest(options);
		applyFilters(discoveryRequest, options);
		return discoveryRequest;
	}

	private TestDiscoveryRequest buildDiscoveryRequest(CommandLineOptions options) {
		if (options.isRunAllTests()) {
			return buildDiscoveryRequestForAllTests(options);
		}
		return buildNameBasedDiscoveryRequest(options);
	}

	private TestDiscoveryRequest buildDiscoveryRequestForAllTests(CommandLineOptions options) {
		Set<File> rootDirectoriesToScan = determineClasspathRootDirectories(options);
		return request().selectors(selectClasspathRoots(rootDirectoriesToScan)).build();
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

	private TestDiscoveryRequest buildNameBasedDiscoveryRequest(CommandLineOptions options) {
		Preconditions.notEmpty(options.getArguments(), "No arguments given");
		return request().selectors(selectNames(options.getArguments())).build();
	}

	private void applyFilters(TestDiscoveryRequest discoveryRequest, CommandLineOptions options) {
		options.getClassnameFilter().ifPresent(
			regex -> discoveryRequest.addFilter(ClassFilter.byClassNamePattern(regex)));
		if (!options.getRequiredTagsFilter().isEmpty()) {
			discoveryRequest.addPostFilter(requireTags(options.getRequiredTagsFilter()));
		}
		if (!options.getExcludedTagsFilter().isEmpty()) {
			discoveryRequest.addPostFilter(excludeTags(options.getExcludedTagsFilter()));
		}
		options.getRequiredEngineFilter().ifPresent(
			engineId -> discoveryRequest.addEngineIdFilter(includeEngineId(engineId)));
	}

}
