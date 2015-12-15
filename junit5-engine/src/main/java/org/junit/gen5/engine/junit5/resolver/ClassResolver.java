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
import java.util.Optional;
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
	public void resolveFor(TestDescriptor parent, TestPlanSpecification testPlanSpecification) {
		ObjectUtils.verifyNonNull(parent, "Parent must not be null!");
		ObjectUtils.verifyNonNull(testPlanSpecification, "TestPlanSpecification must not be null!");

		if (parent.isRoot()) {
			List<TestDescriptor> packageBasedTestClasses = resolveAllPackagesFromSpecification(parent,
				testPlanSpecification);
			getTestResolverRegistry().notifyResolvers(packageBasedTestClasses, testPlanSpecification);

			List<TestDescriptor> classBasedTestClasses = resolveAllClassesFromSpecification(parent,
				testPlanSpecification);
			getTestResolverRegistry().notifyResolvers(classBasedTestClasses, testPlanSpecification);

			List<TestDescriptor> uniqueIdBasedTestClasses = resolveUniqueIdsFromSpecification(parent,
				testPlanSpecification);
			getTestResolverRegistry().notifyResolvers(uniqueIdBasedTestClasses, testPlanSpecification);
		}
		else if (parent instanceof ClassTestDescriptor) {
			Class<?> parentClass = ((ClassTestDescriptor) parent).getTestClass();
			List<Class<?>> nestedClasses = ReflectionUtils.findNestedClasses(parentClass,
				nestedClass -> AnnotationUtils.isAnnotated(nestedClass, Nested.class));
			getTestResolverRegistry().notifyResolvers(getTestDescriptorsForTestClasses(parent, nestedClasses),
				testPlanSpecification);
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

	ClassTestDescriptor getTestDescriptorForTestClass(TestDescriptor parentTestDescriptor, Class<?> testClass) {
		ClassTestDescriptor testDescriptor;
		if (ReflectionUtils.isNestedClass(testClass)) {
			parentTestDescriptor = getTestDescriptorForTestClass(parentTestDescriptor, testClass.getEnclosingClass());
			testDescriptor = new ClassTestDescriptor(getTestEngine(), testClass);
		}
		else {
			testDescriptor = new ClassTestDescriptor(getTestEngine(), testClass);
		}
		testDescriptor = mergeIntoTree(parentTestDescriptor, testDescriptor);
		return testDescriptor;
	}

	private ClassTestDescriptor mergeIntoTree(TestDescriptor parentTestDescriptor, ClassTestDescriptor testDescriptor) {
		Optional<? extends TestDescriptor> uniqueTestDescriptor = parentTestDescriptor.findByUniqueId(
			testDescriptor.getUniqueId());
		if (uniqueTestDescriptor.isPresent()) {
			return (ClassTestDescriptor) uniqueTestDescriptor.get();
		}
		else {
			parentTestDescriptor.addChild(testDescriptor);
			return testDescriptor;
		}
	}
}
