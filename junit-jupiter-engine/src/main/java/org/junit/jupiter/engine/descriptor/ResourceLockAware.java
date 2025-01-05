/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.junit.jupiter.api.parallel.ResourceLockTarget.CHILDREN;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.parallel.ResourceLocksProvider;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource;

/**
 * @since 5.12
 */
interface ResourceLockAware extends TestDescriptor {

	default Stream<ExclusiveResource> determineExclusiveResources() {

		Deque<ResourceLockAware> ancestors = new ArrayDeque<>();
		TestDescriptor parent = this.getParent().orElse(null);
		while (parent instanceof ResourceLockAware) {
			ancestors.addFirst((ResourceLockAware) parent);
			parent = parent.getParent().orElse(null);
		}

		if (ancestors.isEmpty()) {
			return determineOwnExclusiveResources();
		}

		Stream<ExclusiveResource> parentStaticResourcesForChildren = ancestors.getLast() //
				.getExclusiveResourceCollector().getStaticResourcesFor(CHILDREN);

		Stream<ExclusiveResource> ancestorDynamicResources = ancestors.stream() //
				.map(ResourceLockAware::getExclusiveResourceCollector) //
				.flatMap(collector -> collector.getDynamicResources(this::evaluateResourceLocksProvider));

		return Stream.of(ancestorDynamicResources, parentStaticResourcesForChildren, determineOwnExclusiveResources())//
				.flatMap(s -> s);
	}

	default Stream<ExclusiveResource> determineOwnExclusiveResources() {
		return this.getExclusiveResourceCollector().getAllExclusiveResources(this::evaluateResourceLocksProvider);
	}

	ExclusiveResourceCollector getExclusiveResourceCollector();

	Set<ResourceLocksProvider.Lock> evaluateResourceLocksProvider(ResourceLocksProvider provider);

}
