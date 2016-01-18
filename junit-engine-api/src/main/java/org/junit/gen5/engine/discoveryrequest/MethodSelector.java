/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.discoveryrequest;

import java.lang.reflect.Method;

import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.DiscoverySelectorVisitor;

/**
 * @since 5.0
 */
public class MethodSelector implements DiscoverySelector {
	private final Class<?> testClass;
	private final Method testMethod;

	public MethodSelector(Class<?> testClass, Method testMethod) {
		this.testClass = testClass;
		this.testMethod = testMethod;
	}

	@Override
	public void accept(DiscoverySelectorVisitor visitor) {
		visitor.visitMethod(testClass, testMethod);
	}

	public Class<?> getTestClass() {
		return testClass;
	}

	public Method getTestMethod() {
		return testMethod;
	}
}
