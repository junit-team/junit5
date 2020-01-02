/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.discovery;

import static org.apiguardian.api.API.Status.STABLE;

import java.lang.reflect.Method;
import java.util.Objects;

import org.apiguardian.api.API;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.ClassUtils;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.DiscoverySelector;

/**
 * A {@link DiscoverySelector} that selects a {@link Method} or a combination of
 * class name, method name, and parameter types so that
 * {@link org.junit.platform.engine.TestEngine TestEngines} can discover tests
 * or containers based on methods.
 *
 * <p>If a Java {@link Method} is provided, the selector will return that
 * {@linkplain #getJavaMethod() method} and its method name, class name, and
 * parameter types accordingly. If a {@link Class} and method name, a class name
 * and method name, or simply a <em>fully qualified method name</em> is provided,
 * this selector will only attempt to lazily load the {@link Class} and
 * {@link Method} if {@link #getJavaClass()} or {@link #getJavaMethod()} is
 * invoked.
 *
 * <p>In this context, a Java {@code Method} means anything that can be referenced
 * as a {@link Method} on the JVM &mdash; for example, methods from Java classes
 * or methods from other JVM languages such Groovy, Scala, etc.
 *
 * @since 1.0
 * @see DiscoverySelectors#selectMethod(String)
 * @see DiscoverySelectors#selectMethod(String, String)
 * @see DiscoverySelectors#selectMethod(String, String, String)
 * @see DiscoverySelectors#selectMethod(Class, String)
 * @see DiscoverySelectors#selectMethod(Class, String, String)
 * @see DiscoverySelectors#selectMethod(Class, Method)
 * @see org.junit.platform.engine.support.descriptor.MethodSource
 */
@API(status = STABLE, since = "1.0")
public class MethodSelector implements DiscoverySelector {

	private final String className;
	private final String methodName;
	private final String methodParameterTypes;

	private Class<?> javaClass;
	private Method javaMethod;

	MethodSelector(String className, String methodName) {
		this(className, methodName, "");
	}

	MethodSelector(String className, String methodName, String methodParameterTypes) {
		this.className = className;
		this.methodName = methodName;
		this.methodParameterTypes = methodParameterTypes;
	}

	MethodSelector(Class<?> javaClass, String methodName) {
		this(javaClass, methodName, "");
	}

	MethodSelector(Class<?> javaClass, String methodName, String methodParameterTypes) {
		this.javaClass = javaClass;
		this.className = javaClass.getName();
		this.methodName = methodName;
		this.methodParameterTypes = methodParameterTypes;
	}

	MethodSelector(Class<?> javaClass, Method method) {
		this.javaClass = javaClass;
		this.className = javaClass.getName();
		this.javaMethod = method;
		this.methodName = method.getName();
		this.methodParameterTypes = ClassUtils.nullSafeToString(method.getParameterTypes());
	}

	/**
	 * Get the selected class name.
	 */
	public String getClassName() {
		return this.className;
	}

	/**
	 * Get the selected method name.
	 */
	public String getMethodName() {
		return this.methodName;
	}

	/**
	 * Get the parameter types for the selected method as a {@link String},
	 * typically a comma-separated list of primitive types, fully qualified
	 * class names, or array types.
	 *
	 * <p>Note: the parameter types are provided as a single string instead of
	 * a collection in order to allow this selector to be used in a generic
	 * fashion by various test engines. It is therefore the responsibility of
	 * the caller of this method to determine how to parse the returned string.
	 *
	 * @return the parameter types supplied to this {@code MethodSelector} via
	 * a constructor or deduced from a {@code Method} supplied via a constructor;
	 * never {@code null}
	 */
	public String getMethodParameterTypes() {
		return this.methodParameterTypes;
	}

	/**
	 * Get the {@link Class} in which the selected {@linkplain #getJavaMethod
	 * method} is declared, or a subclass thereof.
	 *
	 * <p>If the {@link Class} was not provided, but only the name, this method
	 * attempts to lazily load the {@code Class} based on its name and throws a
	 * {@link PreconditionViolationException} if the class cannot be loaded.
	 *
	 * @see #getJavaMethod()
	 */
	public Class<?> getJavaClass() {
		lazyLoadJavaClass();
		return this.javaClass;
	}

	/**
	 * Get the selected {@link Method}.
	 *
	 * <p>If the {@link Method} was not provided, but only the name, this method
	 * attempts to lazily load the {@code Method} based on its name and throws a
	 * {@link PreconditionViolationException} if the method cannot be loaded.
	 *
	 * @see #getJavaClass()
	 */
	public Method getJavaMethod() {
		lazyLoadJavaMethod();
		return this.javaMethod;
	}

	private void lazyLoadJavaClass() {
		if (this.javaClass == null) {
			// @formatter:off
			this.javaClass = ReflectionUtils.tryToLoadClass(this.className).getOrThrow(
				cause -> new PreconditionViolationException("Could not load class with name: " + this.className, cause));
			// @formatter:on
		}
	}

	private void lazyLoadJavaMethod() {
		lazyLoadJavaClass();

		if (this.javaMethod == null) {
			if (StringUtils.isNotBlank(this.methodParameterTypes)) {
				this.javaMethod = ReflectionUtils.findMethod(this.javaClass, this.methodName,
					this.methodParameterTypes).orElseThrow(
						() -> new PreconditionViolationException(String.format(
							"Could not find method with name [%s] and parameter types [%s] in class [%s].",
							this.methodName, this.methodParameterTypes, this.javaClass.getName())));
			}
			else {
				this.javaMethod = ReflectionUtils.findMethod(this.javaClass, this.methodName).orElseThrow(
					() -> new PreconditionViolationException(
						String.format("Could not find method with name [%s] in class [%s].", this.methodName,
							this.javaClass.getName())));
			}
		}
	}

	/**
	 * @since 1.3
	 */
	@API(status = STABLE, since = "1.3")
	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}
		MethodSelector that = (MethodSelector) o;
		return Objects.equals(this.className, that.className)//
				&& Objects.equals(this.methodName, that.methodName)//
				&& Objects.equals(this.methodParameterTypes, that.methodParameterTypes);
	}

	/**
	 * @since 1.3
	 */
	@API(status = STABLE, since = "1.3")
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
