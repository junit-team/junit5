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

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.gen5.api.Nested;
import org.junit.gen5.commons.util.AnnotationUtils;
import org.junit.gen5.commons.util.ObjectUtils;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;

public class ClassResolver extends JUnit5TestResolver {
	private static final Logger LOG = Logger.getLogger(ClassResolver.class.getName());

	private Pattern uniqueIdRegExPattern = Pattern.compile("^(.+?):([^#]+)$");

	@Override
	public TestResolverResult resolveFor(TestDescriptor parent, TestPlanSpecification testPlanSpecification) {
		ObjectUtils.verifyNonNull(parent, "Parent must not be null!");
		ObjectUtils.verifyNonNull(testPlanSpecification, "TestPlanSpecification must not be null!");

		if (parent.isRoot()) {
			List<TestDescriptor> resolvedTests = new LinkedList<>();
			resolvedTests.addAll(resolveAllPackagesFromSpecification(parent, testPlanSpecification));
			resolvedTests.addAll(resolveAllClassesFromSpecification(parent, testPlanSpecification));
			resolvedTests.addAll(resolveUniqueIdsFromSpecification(parent, testPlanSpecification));
			return TestResolverResult.proceedResolving(resolvedTests);
		}
		else if (parent instanceof ClassTestDescriptor) {
			Class<?> parentClass = ((ClassTestDescriptor) parent).getTestClass();
			List<Class<?>> nestedClasses = ReflectionUtils.findNestedClasses(parentClass,
				nestedClass -> AnnotationUtils.isAnnotated(nestedClass, Nested.class));
			List<TestDescriptor> resolvedTests = getTestDescriptorsForTestClasses(parent, nestedClasses);
			return TestResolverResult.proceedResolving(resolvedTests);
		}
		else {
			return TestResolverResult.empty();
		}
	}

	private List<TestDescriptor> resolveAllPackagesFromSpecification(TestDescriptor parent,
			TestPlanSpecification testPlanSpecification) {
		List<TestDescriptor> result = new LinkedList<>();

		for (String packageName : testPlanSpecification.getPackages()) {
			List<Class<?>> testClasses = ReflectionUtils.findAllClassesInPackage(packageName, aClass -> true);
			result.addAll(getTestDescriptorsForTestClasses(parent, testClasses));
		}

		return result;
	}

	private List<TestDescriptor> resolveAllClassesFromSpecification(TestDescriptor parent,
			TestPlanSpecification testPlanSpecification) {

		List<Class<?>> testClasses = testPlanSpecification.getClasses();
		return getTestDescriptorsForTestClasses(parent, testClasses);

	}

	private List<TestDescriptor> resolveUniqueIdsFromSpecification(TestDescriptor parent,
			TestPlanSpecification testPlanSpecification) {
		List<String> uniqueIds = testPlanSpecification.getUniqueIds();
		List<Class<?>> foundClasses = new LinkedList<>();

		for (String uniqueId : uniqueIds) {
			Matcher matcher = uniqueIdRegExPattern.matcher(uniqueId);
			if (matcher.matches()) {
				try {
					String className = matcher.group(2);
					foundClasses.add(Class.forName(className));
				}
				catch (ClassNotFoundException e) {
					LOG.fine(() -> "Skipping uniqueId " + uniqueId
							+ ": UniqueId does not seem to represent a valid test class.");
				}
			}
		}

		return getTestDescriptorsForTestClasses(parent, foundClasses);
	}

	private List<TestDescriptor> getTestDescriptorsForTestClasses(TestDescriptor parent, List<Class<?>> testClasses) {
		List<TestDescriptor> result = new LinkedList<>();
		for (Class<?> testClass : testClasses) {
			result.add(getTestDescriptorForTestClass(parent, testClass));
		}
		return result;
	}

	private TestDescriptor getTestDescriptorForTestClass(TestDescriptor parentTestDescriptor, Class<?> testClass) {
		ClassTestDescriptor testDescriptor;
		if (testClass.isMemberClass()) {
			testDescriptor = new ClassTestDescriptor(getTestEngine(), testClass);
		}
		else {
			testDescriptor = new ClassTestDescriptor(getTestEngine(), testClass);
		}
		parentTestDescriptor.addChild(testDescriptor);
		return testDescriptor;
	}
}
