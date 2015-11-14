/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution;

import static org.junit.gen5.commons.util.AnnotationUtils.findAnnotation;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.api.extension.MethodArgumentResolver;
import org.junit.gen5.api.extension.TestDecorator;
import org.junit.gen5.api.extension.TestDecorators;
import org.junit.gen5.api.extension.TestExecutionContext;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.MethodTestDescriptor;

class DescriptorBasedTestExecutionContext implements TestExecutionContext {

	private final TestDescriptor descriptor;
	private final TestExecutionContext parent;
	private final Object testInstance;
	private Class<?> testClass = null;
	private Method testMethod = null;

	private final Map<String, Object> attributes = new HashMap<>();
	private final Set<MethodArgumentResolver> resolvers;

	DescriptorBasedTestExecutionContext(TestDescriptor descriptor, TestExecutionContext parent, Object testInstance) {

		this.descriptor = descriptor;
		this.parent = parent;
		this.testInstance = testInstance;

		if (descriptor instanceof ClassTestDescriptor) {
			//also handles ContextTestDescriptor which is subclass of CTD
			testClass = ((ClassTestDescriptor) descriptor).getTestClass();
		}
		else if (descriptor instanceof MethodTestDescriptor) {
			MethodTestDescriptor methodTestDescriptor = (MethodTestDescriptor) descriptor;
			testMethod = methodTestDescriptor.getTestMethod();
			testClass = ((ClassTestDescriptor) methodTestDescriptor.getParent().get()).getTestClass();
		}

		final Set<MethodArgumentResolver> parentResolvers = parent != null ? parent.getResolvers()
				: Collections.emptySet();
		resolvers = testClass != null ? new HashSet<>(getMethodArgumentResolvers(testClass, parentResolvers))
				: new HashSet<>();

	}

	@SuppressWarnings("unchecked")
	private Set<MethodArgumentResolver> getMethodArgumentResolvers(Class<?> testClass,
			Set<MethodArgumentResolver> parentResolvers) {
		// TODO Determine where the MethodArgumentResolverRegistry should be created.
		MethodArgumentResolverRegistry resolverRegistry = new MethodArgumentResolverRegistry(parentResolvers);
		findAnnotation(testClass, TestDecorators.class).map(TestDecorators::value).ifPresent(clazzes -> {
			for (Class<? extends TestDecorator> clazz : clazzes) {
				if (MethodArgumentResolver.class.isAssignableFrom(clazz)) {
					resolverRegistry.addResolverWithClass((Class<? extends MethodArgumentResolver>) clazz);
				}
			}
		});

		return resolverRegistry.getResolvers();
	}

	@Override
	public String getDisplayName() {
		return descriptor.getDisplayName();
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public Optional<TestExecutionContext> getParent() {
		return Optional.ofNullable(parent);
	}

	@Override
	public Optional<Object> getTestInstance() {
		return Optional.ofNullable(testInstance);
	}

	@Override
	public Optional<Method> getTestMethod() {
		return Optional.ofNullable(testMethod);
	}

	@Override
	public Optional<Class<?>> getTestClass() {
		return Optional.ofNullable(testClass);
	}

	@Override
	public Set<MethodArgumentResolver> getResolvers() {
		return resolvers;
	}
}
