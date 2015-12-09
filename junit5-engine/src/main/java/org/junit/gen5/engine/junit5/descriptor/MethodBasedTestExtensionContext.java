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
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.TestExtensionContext;

final class MethodBasedTestExtensionContext implements TestExtensionContext {

	private final Map<String, Object> attributes = new LinkedHashMap<>();

	private final MethodTestDescriptor testDescriptor;
	private final Object testInstance;

	public MethodBasedTestExtensionContext(MethodTestDescriptor testDescriptor, Object testInstance) {
		this.testDescriptor = testDescriptor;
		this.testInstance = testInstance;
	}

	@Override
	public Method getTestMethod() {
		return testDescriptor.getTestMethod();
	}

	@Override
	public Object getTestInstance() {
		return testInstance;
	}

	@Override
	public Class<?> getTestClass() {
		return testDescriptor.getTestClass();
	}

	@Override
	public Optional<ExtensionContext> getParent() {
		// TODO implement this
		return Optional.empty();
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
		return getTestMethod();
	}
}