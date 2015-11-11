/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import static org.junit.gen5.commons.util.ReflectionUtils.findMethods;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.AbstractTestDescriptor;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.gen5.engine.TestPlanSpecificationVisitor;
import org.junit.gen5.engine.junit5.testers.CanBeTestClass;
import org.junit.gen5.engine.junit5.testers.IsTestClassWithTests;
import org.junit.gen5.engine.junit5.testers.IsTestMethod;

public class SpecificationResolver {

	private final EngineDescriptor engineDescriptor;

	private final CanBeTestClass canBeTestClass = new CanBeTestClass();
	private final IsTestMethod isTestMethod = new IsTestMethod();
	private final IsTestClassWithTests isTestClassWithTests = new IsTestClassWithTests();

	public SpecificationResolver(EngineDescriptor engineDescriptor) {
		this.engineDescriptor = engineDescriptor;
	}

	public void resolveElement(TestPlanSpecificationElement element) {
		element.accept(new TestPlanSpecificationVisitor() {

			@Override
			public void visitClassNameSpecification(String className) {
				resolveClassName(className);
			}

			@Override
			public void visitUniqueIdSpecification(String uniqueId) {
				resolveUniqueId(uniqueId);
			}

			@Override
			public void visitPackageSpecification(String packageName) {
				resolvePackage(packageName);
			}
		});
	}

	private void resolvePackage(String packageName) {
		Class<?>[] candidateClasses = ReflectionUtils.findAllClassesInPackage(packageName);
		Arrays.stream(candidateClasses).filter(isTestClassWithTests).forEach(
			testClass -> resolveTestable(JUnit5Testable.fromClass(testClass, engineDescriptor.getUniqueId())));
	}

	private void resolveClassName(String className) {
		JUnit5Testable testable = JUnit5Testable.fromClassName(className, engineDescriptor.getUniqueId());
		resolveTestable(testable);
	}

	private void resolveUniqueId(String uniqueId) {

		JUnit5Testable testable = JUnit5Testable.fromUniqueId(uniqueId, engineDescriptor.getUniqueId());
		resolveTestable(testable);
	}

	private void resolveTestable(JUnit5Testable testable) {
		testable.accept(new JUnit5Testable.Visitor() {

			@Override
			public void visitClass(String uniqueId, Class<?> testClass) {
				resolveClass(testClass, uniqueId, engineDescriptor, true);
			}

			@Override
			public void visitMethod(String uniqueId, Method method, Class<?> container) {
				resolveMethod(method, container, uniqueId, engineDescriptor);
			}

		});
	}

	private void resolveMethod(Method method, Class<?> testClass, String uniqueId, AbstractTestDescriptor parent) {
		if (!isTestMethod.test(method)) {
			throwCannotResolveMethodException(method);
		}
		JUnit5Testable parentId = JUnit5Testable.fromClass(testClass, engineDescriptor.getUniqueId());
		parent = resolveClass(testClass, parentId.getUniqueId(), parent, false);

		MethodTestDescriptor descriptor = getOrCreateMethodDescriptor(method, uniqueId);
		parent.addChild(descriptor);
	}

	private ClassTestDescriptor resolveClass(Class<?> clazz, String uniqueId, AbstractTestDescriptor parent,
			boolean withChildren) {
		if (!canBeTestClass.test(clazz)) {
			throwCannotResolveClassException(clazz);
		}
		ClassTestDescriptor descriptor = getOrCreateClassDescriptor(clazz, uniqueId);
		parent.addChild(descriptor);

		if (withChildren) {
			List<Method> testMethodCandidates = findMethods(clazz, isTestMethod,
				ReflectionUtils.MethodSortOrder.HierarchyDown);

			for (Method method : testMethodCandidates) {
				JUnit5Testable methodTestable = JUnit5Testable.fromMethod(method, clazz,
					engineDescriptor.getUniqueId());
				MethodTestDescriptor methodDescriptor = getOrCreateMethodDescriptor(method,
					methodTestable.getUniqueId());
				descriptor.addChild(methodDescriptor);
			}
		}
		return descriptor;
	}

	private MethodTestDescriptor getOrCreateMethodDescriptor(Method method, String uniqueId) {
		return (MethodTestDescriptor) engineDescriptor.findByUniqueId(uniqueId).orElse(
			new MethodTestDescriptor(uniqueId, method));
	}

	private ClassTestDescriptor getOrCreateClassDescriptor(Class<?> clazz, String uniqueId) {
		return (ClassTestDescriptor) engineDescriptor.findByUniqueId(uniqueId).orElse(
			new ClassTestDescriptor(uniqueId, clazz));
	}

	private static void throwCannotResolveMethodException(Method method) {
		throw new IllegalArgumentException(String.format("Method '%s' is not a test method.", method.getName()));
	}

	private static void throwCannotResolveClassException(Class<?> clazz) {
		throw new IllegalArgumentException(String.format("Class '%s' is not a test class.", clazz.getName()));
	}

}
