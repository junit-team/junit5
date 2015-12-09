/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5ext.resolver;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.junit.gen5.api.Test;
import org.junit.gen5.commons.util.ObjectUtils;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.MutableTestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5ext.descriptor.ClassDescriptor;
import org.junit.gen5.engine.junit5ext.descriptor.MethodDescriptor;

public class MethodResolver implements TestResolver {
	@Override
	public List<MutableTestDescriptor> resolveFor(MutableTestDescriptor parent,
			TestPlanSpecification testPlanSpecification) {
		ObjectUtils.verifyNonNull(parent, "Parent must not be null!");
		ObjectUtils.verifyNonNull(testPlanSpecification, "TestPlanSpecification must not be null!");

		if (parent instanceof ClassDescriptor) {
			ClassDescriptor classDescriptor = (ClassDescriptor) parent;
			Class<?> testClass = classDescriptor.getTestClass();
			List<Method> methods = ReflectionUtils.findMethods(testClass,
				(method) -> method.isAnnotationPresent(Test.class));

			List<MutableTestDescriptor> result = new LinkedList<>();
			for (Method method : methods) {
				result.add(getTestForMethod(parent, method));
			}
			return result;
		}
		else {
			return Collections.emptyList();
		}
	}

	private MutableTestDescriptor getTestForMethod(MutableTestDescriptor parent, Method method) {
		String parentUniqueId = parent.getUniqueId();
		String uniqueId = String.format("%s#%s()", parentUniqueId, method.getName());
		String displayName = method.getName();

		MethodDescriptor methodDescriptor = new MethodDescriptor(method, uniqueId, displayName);
		methodDescriptor.setParent(parent);
		return methodDescriptor;
	}
}
