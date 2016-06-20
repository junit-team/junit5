/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.vintage.engine.discovery;

import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;

import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.runner.Description;
import org.junit.vintage.engine.descriptor.RunnerTestDescriptor;
import org.junit.vintage.engine.descriptor.VintageTestDescriptor;

/**
 * @since 4.12
 */
class UniqueIdFilter extends RunnerTestDescriptorAwareFilter {

	private final UniqueId uniqueId;

	private Deque<Description> path;
	private Set<Description> descendants;

	public UniqueIdFilter(UniqueId uniqueId) {
		this.uniqueId = uniqueId;
	}

	@Override
	void initialize(RunnerTestDescriptor runnerTestDescriptor) {
		Optional<? extends TestDescriptor> identifiedTestDescriptor = runnerTestDescriptor.findByUniqueId(uniqueId);
		descendants = determineDescendants(identifiedTestDescriptor);
		path = determinePath(runnerTestDescriptor, identifiedTestDescriptor);
	}

	private Deque<Description> determinePath(RunnerTestDescriptor runnerTestDescriptor,
			Optional<? extends TestDescriptor> identifiedTestDescriptor) {
		Deque<Description> path = new LinkedList<>();
		Optional<? extends TestDescriptor> current = identifiedTestDescriptor;
		while (current.isPresent() && !current.get().equals(runnerTestDescriptor)) {
			path.addFirst(((VintageTestDescriptor) current.get()).getDescription());
			current = current.get().getParent();
		}
		return path;
	}

	private Set<Description> determineDescendants(Optional<? extends TestDescriptor> identifiedTestDescriptor) {
		if (identifiedTestDescriptor.isPresent()) {
			// @formatter:off
			return identifiedTestDescriptor.get()
					.getAllDescendants()
					.stream()
					.map(VintageTestDescriptor.class::cast)
					.map(VintageTestDescriptor::getDescription)
					.collect(toSet());
			// @formatter:on
		}
		return emptySet();
	}

	@Override
	public boolean shouldRun(Description description) {
		return path.contains(description) || descendants.contains(description);
	}

	@Override
	public String describe() {
		return "Unique ID " + uniqueId;
	}

}
