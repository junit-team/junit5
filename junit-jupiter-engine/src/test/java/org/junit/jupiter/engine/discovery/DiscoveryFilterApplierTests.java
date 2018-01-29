/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.engine.descriptor.TestDescriptorBuilder.classTestDescriptor;
import static org.junit.jupiter.engine.descriptor.TestDescriptorBuilder.engineDescriptor;
import static org.junit.jupiter.engine.descriptor.TestDescriptorBuilder.nestedClassTestDescriptor;
import static org.junit.platform.engine.Filter.composeFilters;
import static org.junit.platform.engine.discovery.ClassNameFilter.includeClassNamePatterns;
import static org.junit.platform.engine.discovery.PackageNameFilter.excludePackageNames;
import static org.junit.platform.engine.discovery.PackageNameFilter.includePackageNames;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.Filter;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;

/**
 * Microtests for {@link DiscoveryFilterApplier}.
 *
 * @since 5.0
 */
class DiscoveryFilterApplierTests {

	@Test
	void packageNameFilterInclude_nonMatchingPackagesAreExcluded() {
		// @formatter:off
		TestDescriptor engineDescriptor = engineDescriptor()
				.with(
						classTestDescriptor("matching", MatchingClass.class)
				)
				.build();
		// @formatter:on

		applyClassNamePredicate(engineDescriptor, includePackageNames("org.junit.jupiter.engine.unknown"));

		assertThat(engineDescriptor.getDescendants()).isEmpty();
	}

	@Test
	void packageNameFilterInclude_matchingPackagesAreIncluded() {

		// @formatter:off
		TestDescriptor engineDescriptor = engineDescriptor()
				.with(
						classTestDescriptor("matching", MatchingClass.class)
				)
				.build();

		applyClassNamePredicate(engineDescriptor, includePackageNames("org.junit.jupiter.engine"));

		assertThat(engineDescriptor.getDescendants())
				.extracting(TestDescriptor::getUniqueId)
				.containsExactly(UniqueId.root("class", "matching"));
		// @formatter:on
	}

	@Test
	void packageNameFilterExclude_matchingPackagesAreExcluded() {

		// @formatter:off
		TestDescriptor engineDescriptor = engineDescriptor()
				.with(
						classTestDescriptor("matching", MatchingClass.class)
				)
				.build();
		// @formatter:on

		applyClassNamePredicate(engineDescriptor, excludePackageNames("org.junit.jupiter.engine"));

		assertThat(engineDescriptor.getDescendants()).isEmpty();
	}

	@Test
	void packageNameFilterExclude_nonMatchingPackagesAreIncluded() {

		// @formatter:off
		TestDescriptor engineDescriptor = engineDescriptor()
				.with(
						classTestDescriptor("matching", MatchingClass.class)
				)
				.build();

		applyClassNamePredicate(engineDescriptor, excludePackageNames("org.junit.jupiter.engine.unknown"));

		assertThat(engineDescriptor.getDescendants())
				.extracting(TestDescriptor::getUniqueId)
				.containsExactly(UniqueId.root("class", "matching"));
		// @formatter:on
	}

	@Test
	void nonMatchingClassesAreExcluded() {

		// @formatter:off
		TestDescriptor engineDescriptor = engineDescriptor()
				.with(
						classTestDescriptor("matching", MatchingClass.class),
						classTestDescriptor("other", OtherClass.class)
				)
				.build();

		applyClassNamePredicate(engineDescriptor, includeClassNamePatterns(".*\\$MatchingClass"));

		assertThat(engineDescriptor.getDescendants())
				.extracting(TestDescriptor::getUniqueId)
				.containsExactly(UniqueId.root("class", "matching"));
		// @formatter:on
	}

	@Test
	void nestedTestClassesAreAlwaysIncludedWhenTheirParentIs() {

		// @formatter:off
		TestDescriptor engineDescriptor = engineDescriptor()
				.with(
						classTestDescriptor("matching", MatchingClass.class)
								.with(nestedClassTestDescriptor("nested", MatchingClass.NestedClass.class))
				)
				.build();

		applyClassNamePredicate(engineDescriptor, includeClassNamePatterns(".*\\$MatchingClass"));

		assertThat(engineDescriptor.getDescendants())
				.extracting(TestDescriptor::getUniqueId)
				.containsExactlyInAnyOrder(
						UniqueId.root("class", "matching"),
						UniqueId.root("nested-class", "nested")
				);
		// @formatter:on
	}

	private void applyClassNamePredicate(TestDescriptor engineDescriptor, Filter<String> filter) {
		DiscoveryFilterApplier applier = new DiscoveryFilterApplier();
		applier.applyClassNamePredicate(composeFilters(filter).toPredicate(), engineDescriptor);
	}

	private static class MatchingClass {
		@Nested
		class NestedClass {
		}
	}

	private static class OtherClass {
	}
}
