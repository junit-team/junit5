/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

import java.util.*;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestSource;
import org.junit.gen5.engine.TestTag;

/**
 * @since 5.0
 */
public final class TestPlan implements TestDescriptor {
	private final HashMap<String, TestDescriptor> engineRootTestDescriptors = new HashMap<>();

	TestPlan() {
		/* no-op */
	}

	public void addTestDescriptorForEngine(TestEngine testEngine, TestDescriptor testDescriptor) {
		engineRootTestDescriptors.put(testEngine.getId(), testDescriptor);
	}

	public Collection<TestDescriptor> getEngineRootTestDescriptors() {
		return Collections.unmodifiableCollection(engineRootTestDescriptors.values());
	}

	public long countStaticTests() {
		return this.engineRootTestDescriptors.values().stream().mapToLong(
			engineDescriptor -> engineDescriptor.countStaticTests()).sum();
	}

	public Optional<TestDescriptor> getTestDescriptorFor(TestEngine testEngine) {
		return Optional.of(this.engineRootTestDescriptors.get(testEngine.getId()));
	}

	@Override
	public String getUniqueId() {
		return "testplan";
	}

	@Override
	public String getDisplayName() {
		return "testplan";
	}

	@Override
	public Optional<TestDescriptor> getParent() {
		return Optional.empty();
	}

	@Override
	public boolean isTest() {
		return false;
	}

	@Override
	public Set<TestTag> getTags() {
		return null;
	}

	@Override
	public void addChild(TestDescriptor descriptor) {
		throw new UnsupportedOperationException("Only use addTestDescriptor to add children");
	}

	@Override
	public void removeChild(TestDescriptor descriptor) {
		engineRootTestDescriptors.remove(descriptor);
	}

	@Override
	public Set<TestDescriptor> getChildren() {
		Set<TestDescriptor> children = new HashSet<>();
		engineRootTestDescriptors.values().forEach(descriptor -> children.add(descriptor));
		return children;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this, () -> {
			//the test plan itself will never be removed
		});
		new HashSet<>(engineRootTestDescriptors.values()).forEach(child -> child.accept(visitor));
	}

	@Override
	public Optional<TestSource> getSource() {
		return Optional.empty();
	}

	void applyFilters(TestPlanSpecification specification) {
		Visitor filteringVisitor = (descriptor, remove) -> {
			if (!descriptor.isTest())
				return;
			if (!specification.acceptDescriptor(descriptor))
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
}