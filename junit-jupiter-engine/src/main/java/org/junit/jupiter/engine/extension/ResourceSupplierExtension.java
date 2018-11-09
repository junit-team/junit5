/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.junit.platform.commons.support.ReflectionSupport.newInstance;

import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ResourceSupplier;
import org.junit.jupiter.api.extension.ResourceSupplier.New;
import org.junit.jupiter.api.extension.ResourceSupplier.Singleton;

class ResourceSupplierExtension implements ParameterResolver {

	private static final Namespace NAMESPACE = Namespace.create(ResourceSupplierExtension.class);

	@Override
	public boolean supportsParameter(ParameterContext parameter, ExtensionContext __) {
		return parameter.isAnnotated(New.class) ^ parameter.isAnnotated(Singleton.class);
	}

	@Override
	public Object resolveParameter(ParameterContext parameter, ExtensionContext extension) {
		ResourceSupplier<?> supplier = supplier(parameter, extension);
		Class<?> parameterType = parameter.getParameter().getType();
		if (ResourceSupplier.class.isAssignableFrom(parameterType)) {
			return supplier;
		}
		Object instance = supplier.get();
		if (parameterType.isAssignableFrom(instance.getClass())) {
			return instance;
		}
		try {
			return supplier.as(parameterType);
		}
		catch (UnsupportedOperationException ignore) {
			// fall-through
		}
		throw new ParameterResolutionException("Parameter type " + parameterType + " isn't compatible with "
				+ ResourceSupplier.class + " nor " + instance.getClass());
	}

	private ResourceSupplier<?> supplier(ParameterContext parameter, ExtensionContext context) {
		Optional<New> newAnnotation = parameter.findAnnotation(New.class);
		if (newAnnotation.isPresent()) {
			Class<? extends ResourceSupplier<?>> type = newAnnotation.get().value();
			String key = type.getName() + '@' + parameter.getIndex();
			ResourceSupplier<?> resourceSupplier = newInstance(type);
			context.getStore(NAMESPACE).put(key, resourceSupplier);
			return resourceSupplier;
		}

		Optional<Singleton> singletonAnnotation = parameter.findAnnotation(Singleton.class);
		if (singletonAnnotation.isPresent()) {
			Class<? extends ResourceSupplier<?>> type = singletonAnnotation.get().value();
			String key = type.getName();
			ExtensionContext.Store store = context.getRoot().getStore(NAMESPACE);
			return store.getOrComputeIfAbsent(key, k -> newInstance(type), ResourceSupplier.class);
		}

		throw new ParameterResolutionException("Can't resolve resource supplier for: " + parameter);
	}
}
