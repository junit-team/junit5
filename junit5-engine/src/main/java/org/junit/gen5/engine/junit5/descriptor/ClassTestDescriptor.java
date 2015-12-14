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
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.extension.AfterEachExtensionPoint;
import org.junit.gen5.api.extension.BeforeEachExtensionPoint;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.commons.util.ReflectionUtils.MethodSortOrder;
import org.junit.gen5.engine.Container;
import org.junit.gen5.engine.JavaSource;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestTag;
import org.junit.gen5.engine.junit5.execution.AfterEachCallback;
import org.junit.gen5.engine.junit5.execution.BeforeEachCallback;
import org.junit.gen5.engine.junit5.execution.JUnit5EngineExecutionContext;
import org.junit.gen5.engine.junit5.execution.MethodInvoker;
import org.junit.gen5.engine.junit5.execution.RegisteredExtensionPoint;
import org.junit.gen5.engine.junit5.execution.TestExtensionRegistry;
import org.junit.gen5.engine.junit5.execution.TestInstanceProvider;
import org.junit.gen5.engine.junit5.execution.ThrowingConsumer;

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
		context = context.extend().withTestExtensionRegistry(
			populateNewTestExtensionRegistryFromExtendWith(testClass, context.getTestExtensionRegistry())).build();
		// @formatter:off
		return context.extend()
				.withTestInstanceProvider(testInstanceProvider(context))
				.withBeforeEachCallback(beforeEachCallback(context))
				.withAfterEachCallback(afterEachCallback(context))
				.withExtensionContext(new ClassBasedContainerExtensionContext(context.getExtensionContext(), this))
				.build();
		// @formatter:on
	}

	protected TestInstanceProvider testInstanceProvider(JUnit5EngineExecutionContext context) {
		return () -> ReflectionUtils.newInstance(testClass);
	}

	protected BeforeEachCallback beforeEachCallback(JUnit5EngineExecutionContext context) {
		List<Method> beforeEachMethods = findAnnotatedMethods(testClass, BeforeEach.class,
			MethodSortOrder.HierarchyDown);
		return (testExtensionContext, testInstance) -> {
			ThrowingConsumer<RegisteredExtensionPoint<BeforeEachExtensionPoint>> applyBeforeEach = registeredExtensionPoint -> {
				try {
					registeredExtensionPoint.getExtensionPoint().beforeEach(testExtensionContext);
				}
				catch (Exception e) { //TODO: Non RTEs should be allowed
					throw new RuntimeException(e);
				}
			};
			TestExtensionRegistry extensionRegistry = context.getTestExtensionRegistry();
			extensionRegistry.applyExtensionPoints(BeforeEachExtensionPoint.class,
				TestExtensionRegistry.ApplicationOrder.FORWARD, applyBeforeEach);

			//TODO: Register beforeEachMethods as extension points to enable correct sorting
			for (Method method : beforeEachMethods) {
				new MethodInvoker(testExtensionContext, extensionRegistry).invoke(methodContext(testInstance, method));
			}
		};
	}

	protected AfterEachCallback afterEachCallback(JUnit5EngineExecutionContext context) {
		List<Method> afterEachMethods = findAnnotatedMethods(testClass, AfterEach.class, MethodSortOrder.HierarchyUp);
		return (testExtensionContext, testInstance, throwable) -> {
			TestExtensionRegistry extensionRegistry = context.getTestExtensionRegistry();
			List<Throwable> throwables = new LinkedList<>();
			throwable.ifPresent(throwables::add);

			//TODO: Register afterEachMethods as extension points to enable correct sorting
			for (Method method : afterEachMethods) {
				try {
					new MethodInvoker(testExtensionContext, extensionRegistry).invoke(
						methodContext(testInstance, method));
				}
				catch (Throwable t) {
					throwables.add(t);
				}
			}

			ThrowingConsumer<RegisteredExtensionPoint<AfterEachExtensionPoint>> applyAfterEach = registeredExtensionPoint -> {
				try {
					registeredExtensionPoint.getExtensionPoint().afterEach(testExtensionContext);
				}
				catch (Exception e) {
					throwables.add(e);
				}
			};
			extensionRegistry.applyExtensionPoints(AfterEachExtensionPoint.class,
				TestExtensionRegistry.ApplicationOrder.BACKWARD, applyAfterEach);

			if (!throwables.isEmpty()) {
				Throwable t = throwables.get(0);
				throwables.stream().skip(1).forEach(t::addSuppressed);
				throw t;
			}
		};
	}

}
