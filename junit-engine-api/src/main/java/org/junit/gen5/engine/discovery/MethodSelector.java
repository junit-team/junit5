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

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.lang.reflect.Method;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.DiscoverySelector;

/**
 * @since 5.0
 */
@API(Experimental)
public class MethodSelector implements DiscoverySelector {

	public static MethodSelector forMethod(String className, String methodName) {
		Preconditions.notBlank(className, "Class name must not be null or empty");
		Preconditions.notBlank(methodName, "Method name must not be null or empty");
		return forMethod(getTestClass(className), getTestMethod(getTestClass(className), methodName));
	}

	public static MethodSelector forMethod(Class<?> clazz, String methodName) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notBlank(methodName, "Method name must not be null or empty");
		return forMethod(clazz, getTestMethod(clazz, methodName));
	}

	public static MethodSelector forMethod(Class<?> clazz, Method method) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(method, "Method must not be null");
		return new MethodSelector(clazz, method);
	}

	private final Class<?> clazz;
	private final Method method;

	private MethodSelector(Class<?> clazz, Method method) {
		this.clazz = clazz;
		this.method = method;
	}

	public Class<?> getTestClass() {
		return this.clazz;
	}

	public Method getTestMethod() {
		return this.method;
	}

	private static Class<?> getTestClass(String clazzName) {
		return ReflectionUtils.loadClass(clazzName).get();
	}

	private static Method getTestMethod(Class<?> clazz, String methodName) {
		return ReflectionUtils.findMethod(clazz, methodName).get();
	}

}
