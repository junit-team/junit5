/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.support.descriptor;

import static org.junit.platform.commons.meta.API.Usage.Experimental;
import static org.junit.platform.commons.util.StringUtils.nullSafeToString;

import java.lang.reflect.Method;
import java.util.Objects;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ToStringBuilder;

/**
 * Java method based {@link org.junit.platform.engine.TestSource}.
 *
 * <p>This class stores the method name along with its parameter types because
 * {@link Method} does not implement {@link java.io.Serializable}.
 *
 * @since 1.0
 * @see org.junit.platform.engine.discovery.JavaMethodSelector
 */
@API(Experimental)
public class JavaMethodSource implements JavaSource {

	private static final long serialVersionUID = 1L;

	private final String className;
	private final String methodName;
	private final String methodParameterTypes;

	/**
	 * Create a new {@code JavaMethodSource} using the supplied
	 * class and method name.
	 *
	 * @param className the {@link Class} name; must not be {@code null} or blank
	 * @param methodName the {@link Method} name; must not be {@code null} or blank
	 */
	public JavaMethodSource(String className, String methodName) {
		this(className, methodName, null);
	}

	/**
	 * Create a new {@code JavaMethodSource} using the supplied
	 * class and method name.
	 *
	 * @param className the {@link Class} name; must not be {@code null} or blank
	 * @param methodName the {@link Method} name; must not be {@code null} or blank
	 * @param methodParameterTypes the {@link Method} parameter types as string
	 */
	public JavaMethodSource(String className, String methodName, String methodParameterTypes) {
		Preconditions.notBlank(className, "Class name must not be null or blank");
		Preconditions.notBlank(methodName, "Method name must not be null or blank");
		this.className = className;
		this.methodName = methodName;
		this.methodParameterTypes = null;
	}

	/**
	 * Create a new {@code JavaMethodSource} using the supplied
	 * {@link Method method}.
	 *
	 * @param method the Java method; must not be {@code null}
	 */
	public JavaMethodSource(Method method) {
		Preconditions.notNull(method, "method must not be null");
		this.className = method.getDeclaringClass().getName();
		this.methodName = method.getName();
		this.methodParameterTypes = nullSafeToString(method.getParameterTypes());
	}

	/**
	 * Get the declaring {@link Class} name of this source.
	 */
	public String getClassName() {
		return this.className;
	}

	/**
	 * Get the {@link Method} name of this source.
	 */
	public final String getMethodName() {
		return this.methodName;
	}

	/**
	 * Get the {@link Method} parameter types of this source.
	 */
	public final String getMethodParameterTypes() {
		return this.methodParameterTypes;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		JavaMethodSource that = (JavaMethodSource) o;
		return Objects.equals(this.className, that.className) && Objects.equals(this.methodName, that.methodName)
				&& Objects.equals(this.methodParameterTypes, that.methodParameterTypes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.className, this.methodName, this.methodParameterTypes);
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("className", this.className)
				.append("methodName", this.methodName)
				.append("methodParameterTypes", this.methodParameterTypes)
				.toString();
		// @formatter:on
	}

}
