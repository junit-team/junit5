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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestTag;

/**
 * @since 5.0
 */
public final class TestPlan implements TestDescriptor {

	/**
	 * List of all TestDescriptors, including children.
	 */
	private final Set<EngineDescriptor> engineDescriptors = new HashSet<>();

	TestPlan() {
		/* no-op */
	}

	public void addEngineDescriptor(EngineDescriptor engineDescriptor) {
		engineDescriptor.setParent(this);
		engineDescriptors.add(engineDescriptor);
	}

	public Collection<TestDescriptor> getEngineDescriptors() {
		return Collections.unmodifiableCollection(engineDescriptors);
	}

	public Optional<EngineDescriptor> getEngineDescriptorFor(TestEngine testEngine) {
		return this.engineDescriptors.stream().filter(
			descriptor -> descriptor.getEngine().equals(testEngine)).findFirst();
	}

	public long getNumberOfStaticTests() {
		return this.engineDescriptors.stream().filter(TestDescriptor::isTest).count();
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
	public TestDescriptor getParent() {
		return null;
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
		throw new UnsupportedOperationException("Only use addEngineDescriptor to add children");
	}

	@Override
	public void removeChild(TestDescriptor descriptor) {
		engineDescriptors.remove(descriptor);
	}

	@Override
	public Set<TestDescriptor> getChildren() {
		return Collections.unmodifiableSet(engineDescriptors);
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this, () -> {
			throw new UnsupportedOperationException("It's not possible to remove the whole test plan");
		});
		new HashSet<>(engineDescriptors).forEach(child -> child.accept(visitor));
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

	private boolean hasTests(TestDescriptor descriptor) {
		if (descriptor.isTest())
			return true;
		return descriptor.getChildren().stream().anyMatch(anyDescriptor -> hasTests(anyDescriptor));
	}

}
