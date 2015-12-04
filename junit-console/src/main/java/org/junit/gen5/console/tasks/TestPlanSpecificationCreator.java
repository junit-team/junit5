/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console.tasks;

import static org.junit.gen5.engine.TestPlanSpecification.allTests;
import static org.junit.gen5.engine.TestPlanSpecification.byTags;
import static org.junit.gen5.engine.TestPlanSpecification.classNameMatches;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.console.options.CommandLineOptions;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestPlanSpecificationElement;

class TestPlanSpecificationCreator {

	TestPlanSpecification toTestPlanSpecification(CommandLineOptions options) {
		TestPlanSpecification testPlanSpecification;
		if (options.isRunAllTests()) {
			Set<File> rootDirectoriesToScan = new HashSet<>();
			if (options.getArguments().isEmpty()) {
				rootDirectoriesToScan.addAll(ReflectionUtils.getAllClasspathRootDirectories());
			}
			else {
				options.getArguments().stream().map(File::new).forEach(rootDirectoriesToScan::add);
			}
			testPlanSpecification = TestPlanSpecification.build(allTests(rootDirectoriesToScan));
		}
		else {
			testPlanSpecification = TestPlanSpecification.build(testPlanSpecificationElementsFromArguments(options));
		}
		options.getClassnameFilter().ifPresent(
			classnameFilter -> testPlanSpecification.filterWith(classNameMatches(classnameFilter)));
		if (!options.getTagsFilter().isEmpty()) {
			testPlanSpecification.filterWith(byTags(options.getTagsFilter()));
		}
		return testPlanSpecification;
	}

	private List<TestPlanSpecificationElement> testPlanSpecificationElementsFromArguments(CommandLineOptions options) {
		Preconditions.notEmpty(options.getArguments(), "No arguments given");
		return TestPlanSpecification.forNames(options.getArguments());
	}
}
