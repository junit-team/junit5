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
import java.util.Set;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.commons.util.ReflectionUtils.MethodSortOrder;
import org.junit.gen5.engine.JavaSource;
import org.junit.gen5.engine.Parent;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestTag;
import org.junit.gen5.engine.junit5.BeforeEachCallback;
import org.junit.gen5.engine.junit5.JUnit5Context;
import org.junit.gen5.engine.junit5.TestInstanceProvider;

/**
 * {@link TestDescriptor} for tests based on Java classes.
 *
 * <p>The pattern of the {@link #getUniqueId unique ID} takes the form of
 * <code>{parent unique id}:{fully qualified class name}</code>.
 *
 * @since 5.0
 */
public class ClassTestDescriptor extends JUnit5TestDescriptor implements Parent<JUnit5Context> {

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
	public JUnit5Context beforeAll(JUnit5Context context) {
		// @formatter:off
		return context
				.withTestInstanceProvider(testInstanceProvider(context))
				.withBeforeEachCallback(beforeEachCallback(context))
				.withTestExtensionRegistry(populateNewTestExtensionRegistryFromExtendWith(testClass, context.getTestExtensionRegistry()));
		// @formatter:on
	}

	protected TestInstanceProvider testInstanceProvider(JUnit5Context context) {
		return () -> ReflectionUtils.newInstance(testClass);
	}

	protected BeforeEachCallback beforeEachCallback(JUnit5Context context) {
		return testInstance -> {
			for (Method method : findAnnotatedMethods(testClass, BeforeEach.class, MethodSortOrder.HierarchyDown)) {
				ReflectionUtils.invokeMethod(method, testInstance);
			}
		};
	}

}
