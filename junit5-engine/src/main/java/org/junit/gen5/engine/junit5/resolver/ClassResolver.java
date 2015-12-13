/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.resolver;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.gen5.api.Nested;
import org.junit.gen5.commons.util.AnnotationUtils;
import org.junit.gen5.commons.util.ObjectUtils;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;

public class ClassResolver implements TestResolver {
	private TestEngine testEngine;

	@Override
	public void setTestEngine(TestEngine testEngine) {
		this.testEngine = testEngine;
	}

	@Override
	public TestResolverResult resolveFor(TestDescriptor parent, TestPlanSpecification testPlanSpecification) {
		ObjectUtils.verifyNonNull(parent, "Parent must not be null!");
		ObjectUtils.verifyNonNull(testPlanSpecification, "TestPlanSpecification must not be null!");

		if (parent.isRoot()) {
			List<TestDescriptor> resolvedTests = new LinkedList<>();
			resolvedTests.addAll(resolveAllPackagesFromSpecification(parent, testPlanSpecification));
			resolvedTests.addAll(resolveAllClassesFromSpecification(parent, testPlanSpecification));
			return TestResolverResult.proceedResolving(resolvedTests);
		}
		else if (parent instanceof ClassTestDescriptor) {
			Class<?> parentClass = ((ClassTestDescriptor) parent).getTestClass();
			List<Class<?>> nestedClasses = ReflectionUtils.findNestedClasses(parentClass, nestedClass -> AnnotationUtils.isAnnotated(nestedClass, Nested.class));
			List<TestDescriptor> resolvedTests = getTestDescriptorForTestClasses(parent, nestedClasses);
			return TestResolverResult.proceedResolving(resolvedTests);
		}
		else {
			return TestResolverResult.empty();
		}
	}

	private List<TestDescriptor> resolveAllPackagesFromSpecification(TestDescriptor parent, TestPlanSpecification testPlanSpecification) {
		List<TestDescriptor> result = new LinkedList<>();

		for (String packageName : testPlanSpecification.getPackages()) {
			List<Class<?>> testClasses = ReflectionUtils.findAllClassesInPackage(packageName, aClass -> true);
			result.addAll(getTestDescriptorForTestClasses(parent, testClasses));
		}

		return result;
	}

	private List<TestDescriptor> resolveAllClassesFromSpecification(TestDescriptor parent,
			TestPlanSpecification testPlanSpecification) {

		List<Class<?>> testClasses = testPlanSpecification.getClasses();
		return getTestDescriptorForTestClasses(parent, testClasses);

	}

	private List<TestDescriptor> getTestDescriptorForTestClasses(TestDescriptor parent, List<Class<?>> testClasses) {
		List<TestDescriptor> result = new LinkedList<>();
		for (Class<?> testClass : testClasses) {
			result.add(getTestGroupForClass(parent, testClass));
		}
		return result;
	}

	private TestDescriptor getTestGroupForClass(TestDescriptor parentTestDescriptor, Class<?> testClass) {
		ClassTestDescriptor testDescriptor = new ClassTestDescriptor(testEngine, testClass);
		parentTestDescriptor.addChild(testDescriptor);
		return testDescriptor;
	}
}
