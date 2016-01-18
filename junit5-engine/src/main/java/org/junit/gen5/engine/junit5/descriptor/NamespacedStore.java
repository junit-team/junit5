/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import java.util.function.Function;

import org.junit.gen5.api.extension.ExtensionContext.Namespace;
import org.junit.gen5.api.extension.ExtensionContext.Store;

public class NamespacedStore implements Store {
	private final ExtensionValuesStore valuesStore;
	private final Namespace namespace;

	public NamespacedStore(ExtensionValuesStore valuesStore, Namespace namespace) {
		this.valuesStore = valuesStore;
		this.namespace = namespace;
	}

	@Override
	public Object get(Object key) {
		return valuesStore.get(key, namespace);
	}

	@Override
	public void put(Object key, Object value) {
		valuesStore.put(key, value, namespace);
	}

	@Override
	public Object getOrComputeIfAbsent(Object key, Function<Object, Object> defaultCreator) {
		return valuesStore.getOrComputeIfAbsent(key, defaultCreator, namespace);
	}

	@Override
	public Object remove(Object key) {
		return valuesStore.remove(key, namespace);
	}
}
