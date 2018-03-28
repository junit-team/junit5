/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;

/**
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
public final class ClassExtensionContext extends AbstractExtensionContext<ClassTestDescriptor> {

	private final Lifecycle lifecycle;

	private final ThrowableCollector throwableCollector;

	private Object testInstance;

	/**
	 * Create a new {@code ClassExtensionContext} with {@link Lifecycle#PER_METHOD}.
	 *
	 * @see #ClassExtensionContext(ExtensionContext, EngineExecutionListener, ClassTestDescriptor, Lifecycle, ConfigurationParameters, ThrowableCollector)
	 */
	public ClassExtensionContext(ExtensionContext parent, EngineExecutionListener engineExecutionListener,
			ClassTestDescriptor testDescriptor, ConfigurationParameters configurationParameters,
			ThrowableCollector throwableCollector) {

		this(parent, engineExecutionListener, testDescriptor, Lifecycle.PER_METHOD, configurationParameters,
			throwableCollector);
	}

	public ClassExtensionContext(ExtensionContext parent, EngineExecutionListener engineExecutionListener,
			ClassTestDescriptor testDescriptor, Lifecycle lifecycle, ConfigurationParameters configurationParameters,
			ThrowableCollector throwableCollector) {

		super(parent, engineExecutionListener, testDescriptor, configurationParameters);

		this.lifecycle = lifecycle;
		this.throwableCollector = throwableCollector;
	}

	@Override
	public Optional<AnnotatedElement> getElement() {
		return Optional.of(getTestDescriptor().getTestClass());
	}

	@Override
	public Optional<Class<?>> getTestClass() {
		return Optional.of(getTestDescriptor().getTestClass());
	}

	@Override
	public Optional<Lifecycle> getTestInstanceLifecycle() {
		return Optional.of(this.lifecycle);
	}

	void setTestInstance(Object testInstance) {
		this.testInstance = testInstance;
	}

	@Override
	public Optional<Object> getTestInstance() {
		return Optional.ofNullable(this.testInstance);
	}

	@Override
	public Optional<Method> getTestMethod() {
		return Optional.empty();
	}

	@Override
	public Optional<Throwable> getExecutionException() {
		return Optional.ofNullable(this.throwableCollector.getThrowable());
	}

}
