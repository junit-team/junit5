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

import static java.util.stream.Collectors.toList;
import static org.junit.gen5.commons.util.AnnotationUtils.findMethods;
import static org.junit.gen5.commons.util.ReflectionUtils.loadClass;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.junit.gen5.commons.util.AnnotationUtils;
import org.junit.gen5.engine.ClassNameSpecification;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.gen5.engine.UniqueIdSpecification;

public class SpecificationResolver {

	private final Set<TestDescriptor> testDescriptors;
	private final TestDescriptor root;

	private final TestClassTester classTester = new TestClassTester();
	private final TestMethodTester methodTester = new TestMethodTester();

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
	}

	private void resolveUniqueIdSpecification(UniqueIdSpecification uniqueIdSpecification) {
		UniqueIdParts uniqueIdParts = new UniqueIdParts(uniqueIdSpecification.getUniqueId());

	}

}
