/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.junit4runner;

import java.util.HashMap;
import java.util.Map;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.launcher.TestPlan;
import org.junit.runner.Description;

class JUnit5TestTree {

	private final Description suiteDescription;
	private final Map<TestDescriptor, Description> descriptions = new HashMap<>();

	JUnit5TestTree(TestPlan plan, Class<?> testClass) {
		suiteDescription = generateDescription(plan, testClass);
	}

	Description getSuiteDescription() {
		return suiteDescription;
	}

	Description addDescriptionFor(TestDescriptor descriptor) {
		return addDescriptionFor(descriptor, suiteDescription);
	}

	Description getDescription(TestDescriptor testDescriptor) {
		return descriptions.get(testDescriptor);
	}

	private Description generateDescription(TestPlan plan, Class<?> testClass) {
		Description suiteDescription = Description.createSuiteDescription(testClass.getName());
		buildDescriptionTree(suiteDescription, plan);
		return suiteDescription;
	}

	private void buildDescriptionTree(Description suiteDescription, TestPlan plan) {
		for (TestDescriptor descriptor : plan.getEngineDescriptors()) {
			addDescriptionFor(descriptor, suiteDescription);
		}
	}

	private Description addDescriptionFor(TestDescriptor descriptor, Description root) {
		if (descriptions.containsKey(descriptor))
			return descriptions.get(descriptor);
		Description newDescription = createJUnit4Description(descriptor);
		descriptions.put(descriptor, newDescription);
		if (descriptor.getParent() == null) {
			root.addChild(newDescription);
		}
		else {
			Description parent = addDescriptionFor(descriptor.getParent(), root);
			parent.addChild(newDescription);
		}
		return newDescription;
	}

	private Description createJUnit4Description(TestDescriptor testDescriptor) {
		if (testDescriptor.isTest()) {
			return Description.createTestDescription(testDescriptor.getParent().getDisplayName(),
				testDescriptor.getDisplayName(), testDescriptor.getUniqueId());
		}
		else {
			return Description.createSuiteDescription(testDescriptor.getDisplayName(), testDescriptor.getUniqueId());
		}
	}
}
