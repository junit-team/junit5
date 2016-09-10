/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine;

import java.util.List;

import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.NestedClassTestDescriptor;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.discovery.ClassNameFilter;

/**
 * Class for applying all {@link org.junit.platform.engine.DiscoveryFilter}s to all
 * children of a {@link TestDescriptor}.
 *
 * @since 5.0
 */
class DiscoveryFilterApplier {

	/**
	 * Apply all filters. Currently only {@link ClassNameFilter} is considered.
	 */
	void applyAllFilters(EngineDiscoveryRequest discoveryRequest, TestDescriptor engineDescriptor) {
		applyClassNameFilters(discoveryRequest.getDiscoveryFiltersByType(ClassNameFilter.class), engineDescriptor);
	}

	private void applyClassNameFilters(List<ClassNameFilter> classNameFilters, TestDescriptor engineDescriptor) {
		if (classNameFilters.isEmpty()) {
			return;
		}
		TestDescriptor.Visitor filteringVisitor = descriptor -> {
			if (descriptor instanceof ClassTestDescriptor) {
				if (!includeClass((ClassTestDescriptor) descriptor, classNameFilters))
					descriptor.removeFromHierarchy();
			}
		};
		engineDescriptor.accept(filteringVisitor);
	}

	private boolean includeClass(ClassTestDescriptor classTestDescriptor, List<ClassNameFilter> classNameFilters) {

		// Nested Tests are never filtered out
		if (classTestDescriptor instanceof NestedClassTestDescriptor)
			return true;

		Class<?> testClass = classTestDescriptor.getTestClass();

		// @formatter:off
        return (classNameFilters.stream()
                .map(filter -> filter.apply(testClass.getName()))
                .noneMatch(FilterResult::excluded));
        // @formatter:on
	}

}
