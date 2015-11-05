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
import java.util.List;
import java.util.Set;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.AbstractTestDescriptor;
import org.junit.gen5.engine.ClassNameSpecification;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.gen5.engine.UniqueIdSpecification;

public class SpecificationResolver {

	private final Set<TestDescriptor> testDescriptors;
	private final EngineDescriptor engineDescriptor;

	private final TestClassTester classTester = new TestClassTester();
	private final TestMethodTester methodTester = new TestMethodTester();

	public SpecificationResolver(Set testDescriptors, EngineDescriptor engineDescriptor) {
		this.testDescriptors = testDescriptors;
		this.engineDescriptor = engineDescriptor;
	}

	public void resolveElement(TestPlanSpecificationElement element) {
		if (element.getClass() == ClassNameSpecification.class) {
			resolveClassNameSpecification((ClassNameSpecification) element);
		}
		if (element.getClass() == UniqueIdSpecification.class) {
			resolveUniqueIdSpecification((UniqueIdSpecification) element);
		}
	}

	private void resolveClassNameSpecification(ClassNameSpecification element) {
		JUnit5Testable testable = JUnit5Testable.fromClassName(element.getClassName(), engineDescriptor.getUniqueId());
		resolveUniqueId(testable);
	}

	private void resolveUniqueIdSpecification(UniqueIdSpecification uniqueIdSpecification) {

		JUnit5Testable testable = JUnit5Testable.fromUniqueId(uniqueIdSpecification.getUniqueId(),
			engineDescriptor.getUniqueId());
		resolveUniqueId(testable);
	}

	private void resolveUniqueId(JUnit5Testable testable) {
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
		if (!methodTester.accept(method)) {
			throwCannotResolveMethodException(method);
		}
		JUnit5Testable parentId = JUnit5Testable.fromClass(testClass, engineDescriptor.getUniqueId());
		parent = resolveClass(testClass, parentId.getUniqueId(), parent, false);

		MethodTestDescriptor descriptor = getOrCreateMethodDescriptor(method, uniqueId);
		parent.addChild(descriptor);
		testDescriptors.add(descriptor);
	}

	private ClassTestDescriptor resolveClass(Class<?> clazz, String uniqueId, AbstractTestDescriptor parent,
			boolean withChildren) {
		if (!classTester.accept(clazz)) {
			throwCannotResolveClassException(clazz);
		}
		if (clazz.isMemberClass()) {
			Class<?> enclosingClass = clazz.getEnclosingClass();
			JUnit5Testable parentId = JUnit5Testable.fromClass(enclosingClass, engineDescriptor.getUniqueId());
			parent = resolveClass(enclosingClass, parentId.getUniqueId(), parent, false);
		}
		ClassTestDescriptor descriptor = getOrCreateClassDescriptor(clazz, uniqueId);
		parent.addChild(descriptor);

		if (withChildren) {
			List<Method> testMethodCandidates = findMethods(clazz, methodTester::accept,
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
