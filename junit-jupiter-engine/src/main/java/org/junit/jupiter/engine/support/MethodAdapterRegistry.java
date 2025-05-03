/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.support;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.ServiceLoader;

public class MethodAdapterRegistry implements MethodAdapterFactory {

	private final ServiceLoader<MethodAdapterFactory> factories;

	public MethodAdapterRegistry() {
		this.factories = ServiceLoader.load(MethodAdapterFactory.class);
	}

	@Override
	public MethodAdapter adapt(Method method) {
		return factories.stream() //
				.map(ServiceLoader.Provider::get) //
				.map(factory -> factory.adapt(method)) //
				.filter(Objects::nonNull) //
				.findFirst() //
				.orElseGet(() -> MethodAdapter.createDefault(method));
	}
}
