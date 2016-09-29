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

import static java.util.Collections.unmodifiableList;
import static org.junit.platform.commons.meta.API.Usage.Experimental;
import static org.junit.platform.commons.util.StringUtils.nullSafeToString;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
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
	private final Class<?>[] javaMethodParameterTypes;

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
		this.javaMethodParameterTypes = method.getParameterTypes();
	}

	/**
	 * Get the declaring {@link Class} name of this source.
	 */
	public String getClassName() {
		return this.className;
	}

	/**
	 * Get the method name of this source.
	 *
	 * @see Method#getName()
	 */
	public final String getMethodName() {
		return this.methodName;
	}

	/**
	 * Get the method parameter types of this source.
	 *
	 * @see Method#getParameterTypes()
	 */
	public final List<Class<?>> getMethodParameterTypes() {
		return unmodifiableList(Arrays.asList(this.javaMethodParameterTypes));
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
				&& Arrays.equals(this.javaMethodParameterTypes, that.javaMethodParameterTypes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(this.className, this.methodName) + Arrays.hashCode(this.javaMethodParameterTypes);
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("className", this.className)
				.append("methodName", this.methodName)
				.append("javaMethodParameterTypes", nullSafeToString(this.javaMethodParameterTypes))
				.toString();
		// @formatter:on
	}

}
