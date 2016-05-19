/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.support.descriptor;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;
import static org.junit.gen5.commons.util.StringUtils.nullSafeToString;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ToStringBuilder;

/**
 * @since 5.0
 */
@API(Experimental)
public class JavaMethodSource implements JavaSource {
	private static final long serialVersionUID = 1L;

	private final Class<?> javaClass;
	private final String javaMethodName;
	private final Class<?>[] javaMethodParameterTypes;

	public JavaMethodSource(Method javaMethod) {
		Preconditions.notNull(javaMethod, "java method must not be null");
		javaClass = javaMethod.getDeclaringClass();
		javaMethodName = javaMethod.getName();
		javaMethodParameterTypes = javaMethod.getParameterTypes();
	}

	public Class<?> getJavaClass() {
		return javaClass;
	}

	public String getJavaMethodName() {
		return javaMethodName;
	}

	public Class<?>[] getJavaMethodParameterTypes() {
		return javaMethodParameterTypes;
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("javaClass", javaClass)
				.append("javaMethodName", javaMethodName)
				.append("javaMethodParameterTypes", nullSafeToString(getJavaMethodParameterTypes()))
				.toString();
		// @formatter:on
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		JavaMethodSource that = (JavaMethodSource) o;
		return Objects.equals(javaClass, that.javaClass) && Objects.equals(javaMethodName, that.javaMethodName)
				&& Arrays.equals(javaMethodParameterTypes, that.javaMethodParameterTypes);
	}

	@Override
	public int hashCode() {
		return Objects.hash(javaClass, javaMethodName, javaMethodParameterTypes);
	}
}
