/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.execution;

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.jupiter.engine.execution.ParameterResolutionUtils.resolveParameters;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExecutableInvoker;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * @since 5.9
 */
@API(status = INTERNAL, since = "5.9")
public class SimpleExecutableInvoker implements ExecutableInvoker {

	private final ExtensionContext extensionContext;
	private final ExtensionRegistry extensionRegistry;

	public SimpleExecutableInvoker(ExtensionContext extensionContext, ExtensionRegistry extensionRegistry) {
		this.extensionContext = extensionContext;
		this.extensionRegistry = extensionRegistry;
	}

	public SimpleExecutableInvoker(JupiterEngineExecutionContext context) {
		this(context.getExtensionContext(), context.getExtensionRegistry());
	}

	@Override
	public Object invoke(Executable executable, Object target) {
		if (executable instanceof Constructor) {
			return invoke((Constructor<?>) executable, null);
		}
		return invoke((Method) executable, target);
	}

	@Override
	public <T> T invoke(Constructor<T> constructor, Object outerInstance) {
		Object[] arguments = resolveParameters(constructor, Optional.empty(), wrapWithOptional(outerInstance),
			extensionContext, extensionRegistry);
		return ReflectionUtils.newInstance(constructor, arguments);
	}

	private Object invoke(Method method, Object target) {
		Object[] arguments = resolveParameters(method, wrapWithOptional(target), extensionContext, extensionRegistry);
		return ReflectionUtils.invokeMethod(method, target, arguments);
	}

	@SuppressWarnings("unchecked")
	private Optional<Object> wrapWithOptional(Object object) {
		return (object instanceof Optional ? (Optional<Object>) object : Optional.ofNullable(object));
	}

}
