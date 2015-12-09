/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.example.timing;

import java.lang.reflect.Method;

import org.junit.gen5.api.extension.AfterEachExtensionPoint;
import org.junit.gen5.api.extension.BeforeEachExtensionPoint;
import org.junit.gen5.api.extension.ExtensionPoint;
import org.junit.gen5.api.extension.ExtensionPointRegistry;
import org.junit.gen5.api.extension.TestExtension;
import org.junit.gen5.api.extension.TestExtensionContext;

/**
 * Simple extension that <em>times</em> the execution of test methods and prints the results to {@link System#out}.
 *
 * @since 5.0
 */
public class TimingExtension implements TestExtension {

	private static final String TIMING_KEY_PREFIX = "TIMING:";

	@Override
	public void registerExtensionPoints(ExtensionPointRegistry registry) {
		registry.register(this::beforeEach, BeforeEachExtensionPoint.class, ExtensionPoint.Position.LAST);
		registry.register(this::afterEach, AfterEachExtensionPoint.class, ExtensionPoint.Position.FIRST);
	}

	private void beforeEach(TestExtensionContext testExecutionContext) throws Exception {
		Method testMethod = testExecutionContext.getTestMethod();
		testExecutionContext.getAttributes().put(createKey(testMethod), System.currentTimeMillis());
	}

	private void afterEach(TestExtensionContext testExecutionContext) throws Exception {
		Method testMethod = testExecutionContext.getTestMethod();
		String key = createKey(testMethod);
		long start = (long) testExecutionContext.getAttributes().get(key);
		long end = System.currentTimeMillis();

		System.out.println(String.format("Method [%s] took %s ms", testMethod.getName(), (end - start)));
		testExecutionContext.getAttributes().remove(key);
	}

	private String createKey(Method testMethod) {
		return TIMING_KEY_PREFIX + testMethod;
	}

}
