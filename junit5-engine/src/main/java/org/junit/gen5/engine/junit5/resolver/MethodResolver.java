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

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.gen5.api.Test;
import org.junit.gen5.commons.util.AnnotationUtils;
import org.junit.gen5.commons.util.ObjectUtils;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.MethodSpecification;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.MethodTestDescriptor;

public class MethodResolver extends JUnit5TestResolver {
	private static final Logger LOG = Logger.getLogger(MethodResolver.class.getName());

	private Pattern uniqueIdRegExPattern = Pattern.compile("^(.+?):([^#]+)#([^(]+)\\(((?:[^,)]+,?)*)\\)$");

	@Override
	public void resolveFor(TestDescriptor parent, TestPlanSpecification testPlanSpecification) {
		ObjectUtils.verifyNonNull(parent, "Parent must not be null!");
		ObjectUtils.verifyNonNull(testPlanSpecification, "TestPlanSpecification must not be null!");

		if (parent.isRoot()) {
			List<TestDescriptor> methodBasedTestMethods = resolveAllMethodsFromSpecification(parent, testPlanSpecification);
			getTestResolverRegistry().notifyResolvers(methodBasedTestMethods, testPlanSpecification);

			List<TestDescriptor> uniqueIdBasedTestMethods = resolveUniqueIdsFromSpecification(parent, testPlanSpecification);
			getTestResolverRegistry().notifyResolvers(uniqueIdBasedTestMethods, testPlanSpecification);
		}
		else if (parent instanceof ClassTestDescriptor) {
			List<TestDescriptor> resolvedTests = resolveTestMethodsOfTestClass((ClassTestDescriptor) parent);
			getTestResolverRegistry().notifyResolvers(resolvedTests, testPlanSpecification);
		}
	}

	private List<TestDescriptor> resolveAllMethodsFromSpecification(TestDescriptor parent,
			TestPlanSpecification testPlanSpecification) {
		List<TestDescriptor> result = new LinkedList<>();
		for (MethodSpecification method : testPlanSpecification.getMethods()) {
			result.add(getTestDescriptorForTestMethod(parent, method.getTestClass(), method.getTestMethod()));
		}
		return result;
	}

	private List<TestDescriptor> resolveUniqueIdsFromSpecification(TestDescriptor parent,
			TestPlanSpecification testPlanSpecification) {
		List<String> uniqueIds = testPlanSpecification.getUniqueIds();
		List<TestDescriptor> result = new LinkedList<>();

		for (String uniqueId : uniqueIds) {
			Matcher matcher = uniqueIdRegExPattern.matcher(uniqueId);
			if (matcher.matches()) {
				try {
					String className = matcher.group(2);
					String methodName = matcher.group(3);
					String parameterTypeNames = matcher.group(4);

					Class<?> testClass = Class.forName(className);
					Optional<Method> testMethodOptional = Optional.empty();

					if (parameterTypeNames.isEmpty()) {
						testMethodOptional = ReflectionUtils.findMethod(testClass, methodName);
					}
					else {
						Class<?>[] parameterTypes = getParameterTypes(parameterTypeNames.split(","));
						testMethodOptional = ReflectionUtils.findMethod(testClass, methodName, parameterTypes);
					}

					if (testMethodOptional.isPresent()) {
						result.add(getTestDescriptorForTestMethod(parent, testClass, testMethodOptional.get()));
					}
					else {
						LOG.fine(() -> "Skipping uniqueId " + uniqueId
								+ ": UniqueId does not seem to represent a valid test method.");
					}
				}
				catch (ClassNotFoundException e) {
					LOG.fine(() -> "Skipping uniqueId " + uniqueId
							+ ": UniqueId does not seem to represent a valid test method.");
				}
			}
		}

		return result;
	}

	private List<TestDescriptor> resolveTestMethodsOfTestClass(ClassTestDescriptor parent) {
		Class<?> testClass = parent.getTestClass();
		List<Method> methods = ReflectionUtils.findMethods(testClass,
			(method) -> AnnotationUtils.isAnnotated(method, Test.class));

		List<TestDescriptor> result = new LinkedList<>();
		for (Method method : methods) {
			result.add(getTestDescriptorForTestMethod(parent, method));
		}
		return result;
	}

	private TestDescriptor getTestDescriptorForTestMethod(ClassTestDescriptor parent, Method method) {
		return getTestDescriptorForTestMethod(parent, parent.getTestClass(), method);
	}

	private TestDescriptor getTestDescriptorForTestMethod(TestDescriptor parent, Class<?> testClass, Method method) {
		MethodTestDescriptor testDescriptor = new MethodTestDescriptor(getTestEngine(), testClass, method);
		parent.addChild(testDescriptor);
		return testDescriptor;
	}

	private Class<?>[] getParameterTypes(String[] parameterTypeNames) throws ClassNotFoundException {
		Class<?>[] parameterTypes = new Class[parameterTypeNames.length];
		for (int i = 0; i < parameterTypeNames.length; i++) {
			parameterTypes[i] = Class.forName(parameterTypeNames[i]);
		}
		return parameterTypes;
	}
}