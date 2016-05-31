/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import static org.junit.gen5.engine.junit5.descriptor.TestDescriptorBuilder.classTestDescriptor;
import static org.junit.gen5.engine.junit5.descriptor.TestDescriptorBuilder.engineDescriptor;
import static org.junit.gen5.engine.junit5.descriptor.TestDescriptorBuilder.nestedClassTestDescriptor;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.gen5.api.Assertions;
import org.junit.gen5.api.Nested;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.discovery.ClassFilter;

/**
 * Microtests for {@link DiscoveryFilterApplier}.
 *
 * @since 5.0
 */
class DiscoveryFilterApplierTests {

	DiscoveryFilterApplier applier = new DiscoveryFilterApplier();

	@Test
	void nonMatchingClassesAreExcluded() {

		EngineDiscoveryRequest request = request().filters(
			ClassFilter.byClassNamePattern(".*\\$MatchingClass")).build();

		// @formatter:off
		TestDescriptor engineDescriptor = engineDescriptor()
			.with(
				classTestDescriptor("matching", MatchingClass.class),
				classTestDescriptor("other", OtherClass.class)
			)
			.build();
		// @formatter:on

		applier.applyAllFilters(request, engineDescriptor);

		List<UniqueId> includedDescriptors = engineDescriptor.allDescendants().stream().map(
			TestDescriptor::getUniqueId).collect(Collectors.toList());
		Assertions.assertEquals(1, includedDescriptors.size());
		Assertions.assertTrue(includedDescriptors.contains(UniqueId.root("class", "matching")));
	}

	@Test
	void nestedTestClassesAreAlwaysIncludedWhenTheirParentIs() {
		EngineDiscoveryRequest request = request().filters(
			ClassFilter.byClassNamePattern(".*\\$MatchingClass")).build();

		// @formatter:off
		TestDescriptor engineDescriptor = engineDescriptor()
			.with(
				classTestDescriptor("matching", MatchingClass.class)
					.with(nestedClassTestDescriptor("nested", MatchingClass.NestedClass.class))
			)
			.build();
		// @formatter:on

		applier.applyAllFilters(request, engineDescriptor);

		List<UniqueId> includedDescriptors = engineDescriptor.allDescendants().stream().map(
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
