/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.discovery;

import static org.junit.gen5.commons.meta.API.Usage.Internal;
import static org.junit.gen5.commons.util.ReflectionUtils.*;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.discovery.ClassSelector;
import org.junit.gen5.engine.discovery.ClasspathSelector;
import org.junit.gen5.engine.discovery.MethodSelector;
import org.junit.gen5.engine.discovery.PackageSelector;
import org.junit.gen5.engine.discovery.UniqueIdSelector;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.JUnit5TestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.MethodTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.NestedClassTestDescriptor;
import org.junit.gen5.engine.support.descriptor.AbstractTestDescriptor;

/**
 * @since 5.0
 */
@API(Internal)
public class DiscoverySelectorResolver {
	private final JUnit5EngineDescriptor engineDescriptor;
	private final IsNestedTestClass isNestedTestClass = new IsNestedTestClass();
	private final IsTestMethod isTestMethod = new IsTestMethod();
	private final IsScannableTestClass isScannableTestClass = new IsScannableTestClass();

	public DiscoverySelectorResolver(JUnit5EngineDescriptor engineDescriptor) {
		this.engineDescriptor = engineDescriptor;
	}

	public void resolveSelectors(EngineDiscoveryRequest request) {
		request.getSelectorsByType(ClasspathSelector.class).forEach(selector -> {
			File rootDirectory = selector.getClasspathRoot();
			findAllClassesInClasspathRoot(rootDirectory, isScannableTestClass).stream().forEach(this::resolveTestClass);
		});
		request.getSelectorsByType(PackageSelector.class).forEach(selector -> {
			String packageName = selector.getPackageName();
			findAllClassesInPackage(packageName, isScannableTestClass).stream().forEach(this::resolveTestClass);
		});
		request.getSelectorsByType(ClassSelector.class).forEach(selector -> {
			resolveTestClass(selector.getTestClass());
		});
		request.getSelectorsByType(MethodSelector.class).forEach(selector -> {
			resolveTestMethod(selector.getTestClass(), selector.getTestMethod());
		});
		request.getSelectorsByType(UniqueIdSelector.class).forEach(selector -> {
			resolveUniqueId(UniqueId.parse(selector.getUniqueId()));
		});
	}

	private void resolveTestClass(Class<?> testClass) {
		JUnit5Testable testable = JUnit5Testable.fromClass(testClass, engineDescriptor.getUniqueId());
		resolveTestable(testable);
	}

	private void resolveTestMethod(Class<?> testClass, Method testMethod) {
		JUnit5Testable testable = JUnit5Testable.fromMethod(testMethod, testClass, engineDescriptor.getUniqueId());
		resolveTestable(testable);
	}

	private void resolveUniqueId(UniqueId uniqueId) {
		JUnit5Testable testable = JUnit5Testable.fromUniqueId(uniqueId, engineDescriptor.getUniqueId());
		resolveTestable(testable);
	}

	private void resolveTestable(JUnit5Testable testable, boolean withChildren) {
		testable.accept(new JUnit5Testable.Visitor() {
			@Override
			public void visitClass(UniqueId uniqueId, Class<?> testClass) {
				resolveClassTestable(testClass, uniqueId, engineDescriptor, withChildren);
			}

			@Override
			public void visitMethod(UniqueId uniqueId, Method method, Class<?> container) {
				resolveMethodTestable(method, container, uniqueId);
			}

			@Override
			public void visitNestedClass(UniqueId uniqueId, Class<?> testClass, Class<?> containerClass) {
				resolveNestedClassTestable(uniqueId, testClass, containerClass, withChildren);
			}
		});
	}

	private void resolveTestable(JUnit5Testable testable) {
		resolveTestable(testable, true);
	}

	private void resolveMethodTestable(Method method, Class<?> testClass, UniqueId uniqueId) {
		JUnit5Testable parentTestable = JUnit5Testable.fromClass(testClass, engineDescriptor.getUniqueId());
		TestDescriptor newParentDescriptor = resolveAndReturnParentTestable(parentTestable);
		MethodTestDescriptor descriptor = getOrCreateMethodDescriptor(testClass, method, uniqueId);
		newParentDescriptor.addChild(descriptor);
	}

	private void resolveClassTestable(Class<?> testClass, UniqueId uniqueId, AbstractTestDescriptor parentDescriptor,
			boolean withChildren) {
		JUnit5TestDescriptor descriptor = getOrCreateClassDescriptor(testClass, uniqueId);
		parentDescriptor.addChild(descriptor);

		if (withChildren) {
			resolveContainedNestedClasses(testClass);
			resolveContainedTestMethods(testClass, descriptor);
		}
	}

	private void resolveNestedClassTestable(UniqueId uniqueId, Class<?> testClass, Class<?> containerClass,
			boolean withChildren) {
		JUnit5Testable containerTestable = JUnit5Testable.fromClass(containerClass, engineDescriptor.getUniqueId());
		TestDescriptor parentDescriptor = resolveAndReturnParentTestable(containerTestable);
		NestedClassTestDescriptor descriptor = getOrCreateNestedClassDescriptor(testClass, uniqueId);
		parentDescriptor.addChild(descriptor);

		if (withChildren) {
			resolveContainedNestedClasses(testClass);
			resolveContainedTestMethods(testClass, descriptor);
		}
	}

	private TestDescriptor resolveAndReturnParentTestable(JUnit5Testable containerTestable) {
		resolveTestable(containerTestable, false);
		return descriptorByUniqueId(containerTestable.getUniqueId()).orElseThrow(() -> {
			String errorMessage = String.format("Testable with unique id %s could not be resolved. Programming error!",
				containerTestable.getUniqueId());
			return new RuntimeException(errorMessage);
		});
	}

	private void resolveContainedTestMethods(Class<?> testClass, AbstractTestDescriptor parentDescriptor) {
		List<Method> testMethodCandidates = findMethods(testClass, isTestMethod,
			ReflectionUtils.MethodSortOrder.HierarchyDown);
		for (Method method : testMethodCandidates) {
			JUnit5Testable methodTestable = JUnit5Testable.fromMethod(method, testClass,
				engineDescriptor.getUniqueId());
			MethodTestDescriptor methodDescriptor = getOrCreateMethodDescriptor(testClass, method,
				methodTestable.getUniqueId());
			parentDescriptor.addChild(methodDescriptor);
		}
	}

	private void resolveContainedNestedClasses(Class<?> clazz) {
		List<Class<?>> nestedClasses = findNestedClasses(clazz, isNestedTestClass);
		for (Class<?> nestedClass : nestedClasses) {
			JUnit5Testable nestedClassTestable = JUnit5Testable.fromClass(nestedClass, engineDescriptor.getUniqueId());
			resolveTestable(nestedClassTestable);
		}
	}

	private MethodTestDescriptor getOrCreateMethodDescriptor(Class<?> testClass, Method method, UniqueId uniqueId) {
		return (MethodTestDescriptor) descriptorByUniqueId(uniqueId).orElseGet(
			() -> new MethodTestDescriptor(uniqueId, testClass, method));
	}

	private NestedClassTestDescriptor getOrCreateNestedClassDescriptor(Class<?> clazz, UniqueId uniqueId) {
		return (NestedClassTestDescriptor) descriptorByUniqueId(uniqueId).orElseGet(
			() -> new NestedClassTestDescriptor(uniqueId, clazz));
	}

	private JUnit5TestDescriptor getOrCreateClassDescriptor(Class<?> clazz, UniqueId uniqueId) {
		return (JUnit5TestDescriptor) descriptorByUniqueId(uniqueId).orElseGet(
			() -> new ClassTestDescriptor(uniqueId, clazz));
	}

	@SuppressWarnings("unchecked")
	private Optional<TestDescriptor> descriptorByUniqueId(UniqueId uniqueId) {
		return (Optional<TestDescriptor>) engineDescriptor.findByUniqueId(uniqueId);
	}
}
