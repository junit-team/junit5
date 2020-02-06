/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.execution;

import static java.util.Collections.unmodifiableList;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.platform.commons.util.ReflectionUtils;

class MethodInvocation<T> implements Invocation<T>, ReflectiveInvocationContext<Method> {

	protected final Method method;
	protected final Optional<Object> target;
	protected final Object[] arguments;

	MethodInvocation(Method method, Optional<Object> target, Object[] arguments) {
		this.method = method;
		this.target = target;
		this.arguments = arguments;
	}

	@Override
	public Class<?> getTargetClass() {
		return target.<Class<?>> map(Object::getClass).orElseGet(method::getDeclaringClass);
	}

	@Override
	@SuppressWarnings("unchecked")
	public Optional<Object> getTarget() {
		return target;
	}

	@Override
	public Method getExecutable() {
		return method;
	}

	@Override
	public List<Object> getArguments() {
		return unmodifiableList(Arrays.asList(arguments));
	}

	@Override
	@SuppressWarnings("unchecked")
	public T proceed() {
		return (T) ReflectionUtils.invokeMethod(method, target.orElse(null), arguments);
	}

}
