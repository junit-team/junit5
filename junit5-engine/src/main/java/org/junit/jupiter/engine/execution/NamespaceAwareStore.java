/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.execution;

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.util.function.Function;

import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.platform.commons.meta.API;

/**
 * @since 5.0
 */
@API(Internal)
public class NamespaceAwareStore implements Store {

	private final ExtensionValuesStore valuesStore;
	private final Namespace namespace;

	public NamespaceAwareStore(ExtensionValuesStore valuesStore, Namespace namespace) {
		this.valuesStore = valuesStore;
		this.namespace = namespace;
	}

	@Override
	public Object get(Object key) {
		return valuesStore.get(namespace, key);
	}

	@Override
	public void put(Object key, Object value) {
		valuesStore.put(namespace, key, value);
	}

	@Override
	public Object getOrComputeIfAbsent(Object key, Function<Object, Object> defaultCreator) {
		return valuesStore.getOrComputeIfAbsent(namespace, key, defaultCreator);
	}

	@Override
	public Object remove(Object key) {
		return valuesStore.remove(namespace, key);
	}

}
