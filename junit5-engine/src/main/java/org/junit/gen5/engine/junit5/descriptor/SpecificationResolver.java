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

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.AbstractTestDescriptor;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.gen5.engine.TestPlanSpecificationVisitor;
import org.junit.gen5.engine.junit5.testers.IsTestClassWithTests;
import org.junit.gen5.engine.junit5.testers.IsTestContext;
import org.junit.gen5.engine.junit5.testers.IsTestMethod;

/**
 * @since 5.0
 */
public class SpecificationResolver {

	private final EngineDescriptor engineDescriptor;

	private final IsTestContext isTestContext = new IsTestContext();
	private final IsTestMethod isTestMethod = new IsTestMethod();
	private final IsTestClassWithTests isTestClassWithTests = new IsTestClassWithTests();

	public SpecificationResolver(EngineDescriptor engineDescriptor) {
		this.engineDescriptor = engineDescriptor;
	}

	public void resolveElement(TestPlanSpecificationElement element) {
		element.accept(new TestPlanSpecificationVisitor() {

			@Override
			public void visitClassSpecification(Class<?> testClass) {
				resolveClassSpecification(testClass);
			}

			@Override
			public void visitMethodSpecification(Class<?> testClass, Method testMethod) {
				resolveMethodSpecification(testClass, testMethod);
			}

			@Override
			public void visitUniqueIdSpecification(String uniqueId) {
				resolveUniqueIdSpecification(uniqueId);
			}

			@Override
			public void visitPackageSpecification(String packageName) {
				findAllClassesInPackage(packageName, isTestClassWithTests).stream().forEach(
					this::visitClassSpecification);
			}

			@Override
			public void visitAllTestsSpecification(File rootDirectory) {
				ReflectionUtils.findAllClassesInClasspathRoot(rootDirectory, isTestClassWithTests).stream().forEach(
					this::visitClassSpecification);
			}
		});
	}

	private void resolveClassSpecification(Class<?> testClass) {
		JUnit5Testable testable = JUnit5Testable.fromClass(testClass, engineDescriptor.getUniqueId());
		resolveTestable(testable);
	}

	private void resolveMethodSpecification(Class<?> testClass, Method testMethod) {
		JUnit5Testable testable = JUnit5Testable.fromMethod(testMethod, testClass, engineDescriptor.getUniqueId());
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
				resolveMethodTestable(method, container, uniqueId);
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

	private void resolveMethodTestable(Method method, Class<?> testClass, String uniqueId) {
		JUnit5Testable parentTestable = JUnit5Testable.fromClass(testClass, engineDescriptor.getUniqueId());
		TestDescriptor newParentDescriptor = resolveAndReturnParentTestable(parentTestable);
		MethodTestDescriptor descriptor = getOrCreateMethodDescriptor(method, uniqueId);
		newParentDescriptor.addChild(descriptor);
	}

	private void resolveClassTestable(Class<?> testClass, String uniqueId, AbstractTestDescriptor parentDescriptor,
			boolean withChildren) {
		ClassTestDescriptor descriptor = getOrCreateClassDescriptor(testClass, uniqueId);
		parentDescriptor.addChild(descriptor);

		if (withChildren) {
			resolveContainedContexts(testClass);
			resolveContainedTestMethods(testClass, descriptor);
		}
	}

	private void resolveContextTestable(String uniqueId, Class<?> testClass, Class<?> containerClass,
			boolean withChildren) {
		JUnit5Testable containerTestable = JUnit5Testable.fromClass(containerClass, engineDescriptor.getUniqueId());
		TestDescriptor parentDescriptor = resolveAndReturnParentTestable(containerTestable);
		ContextTestDescriptor descriptor = getOrCreateContextDescriptor(testClass, uniqueId);
		parentDescriptor.addChild(descriptor);

		if (withChildren) {
			resolveContainedContexts(testClass);
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
		return (MethodTestDescriptor) descriptorByUniqueId(uniqueId).orElse(new MethodTestDescriptor(uniqueId, method));
	}

	private ContextTestDescriptor getOrCreateContextDescriptor(Class<?> clazz, String uniqueId) {
		return (ContextTestDescriptor) descriptorByUniqueId(uniqueId).orElse(
			new ContextTestDescriptor(uniqueId, clazz));
	}

	private ClassTestDescriptor getOrCreateClassDescriptor(Class<?> clazz, String uniqueId) {
		return (ClassTestDescriptor) descriptorByUniqueId(uniqueId).orElse(new ClassTestDescriptor(uniqueId, clazz));
	}

	private Optional<TestDescriptor> descriptorByUniqueId(String uniqueId) {
		return engineDescriptor.findByUniqueId(uniqueId);
	}

}
