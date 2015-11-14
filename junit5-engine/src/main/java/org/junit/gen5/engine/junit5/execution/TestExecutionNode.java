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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.api.extension.MethodArgumentResolver;
import org.junit.gen5.api.extension.TestDecorator;
import org.junit.gen5.api.extension.TestDecorators;
import org.junit.gen5.api.extension.TestExecutionContext;
import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.MethodTestDescriptor;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
public abstract class TestExecutionNode {

	private TestExecutionNode parent;

	private List<TestExecutionNode> children = new LinkedList<>();

	public void addChild(TestExecutionNode child) {
		this.children.add(child);
		child.parent = this;
	}

	public final TestExecutionNode getParent() {
		return this.parent;
	}

	public final List<TestExecutionNode> getChildren() {
		return Collections.unmodifiableList(this.children);
	}

	public abstract TestDescriptor getTestDescriptor();

	public abstract void execute(ExecutionRequest request, TestExecutionContext context);

	protected void executeChild(TestExecutionNode child, ExecutionRequest request, TestExecutionContext parentContext,
			Object testInstance) {

		TestExecutionContext childContext = createChildContext(child, parentContext, testInstance);
		child.execute(request, childContext);

	}

	private TestExecutionContext createChildContext(TestExecutionNode child, TestExecutionContext parentContext,
			Object testInstance) {
		Class<?> testClass = null;
		Method testMethod = null;

		if (child.getTestDescriptor() instanceof ClassTestDescriptor) {
			//also handles ContextTestDescriptor which is subclass of CTD
			testClass = ((ClassTestDescriptor) child.getTestDescriptor()).getTestClass();
		}

		if (child.getTestDescriptor() instanceof MethodTestDescriptor) {
			MethodTestDescriptor methodTestDescriptor = (MethodTestDescriptor) child.getTestDescriptor();
			testMethod = methodTestDescriptor.getTestMethod();
			testClass = ((ClassTestDescriptor) methodTestDescriptor.getParent().get()).getTestClass();
		}
		return createContext(child.getTestDescriptor(), parentContext, testInstance, testMethod, testClass);
	}

	protected TestExecutionContext createContext(TestDescriptor descriptor, TestExecutionContext parent,
			Object testInstance, Method testMethod, Class testClass) {

		final Set<MethodArgumentResolver> parentResolvers = parent != null ? parent.getResolvers()
				: Collections.emptySet();
		final Set<MethodArgumentResolver> resolvers = testClass != null
				? new HashSet<>(getMethodArgumentResolvers(testClass, parentResolvers)) : new HashSet<>();

		return new TestExecutionContext() {

			private final Map<String, Object> attributes = new HashMap<>();

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
		};
	}

	private Set<MethodArgumentResolver> getMethodArgumentResolvers(Class<?> testClass,
			Set<MethodArgumentResolver> parentResolvers) {
		// TODO Determine where the MethodArgumentResolverRegistry should be created.
		MethodArgumentResolverRegistry resolverRegistry = new MethodArgumentResolverRegistry(parentResolvers);
		findAnnotation(testClass, TestDecorators.class).map(TestDecorators::value).ifPresent(clazzes -> {
			for (Class<? extends TestDecorator> clazz : clazzes) {
				resolverRegistry.addResolverWithClass((Class<? extends MethodArgumentResolver>) clazz);
			}
		});

		return resolverRegistry.getResolvers();
	}

}
