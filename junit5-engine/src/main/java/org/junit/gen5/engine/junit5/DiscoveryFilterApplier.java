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

import java.util.List;

import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.FilterResult;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.discovery.ClassFilter;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.NestedClassTestDescriptor;

/**
 * Class for applying all {@link org.junit.gen5.engine.DiscoveryFilter}s to all
 * children of a {@link TestDescriptor}.
 *
 * @since 5.0
 */
class DiscoveryFilterApplier {

	/**
	 * Apply all filters. Currently only {@link ClassFilter} is considered.
	 */
	void applyAllFilters(EngineDiscoveryRequest discoveryRequest, TestDescriptor engineDescriptor) {
		applyClassFilters(discoveryRequest.getDiscoveryFiltersByType(ClassFilter.class), engineDescriptor);
	}

	private void applyClassFilters(List<ClassFilter> classFilters, TestDescriptor engineDescriptor) {
		if (classFilters.isEmpty()) {
			return;
		}
		TestDescriptor.Visitor filteringVisitor = descriptor -> {
			if (descriptor instanceof ClassTestDescriptor) {
				if (!includeClass((ClassTestDescriptor) descriptor, classFilters))
					descriptor.removeFromHierarchy();
			}
		};
		engineDescriptor.accept(filteringVisitor);
	}

	private boolean includeClass(ClassTestDescriptor classTestDescriptor, List<ClassFilter> classFilters) {

		// Nested Tests are never filtered out
		if (classTestDescriptor instanceof NestedClassTestDescriptor)
			return true;

		Class<?> testClass = classTestDescriptor.getTestClass();

		// @formatter:off
        return (classFilters.stream()
                .map(filter -> filter.apply(testClass))
                .noneMatch(FilterResult::excluded));
        // @formatter:on
	}

}
