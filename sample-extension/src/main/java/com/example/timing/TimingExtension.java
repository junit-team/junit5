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

import org.junit.gen5.api.extension.AfterEachCallbacks;
import org.junit.gen5.api.extension.BeforeEachCallbacks;
import org.junit.gen5.api.extension.TestExtensionContext;

/**
 * Simple extension that <em>times</em> the execution of test methods and
 * prints the results to {@link System#out}.
 *
 * @since 5.0
 */
public class TimingExtension implements BeforeEachCallbacks, AfterEachCallbacks {

	private static final String TIMING_KEY_PREFIX = "TIMING:";

	@Override
	public void postBeforeEach(TestExtensionContext context) throws Exception {
		Method testMethod = context.getTestMethod();
		context.getAttributes().put(createKey(testMethod), System.currentTimeMillis());
	}

	@Override
	public void preAfterEach(TestExtensionContext context) throws Exception {
		Method testMethod = context.getTestMethod();
		String key = createKey(testMethod);
		long start = (long) context.getAttributes().get(key);
		long end = System.currentTimeMillis();

		System.out.println(String.format("Method [%s] took %s ms", testMethod.getName(), (end - start)));
		context.getAttributes().remove(key);
	}

	private String createKey(Method testMethod) {
		return TIMING_KEY_PREFIX + testMethod;
	}

}
