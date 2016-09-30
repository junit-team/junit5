/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.discovery;

import org.junit.jupiter.engine.descriptor.ClassTestDescriptor;
import org.junit.jupiter.engine.descriptor.MethodTestDescriptor;
import org.junit.jupiter.engine.descriptor.TestFactoryExtensionMethodTestDescriptor;
import org.junit.jupiter.engine.discovery.predicates.IsTestFactoryExtensionContainer;
import org.junit.jupiter.engine.discovery.predicates.IsTestFactoryExtensionMethod;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.FunctionUtils;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.UniqueId.Segment;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

/**
 * @since 5.0
 */
@API(Experimental)
class TestFactoryExtensionResolver implements ElementResolver {

	static final String CONTAINER_SEGMENT_TYPE = "test-factory-container";
	static final String METHOD_SEGMENT_TYPE = "test-factory-method";

	private static final IsTestFactoryExtensionMethod IS_TEST_FACTORY_METHOD = new IsTestFactoryExtensionMethod();
	private static final IsTestFactoryExtensionContainer IS_TEST_FACTORY_CONTAINER = new IsTestFactoryExtensionContainer();

	private static final MethodFinder METHOD_FINDER = new MethodFinder();

	@Override
	public Set<TestDescriptor> resolveElement(AnnotatedElement element, TestDescriptor parent) {
		if (element instanceof Class)
			return resolveTestFactoryContainer((Class<?>) element, parent);

		if (element instanceof Method)
			return resolveTestFactoryMethod((Method) element, parent);

		return Collections.emptySet();
	}

	private Set<TestDescriptor> resolveTestFactoryContainer(Class<?> container, TestDescriptor parent) {
		if (!isTestFactoryContainer(container))
			return Collections.emptySet();

		UniqueId uniqueId = createUniqueContainerId(container, parent);
		return Collections.singleton(resolveContainer(container, uniqueId));
	}

	protected boolean isTestFactoryContainer(Class<?> candidate) {
		return IS_TEST_FACTORY_CONTAINER.test(candidate);
	}

	protected UniqueId createUniqueContainerId(Class<?> testFactoryContainer, TestDescriptor parent) {
		return parent.getUniqueId().append(CONTAINER_SEGMENT_TYPE, testFactoryContainer.getName());
	}

	protected TestDescriptor resolveContainer(Class<?> testClass, UniqueId uniqueId) {
		// TODO correct descriptor
		throw new RuntimeException("Not yet implemented");
	}

	private Set<TestDescriptor> resolveTestFactoryMethod(Method method, TestDescriptor parent) {
		if (!isTestFactoryMethod(method))
			return Collections.emptySet();

		UniqueId uniqueId = createUniqueMethodId(method, parent);
		return Collections.singleton(resolveMethod(method, (ClassTestDescriptor) parent, uniqueId));
	}

	protected boolean isTestFactoryMethod(Method candidate) {
		return IS_TEST_FACTORY_METHOD.test(candidate);
	}

	protected UniqueId createUniqueMethodId(Method testFactoryMethod, TestDescriptor parent) {
		String methodId = String.format("%s(%s)", testFactoryMethod.getName(),
				StringUtils.nullSafeToString(testFactoryMethod.getParameterTypes()));
		return parent.getUniqueId().append(METHOD_SEGMENT_TYPE, methodId);
	}

	protected TestDescriptor resolveMethod(Method testMethod, ClassTestDescriptor parentClassDescriptor,
			UniqueId uniqueId) {
		return new TestFactoryExtensionMethodTestDescriptor(uniqueId, parentClassDescriptor.getTestClass(), testMethod);
	}

	@Override
	public Optional<TestDescriptor> resolveUniqueId(UniqueId.Segment segment, TestDescriptor parent) {
		return FunctionUtils.firstPresent(
				() -> resolveUniqueContainerId(segment, parent),
				() -> resolveUniqueMethodId(segment, parent)
		);
	}

	private Optional<TestDescriptor> resolveUniqueContainerId(Segment segment, TestDescriptor parent) {
		if (!segment.getType().equals(CONTAINER_SEGMENT_TYPE))
			return Optional.empty();

		if (!requiredContainerParentType().isInstance(parent))
			return Optional.empty();

		Optional<Class<?>> optionalContainerClass = findContainer(segment, parent);
		if (!optionalContainerClass.isPresent())
			return Optional.empty();

		Class<?> container = optionalContainerClass.get();
		if (!isTestFactoryContainer(container))
			return Optional.empty();

		UniqueId uniqueId = createUniqueContainerId(container, parent);
		return Optional.of(resolveContainer(container, uniqueId));
	}

	protected Class<?> requiredContainerParentType() {
		return TestDescriptor.class;
	}

	protected Optional<Class<?>> findContainer(Segment segment, TestDescriptor parent) {
		String containerName = segment.getValue();
		return ReflectionUtils.loadClass(containerName);
	}

	private Optional<TestDescriptor> resolveUniqueMethodId(UniqueId.Segment segment, TestDescriptor parent) {
		if (!segment.getType().equals(METHOD_SEGMENT_TYPE))
			return Optional.empty();

		if (!requiredMethodParentType().isInstance(parent))
			return Optional.empty();

		Optional<Method> optionalMethod = findMethod(segment, (ClassTestDescriptor) parent);
		if (!optionalMethod.isPresent())
			return Optional.empty();

		Method testMethod = optionalMethod.get();
		if (!isTestFactoryMethod(testMethod))
			return Optional.empty();

		UniqueId uniqueId = createUniqueMethodId(testMethod, parent);
		return Optional.of(resolveMethod(testMethod, (ClassTestDescriptor) parent, uniqueId));
	}

	protected Class<?> requiredMethodParentType() {
		return ClassTestDescriptor.class;
	}

	private Optional<Method> findMethod(UniqueId.Segment segment, ClassTestDescriptor parent) {
		return METHOD_FINDER.findMethod(segment.getValue(), parent.getTestClass());
	}

}
