/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.discovery;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.lang.reflect.Method;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.DiscoverySelector;

/**
 * A {@link DiscoverySelector} that selects a {@link Method} so that
 * {@link org.junit.platform.engine.TestEngine TestEngines} can discover
 * tests or containers based on Java methods.
 *
 * @since 1.0
 */
@API(Experimental)
public class MethodSelector implements DiscoverySelector {

	/**
	 * Create a {@code MethodSelector} for the supplied class name and method name.
	 *
	 * @param className the fully qualified name of the class in which the method
	 * is declared, or a subclass thereof; never {@code null} or blank
	 * @param methodName the name of the method to select; never {@code null} or blank
	 */
	public static MethodSelector selectMethod(String className, String methodName) {
		Preconditions.notBlank(className, "Class name must not be null or blank");
		Preconditions.notBlank(methodName, "Method name must not be null or blank");
		Class<?> clazz = loadClass(className);
		return selectMethod(clazz, findMethod(clazz, methodName));
	}

	/**
	 * Create a {@code MethodSelector} for the supplied {@link Class} and method name.
	 *
	 * @param clazz the class in which the method is declared, or a subclass thereof;
	 * never {@code null}
	 * @param methodName the name of the method to select; never {@code null} or blank
	 */
	public static MethodSelector selectMethod(Class<?> clazz, String methodName) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notBlank(methodName, "Method name must not be null or blank");
		return selectMethod(clazz, findMethod(clazz, methodName));
	}

	/**
	 * Create a {@code MethodSelector} for the supplied {@link Class} and {@link Method}.
	 *
	 * @param clazz the class in which the method is declared, or a subclass thereof;
	 * never {@code null}
	 * @param method the method to select; never {@code null}
	 */
	public static MethodSelector selectMethod(Class<?> clazz, Method method) {
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

	/**
	 * Get the Java {@link Class} in which the selected {@linkplain #getJavaMethod
	 * method} is declared, or a subclass thereof.
	 *
	 * @see #getJavaMethod()
	 */
	public Class<?> getJavaClass() {
		return this.clazz;
	}

	/**
	 * Get the selected Java {@link Method}.
	 *
	 * @see #getJavaClass()
	 */
	public Method getJavaMethod() {
		return this.method;
	}

	private static Class<?> loadClass(String className) {
		return ReflectionUtils.loadClass(className).orElseThrow(
			() -> new PreconditionViolationException("Could not load class with name: " + className));
	}

	private static Method findMethod(Class<?> clazz, String methodName) {
		return ReflectionUtils.findMethod(clazz, methodName).orElseThrow(() -> new PreconditionViolationException(
			String.format("Could not find method with name [%s] in class [%s].", methodName, clazz.getName())));
	}

}
