/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.junit.gen5.commons.meta.API.Usage.Internal;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.engine.EngineExecutionListener;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExtensionContext;

/**
 * @since 5.0
 */
@API(Internal)
public final class MethodBasedTestExtensionContext extends AbstractExtensionContext<MethodTestDescriptor>
		implements TestExtensionContext {

	private final Object testInstance;

	public MethodBasedTestExtensionContext(ExtensionContext parent, EngineExecutionListener engineExecutionListener,
			MethodTestDescriptor testDescriptor, Object testInstance) {
		super(parent, engineExecutionListener, testDescriptor);
		this.testInstance = testInstance;
	}

	@Override
	public String getUniqueId() {
		return getTestDescriptor().getUniqueId().toString();
	}

	@Override
	public String getDisplayName() {
		return getTestDescriptor().getDisplayName();
	}

	@Override
	public AnnotatedElement getElement() {
		return getTestMethod().get();
	}

	@Override
	public Optional<Class<?>> getTestClass() {
		return Optional.of(getTestDescriptor().getTestClass());
	}

	@Override
	public Optional<Method> getTestMethod() {
		return Optional.of(getTestDescriptor().getTestMethod());
	}

	@Override
	public Object getTestInstance() {
		return this.testInstance;
	}

}
