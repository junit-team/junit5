/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import static org.junit.gen5.commons.util.AnnotationUtils.findAnnotatedMethods;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.commons.util.ReflectionUtils.MethodSortOrder;
import org.junit.gen5.engine.AbstractTestDescriptor;
import org.junit.gen5.engine.Child;

public class JUnit5MethodDescriptor extends AbstractTestDescriptor implements Child<JUnit5Context> {

	private final Class<?> testClass;
	private final Method method;

	public JUnit5MethodDescriptor(String parentId, Class<?> testClass, Method method) {
		super(parentId + "#" + method.getName());
		this.testClass = testClass;
		this.method = method;
	}

	@Override
	public String getDisplayName() {
		return method.getName();
	}

	@Override
	public boolean isTest() {
		return true;
	}

	@Override
	public boolean isContainer() {
		return false;
	}

	@Override
	public JUnit5Context execute(JUnit5Context context) throws Throwable {
		TestInstanceProvider provider = context.getTestInstanceProvider();
		Object testInstance = provider.getTestInstance();
		for (Method method : findAnnotatedMethods(testClass, BeforeEach.class, MethodSortOrder.HierarchyDown)) {
			ReflectionUtils.invokeMethod(method, testInstance);
		}
		List<Throwable> throwables = new LinkedList<>();
		try {
			ReflectionUtils.invokeMethod(method, testInstance);
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
			return context;
		}
		Throwable t = throwables.get(0);
		throwables.stream().skip(1).forEach(t::addSuppressed);
		throw t;
	}

}
