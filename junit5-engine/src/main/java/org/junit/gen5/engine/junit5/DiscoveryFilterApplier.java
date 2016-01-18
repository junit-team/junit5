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
 * Class for applying all {@link org.junit.gen5.engine.DiscoveryFilter}s to all children of a TestDescriptor
 */
class DiscoveryFilterApplier {

	/**
	 * Apply filters. Currently only {@link ClassFilter} is considered.
	 */
	void apply(EngineDiscoveryRequest discoveryRequest, TestDescriptor engineDescriptor) {

		List<ClassFilter> classFilters = discoveryRequest.getDiscoveryFiltersByType(ClassFilter.class);
		if (classFilters.isEmpty()) {
			return;
		}

		TestDescriptor.Visitor filteringVisitor = (descriptor, remove) -> {
			if (descriptor instanceof ClassTestDescriptor) {
				if (descriptor instanceof NestedClassTestDescriptor)
					return;

				Class<?> testClass = ((ClassTestDescriptor) descriptor).getTestClass();

				// @formatter:off
                if (classFilters.stream()
                        .map(filter -> filter.filter(testClass))
                        .anyMatch(FilterResult::excluded)) {
                    remove.run();
                }
                // @formatter:on
			}
		};
		engineDescriptor.accept(filteringVisitor);
	}

}
