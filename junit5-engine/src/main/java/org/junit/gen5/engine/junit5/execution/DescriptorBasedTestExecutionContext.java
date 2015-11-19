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

import static org.junit.gen5.commons.util.AnnotationUtils.findRepeatableAnnotations;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.api.extension.ExtendWith;
import org.junit.gen5.api.extension.MethodParameterResolver;
import org.junit.gen5.api.extension.TestExecutionContext;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.MethodTestDescriptor;

/**
 * @author Sam Brannen
 * @since 5.0
 */
class DescriptorBasedTestExecutionContext implements TestExecutionContext {

	private final Class<?> testClass;

	private final Object testInstance;

	private final Method testMethod;

	private final Map<String, Object> attributes = new HashMap<>();

	private final String displayName;

	private final TestExecutionContext parent;

	private final Set<MethodParameterResolver> resolvers = new HashSet<>();

	DescriptorBasedTestExecutionContext(TestDescriptor descriptor, TestExecutionContext parent, Object testInstance) {

		this.testInstance = testInstance;
		this.displayName = descriptor.getDisplayName();
		this.parent = parent;

		Set<MethodParameterResolver> parentResolvers = (parent != null ? parent.getParameterResolvers()
				: Collections.emptySet());

		if (descriptor instanceof ClassTestDescriptor) {
			// Also handles ContextTestDescriptor which extends ClassTestDescriptor.
			this.testClass = ((ClassTestDescriptor) descriptor).getTestClass();
			this.testMethod = null;
			this.resolvers.addAll(getMethodParameterResolvers(this.testClass, parentResolvers));
		}
		else if (descriptor instanceof MethodTestDescriptor) {
			MethodTestDescriptor methodTestDescriptor = (MethodTestDescriptor) descriptor;
			this.testClass = ((ClassTestDescriptor) methodTestDescriptor.getParent().get()).getTestClass();
			this.testMethod = methodTestDescriptor.getTestMethod();
			this.resolvers.addAll(getMethodParameterResolvers(this.testMethod, parentResolvers));
		}
		else {
			this.testClass = null;
			this.testMethod = null;
		}
	}

	@SuppressWarnings("unchecked")
	private Set<MethodParameterResolver> getMethodParameterResolvers(AnnotatedElement annotatedElement,
			Set<MethodParameterResolver> parentResolvers) {

		// TODO Determine where the MethodParameterResolverRegistry should be created.
		MethodParameterResolverRegistry resolverRegistry = new MethodParameterResolverRegistry(parentResolvers);

		// @formatter:off
		findRepeatableAnnotations(annotatedElement, ExtendWith.class).stream()
				.map(ExtendWith::value)
				.flatMap(Arrays::stream)
				.filter(MethodParameterResolver.class::isAssignableFrom)
				.forEach(clazz -> {
					resolverRegistry.addResolver((Class<? extends MethodParameterResolver>) clazz);
				});
		// @formatter:off

		return resolverRegistry.getResolvers();
	}

	@Override
	public Optional<Class<?>> getTestClass() {
		return Optional.ofNullable(this.testClass);
	}

	@Override
	public Optional<Object> getTestInstance() {
		return Optional.ofNullable(this.testInstance);
	}

	@Override
	public Optional<Method> getTestMethod() {
		return Optional.ofNullable(this.testMethod);
	}

	@Override
	public Map<String, Object> getAttributes() {
		return this.attributes;
	}

	@Override
	public String getDisplayName() {
		return this.displayName;
	}

	@Override
	public Optional<TestExecutionContext> getParent() {
		return Optional.ofNullable(this.parent);
	}

	@Override
	public Set<MethodParameterResolver> getParameterResolvers() {
		return this.resolvers;
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.testClass, this.testMethod, this.testInstance);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}

		if (!(obj instanceof DescriptorBasedTestExecutionContext)) {
			return false;
		}

		DescriptorBasedTestExecutionContext that = (DescriptorBasedTestExecutionContext) obj;

		// @formatter:off
		return Objects.equals(this.testClass, that.testClass)
				&& Objects.equals(this.testMethod, that.testMethod)
				&& Objects.equals(this.testInstance, that.testInstance);
		// @formatter:on
	}

	@Override
	public String toString() {
		return "TestExecutionContext for " + getDisplayName();
	}

}
