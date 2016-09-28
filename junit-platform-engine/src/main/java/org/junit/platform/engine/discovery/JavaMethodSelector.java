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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.ToStringBuilder;
import org.junit.platform.engine.DiscoverySelector;

/**
 * A {@link DiscoverySelector} that selects a {@link Method} so that
 * {@link org.junit.platform.engine.TestEngine TestEngines} can discover
 * tests or containers based on Java methods.
 *
 *  If a java {@link Method} is provided, the selector will return this
 * {@link Method} and its method name accordingly. If the selector was
 * created with a {@link Class} and {@link Method} name, a {@link Class}
 * name and {@link Method} name, or only a full qualified {@link Method}
 * name, it will tries to lazy load the {@link Class} and {@link Method}
 * only on request.
 *
 * @since 1.0
 * @see org.junit.platform.engine.support.descriptor.JavaMethodSource
 */
@API(Experimental)
public class JavaMethodSelector implements DiscoverySelector {

	private Class<?> javaClass;
	private String className;
	private Method javaMethod;
	private final String methodName;

	JavaMethodSelector(String methodName) {
		this.methodName = methodName;
	}

	JavaMethodSelector(String className, String methodName) {
		this.className = className;
		this.methodName = methodName;
	}

	JavaMethodSelector(Class<?> javaClass, String methodName) {
		this.javaClass = javaClass;
		this.className = javaClass.getName();
		this.methodName = methodName;
	}

	JavaMethodSelector(Class<?> javaClass, Method method) {
		this.javaClass = javaClass;
		this.className = javaClass.getName();
		this.javaMethod = method;
		this.methodName = method.getName();
	}

	/**
	 * Get the selected {@link Class} name.
	 */
	public String getClassName() {
		return this.className;
	}

	/**
	 * Get the Java {@link Class} in which the selected {@linkplain #getJavaMethod
	 * javaMethod} is declared, or a subclass thereof.
	 *
	 * @see #getJavaMethod()
	 */
	public Class<?> getJavaClass() {
		lazyLoadJavaClassAndMethod();
		return this.javaClass;
	}

	/**
	 * Get the selected {@link Method} name.
	 */
	public String getMethodName() {
		return this.methodName;
	}

	/**
	 * Get the selected Java {@link Method}.
	 *
	 * @see #getJavaClass()
	 */
	public Method getJavaMethod() {
		lazyLoadJavaClassAndMethod();
		return this.javaMethod;
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("className", this.className)
				.append("methodName", this.methodName)
				.toString();
		// @formatter:on
	}

	private void lazyLoadJavaClassAndMethod() {
		if (this.javaClass == null) {
			if (isBlank(this.className)) {
				this.javaMethod = ReflectionUtils.loadMethod(this.methodName).orElseThrow(
					() -> new PreconditionViolationException(
						"Could not load class for method name: " + this.methodName));
				this.javaClass = this.javaMethod.getDeclaringClass();
			}
			else {
				this.javaClass = ReflectionUtils.loadClass(this.className).orElseThrow(
					() -> new PreconditionViolationException("Could not load class with name: " + this.className));
			}
		}

		if (this.javaMethod == null) {
			this.javaMethod = ReflectionUtils.findMethod(this.javaClass, this.methodName).orElseThrow(
				() -> new PreconditionViolationException(String.format(
					"Could not find method with name [%s] in class [%s].", this.methodName, this.javaClass.getName())));
		}
	}

}
