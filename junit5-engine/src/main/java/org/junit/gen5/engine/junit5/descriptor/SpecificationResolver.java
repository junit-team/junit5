/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import java.util.Set;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.ClassNameSpecification;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.gen5.engine.UniqueIdSpecification;

public class SpecificationResolver {

	private final Set testDescriptors;
	private final TestDescriptor root;

	public SpecificationResolver(Set testDescriptors, TestDescriptor root) {
		this.testDescriptors = testDescriptors;
		this.root = root;
	}

	public void resolveElement(TestPlanSpecificationElement element) {
		if (element.getClass() == ClassNameSpecification.class) {
			resolveClassNameSpecification((ClassNameSpecification) element);
		}
		if (element.getClass() == UniqueIdSpecification.class) {
			resolveUniqueIdSpecification((UniqueIdSpecification) element);
		}
	}

	private void resolveUniqueIdSpecification(UniqueIdSpecification element) {
		UniqueIdParts uniqueIdParts = new UniqueIdParts(element.getUniqueId());
		Preconditions.condition(element.getUniqueId().equals(uniqueIdParts.rest()),
			"UniqueId is: " + uniqueIdParts.rest());
		String engineId = uniqueIdParts.pop();
		Preconditions.condition(root.getUniqueId().equals(engineId), "Engine ID is: " + engineId);
		TestDescriptor descriptor = new UniqueIdTestDescriptorResolver().resolve(root, element);
		if (descriptor != null) {
			testDescriptors.add(descriptor);
			testDescriptors.addAll(new UniqueIdTestDescriptorResolver().resolveChildren(descriptor, element));
		}
	}

	private void resolveClassNameSpecification(ClassNameSpecification element) {
		ClassTestDescriptor descriptor = new ClassNameTestDescriptorResolver().resolve(root, element);
		if (descriptor != null) {
			testDescriptors.add(descriptor);
			testDescriptors.addAll(new ClassNameTestDescriptorResolver().resolveChildren(descriptor, element));
		}
	}

}
