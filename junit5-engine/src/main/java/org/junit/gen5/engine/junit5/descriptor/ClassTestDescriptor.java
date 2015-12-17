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

import static org.junit.gen5.commons.util.AnnotationUtils.findAnnotatedMethods;
import static org.junit.gen5.engine.junit5.descriptor.MethodContextImpl.methodContext;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.extension.AfterEachExtensionPoint;
import org.junit.gen5.api.extension.BeforeEachExtensionPoint;
import org.junit.gen5.api.extension.ExtensionPoint;
import org.junit.gen5.api.extension.TestExtensionContext;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.commons.util.ReflectionUtils.MethodSortOrder;
import org.junit.gen5.engine.Container;
import org.junit.gen5.engine.JavaSource;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestTag;
import org.junit.gen5.engine.junit5.execution.JUnit5EngineExecutionContext;
import org.junit.gen5.engine.junit5.execution.MethodInvoker;
import org.junit.gen5.engine.junit5.execution.TestExtensionRegistry;
import org.junit.gen5.engine.junit5.execution.TestInstanceProvider;

/**
 * {@link TestDescriptor} for tests based on Java classes.
 * <p>
 * The pattern of the {@link #getUniqueId unique ID} takes the form of
 * <code>{parent unique id}:{fully qualified class name}</code>.
 *
 * @since 5.0
 */
public class ClassTestDescriptor extends JUnit5TestDescriptor implements Container<JUnit5EngineExecutionContext> {

	private final String displayName;

	private final Class<?> testClass;

	ClassTestDescriptor(String uniqueId, Class<?> testClass) {
		super(uniqueId);

		Preconditions.notNull(testClass, "testClass must not be null");

		this.testClass = testClass;
		this.displayName = determineDisplayName(testClass, testClass.getName());

		setSource(new JavaSource(testClass));
	}

	public final Class<?> getTestClass() {
		return this.testClass;
	}

	@Override
	public final String getDisplayName() {
		return this.displayName;
	}

	@Override
	public final Set<TestTag> getTags() {
		return getTags(this.testClass);
	}

	@Override
	public final boolean isTest() {
		return false;
	}

	@Override
	public boolean isContainer() {
		return true;
	}

	@Override
	public JUnit5EngineExecutionContext beforeAll(JUnit5EngineExecutionContext context) {
		TestExtensionRegistry newExtensionRegistry = populateNewTestExtensionRegistryFromExtendWith(testClass,
			context.getTestExtensionRegistry());
		registerBeforeEachMethods(newExtensionRegistry);
		registerAfterEachMethods(newExtensionRegistry);

		context = context.extend().withTestExtensionRegistry(newExtensionRegistry).build();

		// @formatter:off
		return context.extend()
				.withTestInstanceProvider(testInstanceProvider(context))
				.withExtensionContext(new ClassBasedContainerExtensionContext(context.getExtensionContext(), this))
				.build();
		// @formatter:on
	}

	protected TestInstanceProvider testInstanceProvider(JUnit5EngineExecutionContext context) {
		return () -> ReflectionUtils.newInstance(testClass);
	}

	private void registerAfterEachMethods(TestExtensionRegistry extensionRegistry) {
		List<Method> afterEachMethods = findAnnotatedMethods(testClass, AfterEach.class, MethodSortOrder.HierarchyDown);
		afterEachMethods.stream().forEach(method -> {
			AfterEachExtensionPoint extensionPoint = testExtensionContext -> {
				runMethodInExtensionContext(method, testExtensionContext, extensionRegistry);
			};
			extensionRegistry.registerExtension(extensionPoint, ExtensionPoint.Position.DEFAULT, method.getName());
		});
	}

	private void registerBeforeEachMethods(TestExtensionRegistry extensionRegistry) {
		List<Method> beforeEachMethods = findAnnotatedMethods(testClass, BeforeEach.class,
			MethodSortOrder.HierarchyDown);
		beforeEachMethods.stream().forEach(method -> {
			BeforeEachExtensionPoint extensionPoint = testExtensionContext -> {
				runMethodInExtensionContext(method, testExtensionContext, extensionRegistry);
			};
			extensionRegistry.registerExtension(extensionPoint, ExtensionPoint.Position.DEFAULT, method.getName());
		});
	}

	private void runMethodInExtensionContext(Method method, TestExtensionContext testExtensionContext,
			TestExtensionRegistry extensionRegistry) {
		Optional<Object> optionalInstance = ReflectionUtils.getOuterInstance(testExtensionContext.getTestInstance(),
			method.getDeclaringClass());
		optionalInstance.ifPresent(instance -> new MethodInvoker(testExtensionContext, extensionRegistry).invoke(
			methodContext(instance, method)));
	}

}
