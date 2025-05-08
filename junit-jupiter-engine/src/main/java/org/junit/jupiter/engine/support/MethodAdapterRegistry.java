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

import static org.apiguardian.api.API.Status.INTERNAL;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.ServiceLoader;

import org.apiguardian.api.API;

@API(status = INTERNAL, since = "6.0")
public class MethodAdapterRegistry implements MethodAdapterFactory {

	private final List<MethodAdapterFactory> factories;

	public MethodAdapterRegistry() {
		// Load and instantiate factories eagerly to avoid GraalVM issue
		this.factories = ServiceLoader.load(MethodAdapterFactory.class).stream() //
				.map(ServiceLoader.Provider::get) //
				.toList();
	}

	@Override
	public MethodAdapter adapt(Method method) {
		return this.factories.stream() //
				.map(factory -> factory.adapt(method)) //
				.filter(Objects::nonNull) //
				.findFirst() //
				.orElseGet(() -> MethodAdapter.createDefault(method));
	}
}
