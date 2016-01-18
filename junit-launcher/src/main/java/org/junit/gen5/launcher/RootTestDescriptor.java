/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.*;
import org.junit.gen5.engine.support.discovery.AbstractTestDescriptor;

/**
 * @since 5.0
 */
final class RootTestDescriptor extends AbstractTestDescriptor {
	private final List<TestEngine> testEngines = new LinkedList<>();

	RootTestDescriptor() {
		super("testPlan");
	}

	Iterable<TestEngine> getTestEngines() {
		return testEngines;
	}

	@Override
	public void addChild(TestDescriptor child) {
		Preconditions.condition(child instanceof EngineAwareTestDescriptor,
			"TestDescriptors that are added to the root need to be aware of their engines");
		super.addChild(child);
		testEngines.add(((EngineAwareTestDescriptor) child).getEngine());
	}

	@Override
	public void removeChild(TestDescriptor child) {
		Preconditions.condition(child instanceof EngineAwareTestDescriptor,
			"TestDescriptors that are added to the root need to be aware of their engines");
		testEngines.remove(((EngineAwareTestDescriptor) child).getEngine());
		super.removeChild(child);
	}

	TestDescriptor getTestDescriptorFor(TestEngine testEngine) {
		// @formatter:off
		return getChildren().stream()
				.map(EngineAwareTestDescriptor.class::cast)
				.filter(testDescriptor -> Objects.equals(testEngine, testDescriptor.getEngine()))
				.findAny()
				.orElseThrow(() -> new IllegalArgumentException("No TestDescriptor for TestEngine with ID: " + testEngine.getId()));
		// @formatter:on
	}

	@Override
	public String getName() {
		return getUniqueId();
	}

	@Override
	public String getDisplayName() {
		return "Test Plan";
	}

	@Override
	public boolean isTest() {
		return false;
	}

	@Override
	public boolean isContainer() {
		return true;
	}

	void applyFilters(DiscoveryRequest discoveryRequest) {
		Visitor filteringVisitor = (descriptor, remove) -> {
			if (!descriptor.isTest())
				return;
			if (!discoveryRequest.acceptDescriptor(descriptor))
				remove.run();
		};
		accept(filteringVisitor);
	}

	void prune() {
		Visitor pruningVisitor = (descriptor, remove) -> {
			if (descriptor.isRoot() || descriptor.hasTests())
				return;
			remove.run();
		};
		accept(pruningVisitor);
	}

	@Override
	public String toString() {
		return getChildren().toString();
	}
}
