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
import static org.junit.gen5.engine.specification.dsl.ClassFilters.classNameMatches;
import static org.junit.gen5.engine.specification.dsl.DiscoveryRequestBuilder.request;

import java.io.File;
import java.util.Set;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.console.options.CommandLineOptions;
import org.junit.gen5.engine.DiscoveryRequest;
import org.junit.gen5.engine.specification.dsl.ClasspathTestPlanSpecificationElementBuilder;
import org.junit.gen5.engine.specification.dsl.NamedTestPlanSpecificationElementBuilder;
import org.junit.gen5.engine.specification.dsl.TagFilterBuilder;

class TestPlanSpecificationCreator {

	DiscoveryRequest toTestPlanSpecification(CommandLineOptions options) {
		DiscoveryRequest specification = buildSpecification(options);
		applyFilters(specification, options);
		return specification;
	}

	private DiscoveryRequest buildSpecification(CommandLineOptions options) {
		if (options.isRunAllTests()) {
			return buildAllTestsSpecification(options);
		}
		return buildNameBasedSpecification(options);
	}

	private DiscoveryRequest buildAllTestsSpecification(CommandLineOptions options) {
		Set<File> rootDirectoriesToScan = determineClasspathRootDirectories(options);
		return request().select(ClasspathTestPlanSpecificationElementBuilder.allTests(rootDirectoriesToScan)).build();
	}

	private Set<File> determineClasspathRootDirectories(CommandLineOptions options) {
		if (options.getArguments().isEmpty()) {
			return ReflectionUtils.getAllClasspathRootDirectories();
		}
		return options.getArguments().stream().map(File::new).collect(toSet());
	}

	private DiscoveryRequest buildNameBasedSpecification(CommandLineOptions options) {
		Preconditions.notEmpty(options.getArguments(), "No arguments given");
		return request().select(NamedTestPlanSpecificationElementBuilder.forNames(options.getArguments())).build();
	}

	private void applyFilters(DiscoveryRequest specification, CommandLineOptions options) {
		options.getClassnameFilter().ifPresent(regex -> specification.addEngineFilter(classNameMatches(regex)));
		if (!options.getTagsFilter().isEmpty()) {
			specification.addDescriptorFilter(TagFilterBuilder.includeTags(options.getTagsFilter()));
		}
		if (!options.getExcludeTags().isEmpty()) {
			specification.addDescriptorFilter(TagFilterBuilder.excludeTags(options.getExcludeTags()));
		}
	}
}
