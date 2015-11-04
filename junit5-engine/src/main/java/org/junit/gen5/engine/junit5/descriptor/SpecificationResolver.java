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

import static org.junit.gen5.commons.util.AnnotationUtils.findMethods;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

import org.junit.gen5.commons.util.AnnotationUtils;
import org.junit.gen5.engine.AbstractTestDescriptor;
import org.junit.gen5.engine.ClassNameSpecification;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.gen5.engine.UniqueIdSpecification;

public class SpecificationResolver {

	private final Set<TestDescriptor> testDescriptors;
	private final EngineDescriptor root;

	private final TestClassTester classTester = new TestClassTester();
	private final TestMethodTester methodTester = new TestMethodTester();

	public SpecificationResolver(Set testDescriptors, EngineDescriptor root) {
		this.testDescriptors = testDescriptors;
		this.root = root;
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
		UniqueId uniqueId = UniqueId.fromClassName(element.getClassName(), root);
		resolveUniqueId(uniqueId);
	}

	private void resolveUniqueIdSpecification(UniqueIdSpecification uniqueIdSpecification) {
		UniqueId uniqueId = UniqueId.fromUniqueId(uniqueIdSpecification.getUniqueId(), root);
		resolveUniqueId(uniqueId);
	}

	private void resolveUniqueId(UniqueId uniqueId) {
		if (uniqueId.getJavaElement() instanceof Class) {
			resolveClass((Class<?>) uniqueId.getJavaElement(), uniqueId.getUniqueId(), root, true);
		}
		if (uniqueId.getJavaElement() instanceof Method) {
			resolveMethod((Method) uniqueId.getJavaElement(), uniqueId.getUniqueId(), root, true);
		}
	}

	private void resolveMethod(Method method, String uniqueId, EngineDescriptor root, boolean withChildren) {
		if (!methodTester.accept(method)) {
			throwCannotResolveMethodException(method);
		}
	}

	private ClassTestDescriptor resolveClass(Class<?> clazz, String uniqueId, AbstractTestDescriptor parent,
			boolean withChildren) {
		if (!classTester.accept(clazz)) {
			throwCannotResolveClassException(clazz);
		}
		if (clazz.isMemberClass()) {
			Class<?> enclosingClass = clazz.getEnclosingClass();
			UniqueId parentId = UniqueId.fromClass(enclosingClass, root);
			parent = resolveClass(enclosingClass, parentId.getUniqueId(), parent, false);
		}
		ClassTestDescriptor descriptor = new ClassTestDescriptor(uniqueId, clazz);
		parent.addChild(descriptor);
		testDescriptors.add(descriptor);

		if (withChildren) {
			List<Method> testMethodCandidates = findMethods(clazz, methodTester::accept,
				AnnotationUtils.MethodSortOrder.HierarchyDown);

			for (Method method : testMethodCandidates) {
				UniqueId methodId = UniqueId.fromMethod(method, clazz, root);
				MethodTestDescriptor methodDescriptor = new MethodTestDescriptor(methodId.getUniqueId(), method);
				descriptor.addChild(methodDescriptor);
				testDescriptors.add(methodDescriptor);
			}
		}
		return descriptor;
	}

	private static void throwCannotResolveMethodException(Method method) {
		throw new IllegalArgumentException(String.format("Method '%s' is not a test method.", method.getName()));
	}

	private static void throwCannotResolveClassException(Class<?> clazz) {
		throw new IllegalArgumentException(String.format("Class '%s' is not a test class.", clazz.getName()));
	}

}
