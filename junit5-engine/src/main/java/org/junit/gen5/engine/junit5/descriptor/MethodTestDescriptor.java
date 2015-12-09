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
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.api.extension.MethodContext;
import org.junit.gen5.api.extension.TestExtensionContext;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.Child;
import org.junit.gen5.engine.JavaSource;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestTag;
import org.junit.gen5.engine.junit5.execution.JUnit5Context;
import org.junit.gen5.engine.junit5.execution.MethodInvoker;
import org.junit.gen5.engine.junit5.execution.TestExtensionRegistry;

/**
 * {@link TestDescriptor} for tests based on Java methods.
 *
 * @since 5.0
 */
public class MethodTestDescriptor extends JUnit5TestDescriptor implements Child<JUnit5Context> {

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
	public JUnit5Context execute(JUnit5Context context) throws Throwable {
		JUnit5Context newContext = context.extend().withTestExtensionRegistry(
			populateNewTestExtensionRegistryFromExtendWith(testMethod, context.getTestExtensionRegistry())).build();

		Object testInstance = context.getTestInstanceProvider().getTestInstance();
		TestExtensionContext testExtensionContext = new MethodBasedTestExtensionContext(this, testInstance);

		context.getBeforeEachCallback().beforeEach(testExtensionContext, testInstance);

		Optional<Throwable> throwable = invokeTestMethod(testExtensionContext, newContext.getTestExtensionRegistry());

		context.getAfterEachCallback().afterEach(testExtensionContext, testInstance, throwable);

		return newContext;
	}

	private Optional<Throwable> invokeTestMethod(TestExtensionContext testExtensionContext,
			TestExtensionRegistry testExtensionRegistry) {
		try {
			MethodContext methodContext = methodContext(testExtensionContext.getTestInstance(),
				testExtensionContext.getTestMethod());
			new MethodInvoker(methodContext, testExtensionContext, testExtensionRegistry).invoke();
			return Optional.empty();
		}
		catch (Throwable t) {
			return Optional.of(t);
		}
	}

}
