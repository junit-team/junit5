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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.gen5.api.Test;
import org.junit.gen5.commons.util.ObjectUtils;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.MethodTestDescriptor;

public class MethodResolver implements TestResolver {
	@Override
	public TestResolverResult resolveFor(TestDescriptor parent, TestPlanSpecification testPlanSpecification) {
		ObjectUtils.verifyNonNull(parent, "Parent must not be null!");
		ObjectUtils.verifyNonNull(testPlanSpecification, "TestPlanSpecification must not be null!");

		if (parent instanceof ClassTestDescriptor) {
			List<TestDescriptor> resolvedTests = resolveTestMethodsOfTestClass(parent);
			return TestResolverResult.proceedResolving(resolvedTests);
		}
		else {
			return TestResolverResult.empty();
		}
	}

	@Override
	public TestResolverResult resolveFor(String uniqueId, TestDescriptor parent, TestPlanSpecification testPlanSpecification) {
		return null;
	}

	private List<TestDescriptor> resolveTestMethodsOfTestClass(TestDescriptor parent) {
		ClassTestDescriptor classTestDescriptor = (ClassTestDescriptor) parent;
		Class<?> testClass = classTestDescriptor.getTestClass();
		List<Method> methods = ReflectionUtils.findMethods(testClass,
			(method) -> method.isAnnotationPresent(Test.class));

		List<TestDescriptor> result = new LinkedList<>();
		for (Method method : methods) {
			result.add(getTestForMethod(classTestDescriptor, method));
		}
		return result;
	}

	private TestDescriptor getTestForMethod(ClassTestDescriptor parent, Method method) {
		String parentUniqueId = parent.getUniqueId();
		String uniqueId = String.format("%s#%s()", parentUniqueId, method.getName());

		MethodTestDescriptor testDescriptor = new MethodTestDescriptor(uniqueId, parent.getTestClass(), method);
		parent.addChild(testDescriptor);
		return testDescriptor;
	}
}