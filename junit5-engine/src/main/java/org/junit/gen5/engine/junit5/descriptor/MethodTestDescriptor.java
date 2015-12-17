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

import static org.junit.gen5.engine.junit5.descriptor.MethodContextImpl.methodContext;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.gen5.api.extension.AfterEachExtensionPoint;
import org.junit.gen5.api.extension.BeforeEachExtensionPoint;
import org.junit.gen5.api.extension.MethodContext;
import org.junit.gen5.api.extension.TestExtensionContext;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.JavaSource;
import org.junit.gen5.engine.Leaf;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestTag;
import org.junit.gen5.engine.junit5.execution.JUnit5EngineExecutionContext;
import org.junit.gen5.engine.junit5.execution.MethodInvoker;
import org.junit.gen5.engine.junit5.execution.RegisteredExtensionPoint;
import org.junit.gen5.engine.junit5.execution.TestExtensionRegistry;
import org.junit.gen5.engine.junit5.execution.ThrowingConsumer;

/**
 * {@link TestDescriptor} for tests based on Java methods.
 *
 * @since 5.0
 */
public class MethodTestDescriptor extends JUnit5TestDescriptor implements Leaf<JUnit5EngineExecutionContext> {

	private final String displayName;

	private final Class<?> testClass;
	private final Method testMethod;

	MethodTestDescriptor(String uniqueId, Class<?> testClass, Method testMethod) {
		super(uniqueId);
		this.testClass = testClass;

		Preconditions.notNull(testMethod, "testMethod must not be null");

		this.testMethod = testMethod;
		this.displayName = determineDisplayName(testMethod, testMethod.getName());

		setSource(new JavaSource(testMethod));
	}

	@Override
	public final Set<TestTag> getTags() {
		Set<TestTag> methodTags = getTags(getTestMethod());
		getParent().ifPresent(parentDescriptor -> methodTags.addAll(parentDescriptor.getTags()));
		return methodTags;
	}

	@Override
	public final String getDisplayName() {
		return this.displayName;
	}

	public Class<?> getTestClass() {
		return testClass;
	}

	public final Method getTestMethod() {
		return this.testMethod;
	}

	@Override
	public final boolean isTest() {
		return true;
	}

	@Override
	public boolean isContainer() {
		return false;
	}

	@Override
	public JUnit5EngineExecutionContext execute(JUnit5EngineExecutionContext context) throws Throwable {

		TestExtensionRegistry newTestExtensionRegistry = populateNewTestExtensionRegistryFromExtendWith(testMethod,
			context.getTestExtensionRegistry());

		JUnit5EngineExecutionContext newContext = context.extend().withTestExtensionRegistry(
			newTestExtensionRegistry).build();

		Object testInstance = context.getTestInstanceProvider().getTestInstance();
		TestExtensionContext testExtensionContext = new MethodBasedTestExtensionContext(context.getExtensionContext(),
			this, testInstance);

		invokeBeforeEachExtensionPoints(newTestExtensionRegistry, testExtensionContext);

		List<Throwable> throwablesCollector = new LinkedList<>();
		invokeTestMethod(testExtensionContext, newTestExtensionRegistry, throwablesCollector);

		invokeAfterEachExtensionPoints(newTestExtensionRegistry, testExtensionContext, throwablesCollector);

		if (!throwablesCollector.isEmpty()) {
			Throwable t = throwablesCollector.get(0);
			throwablesCollector.stream().skip(1).forEach(t::addSuppressed);
			throw t;
		}

		return newContext;
	}

	protected void invokeAfterEachExtensionPoints(TestExtensionRegistry newTestExtensionRegistry,
			TestExtensionContext testExtensionContext, List<Throwable> throwables) throws Throwable {
		ThrowingConsumer<RegisteredExtensionPoint<AfterEachExtensionPoint>> applyAfterEach = registeredExtensionPoint -> {
			try {
				registeredExtensionPoint.getExtensionPoint().afterEach(testExtensionContext);
			}
			catch (Throwable t) {
				throwables.add(t);
			}
		};
		newTestExtensionRegistry.applyExtensionPoints(AfterEachExtensionPoint.class,
			TestExtensionRegistry.ApplicationOrder.BACKWARD, applyAfterEach);
	}

	protected void invokeBeforeEachExtensionPoints(TestExtensionRegistry newTestExtensionRegistry,
			TestExtensionContext testExtensionContext) throws Throwable {
		ThrowingConsumer<RegisteredExtensionPoint<BeforeEachExtensionPoint>> applyBeforeEach = registeredExtensionPoint -> {
			try {
				registeredExtensionPoint.getExtensionPoint().beforeEach(testExtensionContext);
			}
			catch (Exception e) { //TODO: Non RTEs should be allowed
				throw new RuntimeException(e);
			}
		};
		newTestExtensionRegistry.applyExtensionPoints(BeforeEachExtensionPoint.class,
			TestExtensionRegistry.ApplicationOrder.FORWARD, applyBeforeEach);
	}

	private void invokeTestMethod(TestExtensionContext testExtensionContext,
			TestExtensionRegistry testExtensionRegistry, List<Throwable> throwablesCollector) {
		try {
			MethodContext methodContext = methodContext(testExtensionContext.getTestInstance(),
				testExtensionContext.getTestMethod());
			new MethodInvoker(testExtensionContext, testExtensionRegistry).invoke(methodContext);
		}
		catch (Throwable t) {
			throwablesCollector.add(t);
		}
	}

}
