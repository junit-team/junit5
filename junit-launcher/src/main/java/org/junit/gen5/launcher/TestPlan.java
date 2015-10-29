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

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;

/**
 * @since 5.0
 */
final class TestPlan {

	/**
	 * List of all TestDescriptors, including children.
	 */
	private final Collection<TestDescriptor> testDescriptors = new LinkedList<>();


	TestPlan() {
		/* no-op */
	}

	public void addTest(TestDescriptor testDescriptor) {
		addTests(singleton(testDescriptor));
	}

	public void addTests(TestDescriptor... testDescriptors) {
		addTests(asList(testDescriptors));
	}

	public void addTests(Collection<TestDescriptor> testDescriptors) {
		this.testDescriptors.addAll(testDescriptors);
	}

	public Collection<TestDescriptor> getTests() {
		return Collections.unmodifiableCollection(testDescriptors);
	}

	public List<TestDescriptor> getAllTestsForTestEngine(TestEngine testEngine) {
		return this.testDescriptors.stream().filter(testEngine::supports).collect(Collectors.toList());
	}

}
