/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import java.lang.reflect.Method;
import java.util.Optional;

public class JavaSource implements TestSource {

	private final Class<?> javaClass;

	private final Method javaMethod;

	public JavaSource(Class<?> clazz) {
		javaClass = clazz;
		javaMethod = null;
	}

	public JavaSource(Method method) {
		javaClass = method.getDeclaringClass();
		javaMethod = method;
	}

	@Override
	public boolean isJavaClass() {
		return javaClass != null && javaMethod == null;
	}

	@Override
	public boolean isJavaMethod() {
		return javaMethod != null;
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

	public Optional<Method> getJavaMethod() {
		return Optional.ofNullable(javaMethod);
	}

	@Override
	public String toString() {
		// TODO Add parameters to method string
		StringBuilder builder = new StringBuilder();
		getJavaClass().ifPresent(clazz -> builder.append(clazz.getName()));
		getJavaMethod().ifPresent(method -> {
			builder.append('#');
			builder.append(method.getName());
		});
		return builder.toString();
	}
}
