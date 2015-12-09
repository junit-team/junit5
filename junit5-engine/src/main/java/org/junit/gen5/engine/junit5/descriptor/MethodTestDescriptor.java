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

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.extension.MethodContext;
import org.junit.gen5.api.extension.TestExtensionContext;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.commons.util.ReflectionUtils.MethodSortOrder;
import org.junit.gen5.engine.Child;
import org.junit.gen5.engine.JavaSource;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestTag;
import org.junit.gen5.engine.junit5.JUnit5Context;
import org.junit.gen5.engine.junit5.TestInstanceProvider;
import org.junit.gen5.engine.junit5.execution.MethodInvoker;

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
		JUnit5Context myContext = context.extend().withTestExtensionRegistry(
			populateNewTestExtensionRegistryFromExtendWith(testMethod, context.getTestExtensionRegistry())).build();

		TestInstanceProvider provider = context.getTestInstanceProvider();
		Object testInstance = provider.getTestInstance();
		TestExtensionContext testExtensionContext = new MethodBasedTestExtensionContext(this, testInstance);

		context.getBeforeEachCallback().beforeEach(testExtensionContext, testInstance);

		List<Throwable> throwables = new LinkedList<>();
		try {
			MethodContext methodContext = new MethodContextImpl(testInstance, testMethod);
			new MethodInvoker(methodContext, testExtensionContext, myContext.getTestExtensionRegistry()).invoke();
		}
		catch (Throwable t) {
			throwables.add(t);
		}
		finally {
			for (Method method : findAnnotatedMethods(testClass, AfterEach.class, MethodSortOrder.HierarchyUp)) {
				try {
					ReflectionUtils.invokeMethod(method, testInstance);
				}
				catch (Throwable t) {
					throwables.add(t);
				}
			}
		}
		if (throwables.isEmpty()) {
			return myContext;
		}
		Throwable t = throwables.get(0);
		throwables.stream().skip(1).forEach(t::addSuppressed);
		throw t;
	}

}
