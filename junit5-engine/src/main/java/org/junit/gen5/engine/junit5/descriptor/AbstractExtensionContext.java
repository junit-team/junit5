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

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.engine.EngineExecutionListener;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.ExtensionValuesStore.Namespace;

abstract class AbstractExtensionContext implements ExtensionContext {

	private final Map<String, Object> attributes = new HashMap<>();

	//Will replace attributes if done
	private final ExtensionValuesStore store;

	private final ExtensionContext parent;
	private final EngineExecutionListener engineExecutionListener;
	private final TestDescriptor testDescriptor;

	AbstractExtensionContext(ExtensionContext parent, EngineExecutionListener engineExecutionListener,
			TestDescriptor testDescriptor) {
		this.parent = parent;
		this.engineExecutionListener = engineExecutionListener;
		this.testDescriptor = testDescriptor;
		this.store = createStore(parent);
	}

	private final ExtensionValuesStore createStore(ExtensionContext parent) {
		ExtensionValuesStore parentStore = null;
		if (parent != null) {
			parentStore = ((AbstractExtensionContext) parent).store;
		}
		return new ExtensionValuesStore(parentStore);
	}

	@Override
	public void publishReportEntry(Map<String, String> entry) {
		engineExecutionListener.reportingEntryPublished(this.testDescriptor, entry);
	}

	@Override
	public Optional<ExtensionContext> getParent() {
		return Optional.ofNullable(parent);
	}

	@Override
	public Object getAttribute(String key) {
		Object value = attributes.get(key);
		if (value == null && parent != null)
			return parent.getAttribute(key);
		return value;
	}

	@Override
	public void putAttribute(String key, Object value) {
		attributes.put(key, value);
	}

	@Override
	public Object removeAttribute(String key) {
		return attributes.remove(key);
	}

	protected TestDescriptor getTestDescriptor() {
		return testDescriptor;
	}

	//Storing methods. All delegate to the store.
	//TODO: Remove duplication between these methods and ExtensionValuesStore
	//      as soon as we have a decision if methods should be exposed on store object instead of via delegation

	@Override
	public Object get(Object key) {
		return store.get(key);
	}

	@Override
	public void put(Object key, Object value) {
		store.put(key, value);
	}

	@Override
	public Object getOrComputeIfAbsent(Object key, Function<Object, Object> defaultCreator) {
		return store.getOrComputeIfAbsent(key, defaultCreator);
	}

	@Override
	public void remove(Object key) {
		store.remove(key);
	}

	@Override
	public Object get(Object key, String namespace) {
		return store.get(key, Namespace.sharedWith(namespace));
	}

	@Override
	public void put(Object key, Object value, String namespace) {
		store.put(key, value, Namespace.sharedWith(namespace));
	}

	@Override
	public Object getOrComputeIfAbsent(Object key, Function<Object, Object> defaultCreator, String namespace) {
		return store.getOrComputeIfAbsent(key, defaultCreator, Namespace.sharedWith(namespace));
	}

	@Override
	public void remove(Object key, String namespace) {
		store.remove(key, Namespace.sharedWith(namespace));
	}

}
