/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import static org.junit.gen5.commons.util.ReflectionUtils.*;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.AbstractTestDescriptor;
import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.DiscoverySelectorVisitor;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5.discovery.IsNestedTestClass;
import org.junit.gen5.engine.junit5.discovery.IsScannableTestClass;
import org.junit.gen5.engine.junit5.discovery.IsTestMethod;

/**
 * @since 5.0
 */
public class DiscoverySelectorResolver {
	private final JUnit5EngineDescriptor engineDescriptor;
	private final IsNestedTestClass isNestedTestClass = new IsNestedTestClass();
	private final IsTestMethod isTestMethod = new IsTestMethod();
	private final IsScannableTestClass isScannableTestClass = new IsScannableTestClass();

	public DiscoverySelectorResolver(JUnit5EngineDescriptor engineDescriptor) {
		this.engineDescriptor = engineDescriptor;
	}

	public void resolveElement(DiscoverySelector element) {
		element.accept(new DiscoverySelectorVisitor() {
			@Override
			public void visitClass(Class<?> testClass) {
				resolveTestClass(testClass);
			}

			@Override
			public void visitMethod(Class<?> testClass, Method testMethod) {
				resolveTestMethod(testClass, testMethod);
			}

			@Override
			public void visitUniqueId(String uniqueId) {
				resolveUniqueId(uniqueId);
			}

			@Override
			public void visitPackage(String packageName) {
				findAllClassesInPackage(packageName, isScannableTestClass).stream().forEach(this::visitClass);
			}

			@Override
			public void visitAllTests(File rootDirectory) {
				findAllClassesInClasspathRoot(rootDirectory, isScannableTestClass).stream().forEach(this::visitClass);
			}
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

	private void resolveUniqueId(String uniqueId) {
		JUnit5Testable testable = JUnit5Testable.fromUniqueId(uniqueId, engineDescriptor.getUniqueId());
		resolveTestable(testable);
	}

	private void resolveTestable(JUnit5Testable testable, boolean withChildren) {
		testable.accept(new JUnit5Testable.Visitor() {
			@Override
			public void visitClass(String uniqueId, Class<?> testClass) {
				resolveClassTestable(testClass, uniqueId, engineDescriptor, withChildren);
			}

			@Override
			public void visitMethod(String uniqueId, Method method, Class<?> container) {
				resolveMethodTestable(method, container, uniqueId);
			}

			@Override
			public void visitNestedClass(String uniqueId, Class<?> testClass, Class<?> containerClass) {
				resolveNestedClassTestable(uniqueId, testClass, containerClass, withChildren);
			}
		});
	}

	private void resolveTestable(JUnit5Testable testable) {
		resolveTestable(testable, true);
	}

	private void resolveMethodTestable(Method method, Class<?> testClass, String uniqueId) {
		JUnit5Testable parentTestable = JUnit5Testable.fromClass(testClass, engineDescriptor.getUniqueId());
		TestDescriptor newParentDescriptor = resolveAndReturnParentTestable(parentTestable);
		MethodTestDescriptor descriptor = getOrCreateMethodDescriptor(testClass, method, uniqueId);
		newParentDescriptor.addChild(descriptor);
	}

	private void resolveClassTestable(Class<?> testClass, String uniqueId, AbstractTestDescriptor parentDescriptor,
			boolean withChildren) {
		JUnit5TestDescriptor descriptor = getOrCreateClassDescriptor(testClass, uniqueId);
		parentDescriptor.addChild(descriptor);

		if (withChildren) {
			resolveContainedNestedClasses(testClass);
			resolveContainedTestMethods(testClass, descriptor);
		}
	}

	private void resolveNestedClassTestable(String uniqueId, Class<?> testClass, Class<?> containerClass,
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

	private MethodTestDescriptor getOrCreateMethodDescriptor(Class<?> testClass, Method method, String uniqueId) {
		return (MethodTestDescriptor) descriptorByUniqueId(uniqueId).orElseGet(
			() -> new MethodTestDescriptor(uniqueId, testClass, method));
	}

	private NestedClassTestDescriptor getOrCreateNestedClassDescriptor(Class<?> clazz, String uniqueId) {
		return (NestedClassTestDescriptor) descriptorByUniqueId(uniqueId).orElseGet(
			() -> new NestedClassTestDescriptor(uniqueId, clazz));
	}

	private JUnit5TestDescriptor getOrCreateClassDescriptor(Class<?> clazz, String uniqueId) {
		return (JUnit5TestDescriptor) descriptorByUniqueId(uniqueId).orElseGet(
			() -> new ClassTestDescriptor(uniqueId, clazz));
	}

	@SuppressWarnings("unchecked")
	private Optional<TestDescriptor> descriptorByUniqueId(String uniqueId) {
		return (Optional<TestDescriptor>) engineDescriptor.findByUniqueId(uniqueId);
	}

}
