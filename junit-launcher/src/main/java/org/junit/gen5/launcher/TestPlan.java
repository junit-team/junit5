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
import java.util.LinkedList;
import java.util.Optional;

import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;

/**
 * @since 5.0
 */
public final class TestPlan {

	/**
	 * List of all TestDescriptors, including children.
	 */
	private final Collection<EngineDescriptor> engineDescriptors = new LinkedList<>();

	TestPlan() {
		/* no-op */
	}

	public void addEngineDescriptor(EngineDescriptor engineDescriptor) {
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

}
