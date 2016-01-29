/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.discovery;

import java.lang.reflect.Method;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.commons.util.ToStringBuilder;
import org.junit.gen5.engine.DiscoverySelector;

/**
 * @since 5.0
 */
public class MethodSelector implements DiscoverySelector {

	public static MethodSelector forMethod(String testClassName, String testMethodName) {
		Class<?> testClass = getTestClass(testClassName);
		Method testMethod = getTestMethod(testClass, testMethodName);
		return forMethod(testClass, testMethod);
	}

	public static MethodSelector forMethod(Class<?> testClass, String testMethodName) {
		Method testMethod = getTestMethod(testClass, testMethodName);
		return forMethod(testClass, testMethod);
	}

	public static MethodSelector forMethod(Class<?> testClass, Method testMethod) {
		return new MethodSelector(testClass, testMethod);
	}

	private final Class<?> testClass;
	private final Method testMethod;

	private MethodSelector(Class<?> testClass, Method testMethod) {
		this.testClass = testClass;
		this.testMethod = testMethod;
	}

	public Class<?> getTestClass() {
		return testClass;
	}

	public Method getTestMethod() {
		return testMethod;
	}

	private static Class<?> getTestClass(String testClassName) {
		return ReflectionUtils.loadClass(testClassName).get();
	}

	private static Method getTestMethod(Class<?> testClass, String testMethodName) {
		return ReflectionUtils.findMethod(testClass, testMethodName).get();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		MethodSelector that = (MethodSelector) o;
		if (!testClass.equals(that.testClass))
			return false;
		return testMethod.equals(that.testMethod);
	}

	@Override
	public int hashCode() {
		int result = testClass.hashCode();
		result = 31 * result + testMethod.hashCode();
		return result;
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("testClass", testClass)
				.append("testMethod", testMethod)
				.toString();
		// @formatter:on
	}
}
