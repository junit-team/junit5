/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.junit.gen5.commons.util.Preconditions;

// TODO: Implement support for other LifeCyle and Inheritance configurations
public class ContextScope<K, V> {

	public enum Inheritance {
		Yes, No
	}

	private final Function<K, V> creator;

	private Map<TestExecutionContext, Map<K, V>> values = new HashMap<>();

	public ContextScope(Function<K, V> creator, Inheritance inheritance) {
		Preconditions.condition(inheritance == Inheritance.Yes, "Only Inheritance.Yes supported");
		this.creator = creator;
	}

	public V get(TestExecutionContext context, K key) {
		V value = getInContext(Optional.of(context), key);
		if (value == null) {
			value = creator.apply(key);
			putInContext(context, key, value);
		}
		return value;
	}

	private V getInContext(Optional<TestExecutionContext> optionalContext, K key) {
		if (!optionalContext.isPresent())
			return null;
		TestExecutionContext context = optionalContext.get();
		Map<K, V> valuesMap = valuesFor(context);
		V value = valuesMap.get(key);
		if (value == null)
			return getInContext(context.getParent(), key);
		else
			return value;
	}

	private void putInContext(TestExecutionContext context, K key, V value) {
		valuesFor(context).put(key, value);
	}

	private Map<K, V> valuesFor(TestExecutionContext context) {
		return this.values.computeIfAbsent(context, key -> new HashMap<>());
	}

}
