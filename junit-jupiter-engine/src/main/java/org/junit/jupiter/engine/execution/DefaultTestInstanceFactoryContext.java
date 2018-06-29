/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.execution;

import java.util.Optional;

import org.junit.jupiter.api.extension.TestInstanceFactoryContext;

public class DefaultTestInstanceFactoryContext implements TestInstanceFactoryContext {

	private final Class<?> testClass;
	private final Optional<Object> outerInstance;

	public DefaultTestInstanceFactoryContext(Class<?> testClass, Optional<Object> outerInstance) {
		this.testClass = testClass;
		this.outerInstance = outerInstance;
	}

	@Override
	public Class<?> getTestClass() {
		return testClass;
	}

	@Override
	public Optional<Object> getOuterInstance() {
		return outerInstance;
	}

}
