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
import java.util.Set;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.AbstractTestDescriptor;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.gen5.engine.junit5.JUnit5TestEngine;
import org.junit.gen5.engine.junit5.testers.CanBeTestClass;
import org.junit.gen5.engine.junit5.testers.IsTestClassWithTests;
import org.junit.gen5.engine.junit5.testers.IsTestMethod;

public class SpecificationResolver {

	private final JUnit5TestEngine testEngine;
	private final Set<TestDescriptor> testDescriptors;
	private final AbstractTestDescriptor parent;

	private final CanBeTestClass canBeTestClass = new CanBeTestClass();
	private final IsTestMethod isTestMethod = new IsTestMethod();
	private final IsTestClassWithTests isTestClassWithTests = new IsTestClassWithTests();

	public SpecificationResolver(JUnit5TestEngine testEngine, Set testDescriptors, AbstractTestDescriptor parent) {
		this.testEngine = testEngine;
		this.testDescriptors = testDescriptors;
		this.parent = parent;
	}

	public void resolveElement(TestPlanSpecificationElement element) {
		element.accept(new TestPlanSpecificationElement.Visitor() {
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
			testClass -> resolveTestable(testEngine.fromClass(testClass)));
	}

	private void resolveClassName(String className) {
		JUnit5Testable testable = testEngine.fromClassName(className);
		resolveTestable(testable);
	}

	private void resolveUniqueId(String uniqueId) {
		JUnit5Testable testable = testEngine.fromUniqueId(uniqueId);
		resolveTestable(testable);
	}

	private void resolveTestable(JUnit5Testable testable) {
		testable.accept(new JUnit5Testable.Visitor() {
			@Override
			public void visitClass(String uniqueId, Class<?> testClass) {
				resolveClass(testClass, uniqueId, parent, true);
			}

			@Override
			public void visitMethod(String uniqueId, Method method, Class<?> container) {
				resolveMethod(method, container, uniqueId, parent);
			}
		});
	}

	private void resolveMethod(Method method, Class<?> testClass, String uniqueId, AbstractTestDescriptor parent) {
		if (!isTestMethod.test(method)) {
			throwCannotResolveMethodException(method);
		}
		JUnit5Testable parentId = testEngine.fromClass(testClass);
		parent = resolveClass(testClass, parentId.getUniqueId(), parent, false);

		MethodTestDescriptor descriptor = getOrCreateMethodDescriptor(method, uniqueId);
		parent.addChild(descriptor);
		testDescriptors.add(descriptor);
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
				JUnit5Testable methodTestable = testEngine.fromMethod(method, clazz
				);
				MethodTestDescriptor methodDescriptor = getOrCreateMethodDescriptor(method,
					methodTestable.getUniqueId());
				descriptor.addChild(methodDescriptor);
			}
		}
		return descriptor;
	}

	private MethodTestDescriptor getOrCreateMethodDescriptor(Method method, String uniqueId) {
		MethodTestDescriptor methodTestDescriptor = (MethodTestDescriptor) descriptorByUniqueId(uniqueId);
		if (methodTestDescriptor == null) {
			methodTestDescriptor = new MethodTestDescriptor(uniqueId, method);
			testDescriptors.add(methodTestDescriptor);
		}
		return methodTestDescriptor;
	}

	private ClassTestDescriptor getOrCreateClassDescriptor(Class<?> clazz, String uniqueId) {
		ClassTestDescriptor classTestDescriptor = (ClassTestDescriptor) descriptorByUniqueId(uniqueId);
		if (classTestDescriptor == null) {
			classTestDescriptor = new ClassTestDescriptor(uniqueId, clazz);
			testDescriptors.add(classTestDescriptor);
		}
		return classTestDescriptor;
	}

	private TestDescriptor descriptorByUniqueId(String uniqueId) {
		for (TestDescriptor descriptor : testDescriptors) {
			if (descriptor.getUniqueId().equals(uniqueId)) {
				return descriptor;
			}
		}
		return null;
	}

	private static void throwCannotResolveMethodException(Method method) {
		throw new IllegalArgumentException(String.format("Method '%s' is not a test method.", method.getName()));
	}

	private static void throwCannotResolveClassException(Class<?> clazz) {
		throw new IllegalArgumentException(String.format("Class '%s' is not a test class.", clazz.getName()));
	}

}
