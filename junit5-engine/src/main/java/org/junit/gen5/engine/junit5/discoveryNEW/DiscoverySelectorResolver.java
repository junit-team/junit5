/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.discoveryNEW;

import static org.junit.gen5.commons.util.ReflectionUtils.findMethods;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.discovery.ClassSelector;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.MethodTestDescriptor;
import org.junit.gen5.engine.junit5.discovery.IsPotentialTestContainer;
import org.junit.gen5.engine.junit5.discovery.IsTestMethod;
import org.junit.gen5.engine.junit5.discovery.JUnit5EngineDescriptor;

public class DiscoverySelectorResolver {
	private final JUnit5EngineDescriptor engineDescriptor;

	public DiscoverySelectorResolver(JUnit5EngineDescriptor engineDescriptor) {
		this.engineDescriptor = engineDescriptor;
	}

	public void resolveSelectors(EngineDiscoveryRequest request) {
		request.getSelectorsByType(ClassSelector.class).forEach(selector -> {
			resolveClass(selector.getTestClass());
		});

	}

	private void resolveClass(Class<?> testClass) {
		TestDescriptor parent = engineDescriptor;
		resolve(testClass, parent, true);
	}

	private void resolve(Class<?> testClass, TestDescriptor parent, boolean withChildren) {
		if (!new IsPotentialTestContainer().test(testClass))
			return;
		UniqueId uniqueId = parent.getUniqueId().append("class", testClass.getName());
		ClassTestDescriptor descriptor = new ClassTestDescriptor(uniqueId, testClass);
		parent.addChild(descriptor);

		if (withChildren) {
			List<Method> testMethodCandidates = findMethods(testClass, new IsTestMethod(),
				ReflectionUtils.MethodSortOrder.HierarchyDown);
			testMethodCandidates.forEach(method -> resolve(method, descriptor, true));
		}
	}

	private void resolve(Method testMethod, TestDescriptor parent, boolean withChildren) {
		ClassTestDescriptor parentClassDescriptor = (ClassTestDescriptor) parent;
		UniqueId uniqueId = parentClassDescriptor.getUniqueId().append("method", testMethod.getName() + "()");
		MethodTestDescriptor methodTestDescriptor = new MethodTestDescriptor(uniqueId,
			parentClassDescriptor.getTestClass(), testMethod);
		parentClassDescriptor.addChild(methodTestDescriptor);
	}
}
