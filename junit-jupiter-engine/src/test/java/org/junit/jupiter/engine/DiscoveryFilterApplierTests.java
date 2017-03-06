/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine;

import static org.junit.jupiter.engine.descriptor.TestDescriptorBuilder.classTestDescriptor;
import static org.junit.jupiter.engine.descriptor.TestDescriptorBuilder.engineDescriptor;
import static org.junit.jupiter.engine.descriptor.TestDescriptorBuilder.nestedClassTestDescriptor;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.discovery.ClassNameFilter;
import org.junit.platform.engine.discovery.PackageNameFilter;

/**
 * Microtests for {@link DiscoveryFilterApplier}.
 *
 * @since 5.0
 */
class DiscoveryFilterApplierTests {

	DiscoveryFilterApplier applier = new DiscoveryFilterApplier();

	@Test
	void packageNameFilterInclude_nonMatchingPackagesAreExcluded() {

		EngineDiscoveryRequest request = request().filters(
			PackageNameFilter.includePackageNames("org.junit.jupiter.engine.unknown")).build();

		// @formatter:off
		TestDescriptor engineDescriptor = engineDescriptor()
				.with(
						classTestDescriptor("matching", MatchingClass.class)
				)
				.build();
		// @formatter:on

		applier.applyAllFilters(request, engineDescriptor);

		List<UniqueId> includedDescriptors = engineDescriptor.getDescendants().stream().map(
			TestDescriptor::getUniqueId).collect(Collectors.toList());
		Assertions.assertEquals(0, includedDescriptors.size());
	}

	@Test
	void packageNameFilterInclude_matchingPackagesAreIncluded() {

		EngineDiscoveryRequest request = request().filters(
			PackageNameFilter.includePackageNames("org.junit.jupiter.engine")).build();

		// @formatter:off
		TestDescriptor engineDescriptor = engineDescriptor()
				.with(
						classTestDescriptor("matching", MatchingClass.class)
				)
				.build();
		// @formatter:on

		applier.applyAllFilters(request, engineDescriptor);

		List<UniqueId> includedDescriptors = engineDescriptor.getDescendants().stream().map(
			TestDescriptor::getUniqueId).collect(Collectors.toList());
		Assertions.assertEquals(1, includedDescriptors.size());
		Assertions.assertTrue(includedDescriptors.contains(UniqueId.root("class", "matching")));
	}

	@Test
	void packageNameFilterExclude_matchingPackagesAreExcluded() {

		EngineDiscoveryRequest request = request().filters(
			PackageNameFilter.excludePackageNames("org.junit.jupiter.engine")).build();

		// @formatter:off
		TestDescriptor engineDescriptor = engineDescriptor()
				.with(
						classTestDescriptor("matching", MatchingClass.class)
				)
				.build();
		// @formatter:on

		applier.applyAllFilters(request, engineDescriptor);

		List<UniqueId> includedDescriptors = engineDescriptor.getDescendants().stream().map(
			TestDescriptor::getUniqueId).collect(Collectors.toList());
		Assertions.assertEquals(0, includedDescriptors.size());
	}

	@Test
	void packageNameFilterExclude_nonMatchingPackagesAreIncluded() {

		EngineDiscoveryRequest request = request().filters(
			PackageNameFilter.excludePackageNames("org.junit.jupiter.engine.unknown")).build();

		// @formatter:off
		TestDescriptor engineDescriptor = engineDescriptor()
				.with(
						classTestDescriptor("matching", MatchingClass.class)
				)
				.build();
		// @formatter:on

		applier.applyAllFilters(request, engineDescriptor);

		List<UniqueId> includedDescriptors = engineDescriptor.getDescendants().stream().map(
			TestDescriptor::getUniqueId).collect(Collectors.toList());
		Assertions.assertEquals(1, includedDescriptors.size());
		Assertions.assertTrue(includedDescriptors.contains(UniqueId.root("class", "matching")));
	}

	@Test
	void nonMatchingClassesAreExcluded() {

		EngineDiscoveryRequest request = request().filters(
			ClassNameFilter.includeClassNamePatterns(".*\\$MatchingClass")).build();

		// @formatter:off
		TestDescriptor engineDescriptor = engineDescriptor()
			.with(
				classTestDescriptor("matching", MatchingClass.class),
				classTestDescriptor("other", OtherClass.class)
			)
			.build();
		// @formatter:on

		applier.applyAllFilters(request, engineDescriptor);

		List<UniqueId> includedDescriptors = engineDescriptor.getDescendants().stream().map(
			TestDescriptor::getUniqueId).collect(Collectors.toList());
		Assertions.assertEquals(1, includedDescriptors.size());
		Assertions.assertTrue(includedDescriptors.contains(UniqueId.root("class", "matching")));
	}

	@Test
	void nestedTestClassesAreAlwaysIncludedWhenTheirParentIs() {
		EngineDiscoveryRequest request = request().filters(
			ClassNameFilter.includeClassNamePatterns(".*\\$MatchingClass")).build();

		// @formatter:off
		TestDescriptor engineDescriptor = engineDescriptor()
			.with(
				classTestDescriptor("matching", MatchingClass.class)
					.with(nestedClassTestDescriptor("nested", MatchingClass.NestedClass.class))
			)
			.build();
		// @formatter:on

		applier.applyAllFilters(request, engineDescriptor);

		List<UniqueId> includedDescriptors = engineDescriptor.getDescendants().stream().map(
			TestDescriptor::getUniqueId).collect(Collectors.toList());
		Assertions.assertEquals(2, includedDescriptors.size());
		Assertions.assertTrue(includedDescriptors.contains(UniqueId.root("class", "matching")));
		Assertions.assertTrue(includedDescriptors.contains(UniqueId.root("nested-class", "nested")));
	}

	private static class MatchingClass {
		@Nested
		class NestedClass {
		}
	}

	private static class OtherClass {
	}
}
