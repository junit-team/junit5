/*
 * Copyright 2015 the original author or authors.
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

import org.junit.gen5.api.extension.ExtensionContext;

abstract class AbstractExtensionContext implements ExtensionContext {

	private final Map<String, Object> attributes = new HashMap<>();

	private ExtensionContext parent;

	AbstractExtensionContext(ExtensionContext parent) {
		this.parent = parent;
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
}