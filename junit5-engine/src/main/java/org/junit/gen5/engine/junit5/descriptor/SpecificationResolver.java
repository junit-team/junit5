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

import static org.junit.gen5.commons.util.ReflectionUtils.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.AbstractTestDescriptor;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.gen5.engine.TestPlanSpecificationVisitor;
import org.junit.gen5.engine.junit5.testers.CanBeTestClass;
import org.junit.gen5.engine.junit5.testers.IsTestClassWithTests;
import org.junit.gen5.engine.junit5.testers.IsTestContext;
import org.junit.gen5.engine.junit5.testers.IsTestMethod;

public class SpecificationResolver {

	private final Set<TestDescriptor> testDescriptors;
	private final EngineDescriptor engineDescriptor;

	private final CanBeTestClass canBeTestClass = new CanBeTestClass();
	private final IsTestContext isTestContext = new IsTestContext();
	private final IsTestMethod isTestMethod = new IsTestMethod();
	private final IsTestClassWithTests isTestClassWithTests = new IsTestClassWithTests();

	public SpecificationResolver(Set<TestDescriptor> testDescriptors, EngineDescriptor engineDescriptor) {
		this.testDescriptors = testDescriptors;
		this.engineDescriptor = engineDescriptor;
	}

	public void resolveElement(TestPlanSpecificationElement element) {
		element.accept(new TestPlanSpecificationVisitor() {

			@Override
			public void visitClassNameSpecification(String className) {
				resolveClassNameSpecification(className);
			}

			@Override
			public void visitUniqueIdSpecification(String uniqueId) {
				resolveUniqueIdSpecification(uniqueId);
			}

			@Override
			public void visitPackageSpecification(String packageName) {
				resolvePackageSpecification(packageName);
			}
		});
	}

	private void resolvePackageSpecification(String packageName) {
		Class<?>[] candidateClasses = ReflectionUtils.findAllClassesInPackage(packageName);
		Arrays.stream(candidateClasses).filter(isTestClassWithTests).forEach(
			testClass -> resolveTestable(JUnit5Testable.fromClass(testClass, engineDescriptor.getUniqueId())));
	}

	private void resolveClassNameSpecification(String className) {
		JUnit5Testable testable = JUnit5Testable.fromClassName(className, engineDescriptor.getUniqueId());
		resolveTestable(testable);
	}

	private void resolveUniqueIdSpecification(String uniqueId) {

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
				resolveMethodTestable(method, container, uniqueId, engineDescriptor);
			}

			@Override
			public void visitContext(String uniqueId, Class<?> testClass, Class<?> containerClass) {
				resolveContextTestable(uniqueId, testClass, containerClass, withChildren);
			}
		});
	}

	private void resolveTestable(JUnit5Testable testable) {
		resolveTestable(testable, true);
	}

	private void resolveMethodTestable(Method method, Class<?> testClass, String uniqueId,
			AbstractTestDescriptor parentDescriptor) {
		JUnit5Testable parentTestable = JUnit5Testable.fromClass(testClass, engineDescriptor.getUniqueId());
		AbstractTestDescriptor newParentDescriptor = resolveAndReturnParentTestable(parentTestable);
		MethodTestDescriptor descriptor = getOrCreateMethodDescriptor(method, uniqueId);
		newParentDescriptor.addChild(descriptor);
		testDescriptors.add(descriptor);
	}

	private void resolveClassTestable(Class<?> testClass, String uniqueId, AbstractTestDescriptor parentDescriptor,
			boolean withChildren) {
		ClassTestDescriptor descriptor = getOrCreateClassDescriptor(testClass, uniqueId);
		parentDescriptor.addChild(descriptor);

		if (withChildren) {
			resolveContainedContexts(testClass);
			resolveContainedTestMethods(testClass, descriptor);
		}
		testDescriptors.add(descriptor);
	}

	private void resolveContextTestable(String uniqueId, Class<?> testClass, Class<?> containerClass,
			boolean withChildren) {
		JUnit5Testable containerTestable = JUnit5Testable.fromClass(containerClass, engineDescriptor.getUniqueId());
		AbstractTestDescriptor parentDescriptor = resolveAndReturnParentTestable(containerTestable);
		ContextTestDescriptor descriptor = getOrCreateContextDescriptor(testClass, uniqueId);
		parentDescriptor.addChild(descriptor);

		if (withChildren) {
			resolveContainedContexts(testClass);
			resolveContainedTestMethods(testClass, descriptor);
		}
		testDescriptors.add(descriptor);
	}

	private AbstractTestDescriptor resolveAndReturnParentTestable(JUnit5Testable containerTestable) {
		resolveTestable(containerTestable, false);
		return descriptorByUniqueId(containerTestable.getUniqueId());
	}

	private void resolveContainedTestMethods(Class<?> testClass, AbstractTestDescriptor parentDescriptor) {
		List<Method> testMethodCandidates = findMethods(testClass, isTestMethod, MethodSortOrder.HierarchyDown);
		for (Method method : testMethodCandidates) {
			JUnit5Testable methodTestable = JUnit5Testable.fromMethod(method, testClass,
				engineDescriptor.getUniqueId());
			MethodTestDescriptor methodDescriptor = getOrCreateMethodDescriptor(method, methodTestable.getUniqueId());
			parentDescriptor.addChild(methodDescriptor);
		}
	}

	private void resolveContainedContexts(Class<?> clazz) {
		List<Class<?>> contextClasses = findInnerClasses(clazz, isTestContext);
		for (Class<?> contextClass : contextClasses) {
			JUnit5Testable contextTestable = JUnit5Testable.fromClass(contextClass, engineDescriptor.getUniqueId());
			resolveTestable(contextTestable);
		}
	}

	private MethodTestDescriptor getOrCreateMethodDescriptor(Method method, String uniqueId) {
		MethodTestDescriptor methodTestDescriptor = (MethodTestDescriptor) descriptorByUniqueId(uniqueId);
		if (methodTestDescriptor == null) {
			methodTestDescriptor = new MethodTestDescriptor(uniqueId, method);
			testDescriptors.add(methodTestDescriptor);
		}
		return methodTestDescriptor;
	}

	private ContextTestDescriptor getOrCreateContextDescriptor(Class<?> clazz, String uniqueId) {
		ContextTestDescriptor contextTestDescriptor = (ContextTestDescriptor) descriptorByUniqueId(uniqueId);
		if (contextTestDescriptor == null) {
			contextTestDescriptor = new ContextTestDescriptor(uniqueId, clazz);
			testDescriptors.add(contextTestDescriptor);
		}
		return contextTestDescriptor;
	}

	private ClassTestDescriptor getOrCreateClassDescriptor(Class<?> clazz, String uniqueId) {
		ClassTestDescriptor classTestDescriptor = (ClassTestDescriptor) descriptorByUniqueId(uniqueId);
		if (classTestDescriptor == null) {
			classTestDescriptor = new ClassTestDescriptor(uniqueId, clazz);
			testDescriptors.add(classTestDescriptor);
		}
		return classTestDescriptor;
	}

	private AbstractTestDescriptor descriptorByUniqueId(String uniqueId) {
		for (TestDescriptor descriptor : testDescriptors) {
			if (descriptor.getUniqueId().equals(uniqueId)) {
				return (AbstractTestDescriptor) descriptor;
			}
		}
		return null;
	}

}
