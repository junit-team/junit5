/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.resolver;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.junit.gen5.commons.util.ReflectionUtils.MethodSortOrder.HierarchyDown;
import static org.junit.gen5.commons.util.ReflectionUtils.findMethods;
import static org.junit.gen5.engine.discovery.ClassSelector.forClass;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.discovery.MethodSelector;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.MethodTestDescriptor;
import org.junit.gen5.engine.junit5.discovery.IsTestMethod;

/**
 * @since 5.0
 */
public class MethodResolver extends JUnit5TestResolver {
	private static final String RESOLVER_ID = "method";

	private final IsTestMethod isTestMethod = new IsTestMethod();

	public static MethodTestDescriptor resolveMethod(TestDescriptor parent, Class<?> testClass, Method testMethod) {

		return fetchFromTreeOrCreateNew(parent, UniqueId.from(RESOLVER_ID, getSignatureFromMethod(testMethod)),
			(uniqueId) -> new MethodTestDescriptor(uniqueId.toString(), testClass, testMethod));
	}

	@Override
	public void bindTestResolveryRegistry(TestResolverRegistry testResolverRegistry) {
		super.bindTestResolveryRegistry(testResolverRegistry);
	}

	@Override
	public void resolveAllFrom(TestDescriptor parent, EngineDiscoveryRequest discoveryRequest) {
		Preconditions.notNull(parent, "parent must not be null!");
		Preconditions.notNull(discoveryRequest, "discoveryRequest must not be null!");

		if (parent.isRoot()) {
			resolveMethodsFromSelectors(parent, discoveryRequest);
		}
		else if (parent instanceof ClassTestDescriptor) {
			Class<?> testClass = ((ClassTestDescriptor) parent).getTestClass();
			resolveTestMethodsInClasses(testClass, parent, discoveryRequest);
		}
	}

	private void resolveMethodsFromSelectors(TestDescriptor root, EngineDiscoveryRequest discoveryRequest) {
		List<MethodSelector> methodSelectors = discoveryRequest.getSelectorsByType(MethodSelector.class);

		for (MethodSelector methodSelector : methodSelectors) {
			DiscoverySelector selector = forClass(methodSelector.getTestClass());
			TestDescriptor parent = getTestResolverRegistry().fetchParent(selector, root);
			MethodTestDescriptor child = resolveMethod(parent, methodSelector.getTestClass(),
				methodSelector.getTestMethod());
			parent.addChild(child);
			getTestResolverRegistry().notifyResolvers(child, discoveryRequest);
		}
	}

	private void resolveTestMethodsInClasses(Class<?> testClass, TestDescriptor parent,
			EngineDiscoveryRequest discoveryRequest) {
		// @formatter:off
        List<MethodTestDescriptor> testMethods = findMethods(testClass, isTestMethod::test, HierarchyDown).stream()
                .map(testMethod -> resolveMethod(parent, testClass, testMethod))
                .collect(toList());
        // @formatter:on

		for (MethodTestDescriptor child : testMethods) {
			parent.addChild(child);
			getTestResolverRegistry().notifyResolvers(child, discoveryRequest);
		}
	}

	@Override
	public void resolveUniqueId(TestDescriptor parent, UniqueId uniqueId, EngineDiscoveryRequest discoveryRequest) {
		if (uniqueId.currentKey().equals(RESOLVER_ID) && parent instanceof ClassTestDescriptor) {
			Class<?> testClass = ((ClassTestDescriptor) parent).getTestClass();
			Optional<Method> method = getMethodFromSignature(testClass, uniqueId.currentValue());
			if (method.isPresent()) {
				TestDescriptor next = resolveMethod(parent, testClass, method.get());
				parent.addChild(next);
				getTestResolverRegistry().resolveUniqueId(next, uniqueId.getRemainder(), discoveryRequest);
			}
		}
	}

	@Override
	public Optional<TestDescriptor> fetchBySelector(DiscoverySelector selector, TestDescriptor root) {
		return Optional.empty();
	}

	private static String getSignatureFromMethod(Method testMethod) {
		// @formatter:off
        String parameterTypeList = Arrays.stream(testMethod.getParameterTypes())
                .map(Class::getName)
                .collect(joining(","));
        // @formatter:on
		return String.format("%s(%s)", testMethod.getName(), parameterTypeList);
	}

	private static Optional<Method> getMethodFromSignature(Class<?> testClass, String methodSignature) {
		int index = methodSignature.indexOf('(');
		String methodName = methodSignature.substring(0, index);
		boolean methodHasNoParameters = (methodName.length() == methodSignature.length() - 2);
		if (methodHasNoParameters) {
			return ReflectionUtils.findMethod(testClass, methodName);
		}
		else {
			String[] parameterTypeNames = methodSignature.substring(index + 1, methodSignature.length() - 1).split(",");
			// @formatter:off
            Class[] parameterTypes = Arrays.stream(parameterTypeNames)
                    .map(ReflectionUtils::loadClass)
                    .map(Optional::get)
                    .collect(toList())
                    .toArray(new Class[parameterTypeNames.length]);
            // @formatter:on
			return ReflectionUtils.findMethod(testClass, methodName, parameterTypes);
		}
	}
}
