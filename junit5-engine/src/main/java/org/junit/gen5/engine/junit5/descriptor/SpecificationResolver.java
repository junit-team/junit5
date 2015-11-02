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

import static org.junit.gen5.commons.util.ReflectionUtils.loadClass;

import java.util.HashSet;
import java.util.Set;

import org.junit.gen5.engine.ClassNameSpecification;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.gen5.engine.UniqueIdSpecification;

public class SpecificationResolver {

	private final Set<TestDescriptor> testDescriptors;
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

	private void resolveClassNameSpecification(ClassNameSpecification element) {
		ClassTestDescriptor descriptor = new ClassNameTestDescriptorResolver().resolve(root, element);
		if (descriptor != null) {
			testDescriptors.add(descriptor);
			testDescriptors.addAll(new ClassNameTestDescriptorResolver().resolveChildren(descriptor, element));
		}
	}

	private void resolveUniqueIdSpecification(UniqueIdSpecification uniqueIdSpecification) {
		UniqueIdParts uniqueIdParts = new UniqueIdParts(uniqueIdSpecification.getUniqueId());

		String engineId = uniqueIdParts.pop();
		if (!root.getUniqueId().equals(engineId)) {
			throwCannotResolveException(uniqueIdSpecification, engineId);
		}

		resolveUniqueId(uniqueIdSpecification, root, uniqueIdParts, new HashSet<>());

		//		TestDescriptor descriptor = new UniqueIdTestDescriptorResolver().resolve(root, element);
		//		if (descriptor != null) {
		//			testDescriptors.add(descriptor);
		//			testDescriptors.addAll(new UniqueIdTestDescriptorResolver().resolveChildren(descriptor, element));
		//		}
	}

	private void resolveUniqueId(UniqueIdSpecification uniqueIdSpecification, TestDescriptor parent,
			UniqueIdParts uniqueIdRest, HashSet<TestDescriptor> resolvedDescriptors) {
		String part = uniqueIdRest.pop();
		if (part.isEmpty()) {
			testDescriptors.addAll(resolvedDescriptors);
			return;
		}
		if (part.startsWith(":")) {
			TestDescriptor classDescriptor = getClassTestDescriptor(parent, part);
			resolvedDescriptors.add(classDescriptor);
			resolveUniqueId(uniqueIdSpecification, classDescriptor, uniqueIdRest, resolvedDescriptors);
			return;
		}
		throwCannotResolveException(uniqueIdSpecification, part);
	}

	private TestDescriptor getClassTestDescriptor(TestDescriptor parent, String uniqueIdPart) {
		String uniqueId = parent.getUniqueId() + uniqueIdPart;
		TestDescriptor descriptor = descriptorByUniqueId(uniqueId);
		if (descriptor == null) {
			String className = uniqueIdPart.substring(1);
			Class<?> clazz = loadClass(className);
			descriptor = new ClassTestDescriptor(clazz, parent);
		}
		return descriptor;
	}

	private TestDescriptor descriptorByUniqueId(String uniqueId) {
		for (TestDescriptor descriptor : testDescriptors) {
			if (descriptor.getUniqueId().equals(uniqueId)) {
				return descriptor;
			}
		}
		return null;
	}

	private void throwCannotResolveException(UniqueIdSpecification specification, String part) {
		throw new IllegalArgumentException(
			String.format("Cannot resolve part '%s' of unique id '%s'", part, specification.getUniqueId()));
	}

}
