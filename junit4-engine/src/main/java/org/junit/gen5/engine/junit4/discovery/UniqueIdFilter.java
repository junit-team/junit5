/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.discovery;

import static java.util.stream.Collectors.toSet;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit4.descriptor.JUnit4TestDescriptor;
import org.junit.gen5.engine.junit4.descriptor.RunnerTestDescriptor;
import org.junit.runner.Description;

class UniqueIdFilter extends RunnerTestDescriptorAwareFilter {

	private final String uniqueId;

	private Deque<Description> path;
	private Set<Description> descendants;

	public UniqueIdFilter(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	@Override
	void initialize(RunnerTestDescriptor runnerTestDescriptor) {
		Optional<? extends TestDescriptor> identifiedTestDescriptor = runnerTestDescriptor.findByUniqueId(uniqueId);
		if (identifiedTestDescriptor.isPresent()) {
			descendants = identifiedTestDescriptor.get().allDescendants().stream().map(
				JUnit4TestDescriptor.class::cast).map(JUnit4TestDescriptor::getDescription).collect(toSet());
		}
		path = new LinkedList<>();
		while (identifiedTestDescriptor.isPresent() && !identifiedTestDescriptor.get().equals(runnerTestDescriptor)) {
			path.addFirst(((JUnit4TestDescriptor) identifiedTestDescriptor.get()).getDescription());
			identifiedTestDescriptor = identifiedTestDescriptor.get().getParent();
		}
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
