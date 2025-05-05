/*
 * Copyright 2015-2025 the original author or authors.
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
import java.lang.reflect.Method;
import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExecutableInvoker;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.jupiter.engine.support.MethodAdapterFactory;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * @since 5.9
 */
@API(status = INTERNAL, since = "5.9")
public class DefaultExecutableInvoker implements ExecutableInvoker {

	private final ExtensionContext extensionContext;
	private final ExtensionRegistry extensionRegistry;
	private final MethodAdapterFactory methodAdapterFactory;

	public DefaultExecutableInvoker(ExtensionContext extensionContext, ExtensionRegistry extensionRegistry,
			MethodAdapterFactory methodAdapterFactory) {
		this.extensionContext = extensionContext;
		this.extensionRegistry = extensionRegistry;
		this.methodAdapterFactory = methodAdapterFactory;
	}

	@Override
	public <T> T invoke(Constructor<T> constructor, Object outerInstance) {
		Object[] arguments = resolveParameters(constructor, Optional.empty(), Optional.ofNullable(outerInstance),
			extensionContext, extensionRegistry);
		return ReflectionUtils.newInstance(constructor, arguments);
	}

	@Override
	public Object invoke(Method method, Object target) {
		var adapter = methodAdapterFactory.adapt(method);
		Object[] arguments = resolveParameters(adapter, Optional.ofNullable(target), extensionContext,
			extensionRegistry);
		return adapter.invoke(target, arguments);
	}

}
