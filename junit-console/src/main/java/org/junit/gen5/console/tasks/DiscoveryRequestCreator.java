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

import static java.util.stream.Collectors.toSet;
import static org.junit.gen5.engine.discoveryrequest.dsl.ClassFilters.classNameMatches;
import static org.junit.gen5.engine.discoveryrequest.dsl.ClasspathSelectorBuilder.byPaths;
import static org.junit.gen5.engine.discoveryrequest.dsl.DiscoveryRequestBuilder.request;
import static org.junit.gen5.engine.discoveryrequest.dsl.NameBasedSelectorBuilder.byNames;

import java.io.File;
import java.util.Set;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.console.options.CommandLineOptions;
import org.junit.gen5.engine.DiscoveryRequest;
import org.junit.gen5.engine.discoveryrequest.dsl.TagFilterBuilder;

class DiscoveryRequestCreator {
	DiscoveryRequest toDiscoveryRequest(CommandLineOptions options) {
		DiscoveryRequest discoveryRequest = buildDiscoveryRequest(options);
		applyFilters(discoveryRequest, options);
		return discoveryRequest;
	}

	private DiscoveryRequest buildDiscoveryRequest(CommandLineOptions options) {
		if (options.isRunAllTests()) {
			return buildDiscoveryRequestForAllTests(options);
		}
		return buildNameBasedDiscoveryRequest(options);
	}

	private DiscoveryRequest buildDiscoveryRequestForAllTests(CommandLineOptions options) {
		Set<File> rootDirectoriesToScan = determineClasspathRootDirectories(options);
		return request().select(byPaths(rootDirectoriesToScan)).build();
	}

	private Set<File> determineClasspathRootDirectories(CommandLineOptions options) {
		if (options.getArguments().isEmpty()) {
			return ReflectionUtils.getAllClasspathRootDirectories();
		}
		return options.getArguments().stream().map(File::new).collect(toSet());
	}

	private DiscoveryRequest buildNameBasedDiscoveryRequest(CommandLineOptions options) {
		Preconditions.notEmpty(options.getArguments(), "No arguments given");
		return request().select(byNames(options.getArguments())).build();
	}

	private void applyFilters(DiscoveryRequest discoveryRequest, CommandLineOptions options) {
		options.getClassnameFilter().ifPresent(regex -> discoveryRequest.addFilter(classNameMatches(regex)));
		if (!options.getTagsFilter().isEmpty()) {
			discoveryRequest.addPostFilter(TagFilterBuilder.includeTags(options.getTagsFilter()));
		}
		if (!options.getExcludeTags().isEmpty()) {
			discoveryRequest.addPostFilter(TagFilterBuilder.excludeTags(options.getExcludeTags()));
		}
	}
}
