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

import java.lang.reflect.AnnotatedElement;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.gen5.api.extension.ContainerExtensionContext;
import org.junit.gen5.api.extension.ExtensionContext;

final class ClassBasedContainerExtensionContext implements ContainerExtensionContext {

	private final Map<String, Object> attributes = new LinkedHashMap<>();

	private ExtensionContext parent;
	private final ClassTestDescriptor testDescriptor;

	public ClassBasedContainerExtensionContext(ExtensionContext parent, ClassTestDescriptor testDescriptor) {
		this.parent = parent;
		this.testDescriptor = testDescriptor;
	}

	@Override
	public Class<?> getTestClass() {
		return testDescriptor.getTestClass();
	}

	@Override
	public Optional<ExtensionContext> getParent() {
		return Optional.ofNullable(parent);
	}

	@Override
	public String getDisplayName() {
		return testDescriptor.getDisplayName();
	}

	@Override
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	@Override
	public AnnotatedElement getElement() {
		return getTestClass();
	}

	@Override
	public Optional<Object> getContainerInstance() {
		// TODO implement this
		return Optional.empty();
	}
}