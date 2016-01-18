/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.support.discovery;

import static org.junit.gen5.commons.util.StringUtils.nullSafeToString;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.gen5.engine.TestSource;

public class JavaSource implements TestSource {

	private static final long serialVersionUID = 1L;

	private final Class<?> javaClass;
	private final String javaMethodName;
	private final Class<?>[] javaMethodParameterTypes;

	public JavaSource(Class<?> clazz) {
		javaClass = clazz;
		javaMethodName = null;
		javaMethodParameterTypes = null;
	}

	public JavaSource(Method method) {
		javaClass = method.getDeclaringClass();
		javaMethodName = method.getName();
		javaMethodParameterTypes = method.getParameterTypes();
	}

	@Override
	public boolean isJavaClass() {
		return javaClass != null && javaMethodName == null;
	}

	@Override
	public boolean isJavaMethod() {
		return javaMethodName != null;
	}

	@Override
	public boolean isDirectory() {
		return false;
	}

	@Override
	public boolean isFile() {
		return false;
	}

	@Override
	public boolean isFilePosition() {
		return false;
	}

	public Optional<Class<?>> getJavaClass() {
		return Optional.ofNullable(javaClass);
	}

	public Optional<String> getJavaMethodName() {
		return Optional.ofNullable(javaMethodName);
	}

	public Optional<Class<?>[]> getJavaMethodParameterTypes() {
		return Optional.ofNullable(javaMethodParameterTypes);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		getJavaClass().ifPresent(clazz -> builder.append(clazz.getName()));
		getJavaMethodName().ifPresent(method -> {
			builder.append('#').append(method);
		});
		getJavaMethodParameterTypes().ifPresent(parameterTypes -> {
			builder.append('(').append(nullSafeToString(parameterTypes)).append(')');
		});
		return builder.toString();
	}
}
