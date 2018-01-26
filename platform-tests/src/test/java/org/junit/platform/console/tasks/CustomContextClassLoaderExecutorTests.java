/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.tasks;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 * @since 1.0
 */
class CustomContextClassLoaderExecutorTests {

	@Test
	void invokeWithoutCustomClassLoaderDoesNotSetClassLoader() throws Exception {
		ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		CustomContextClassLoaderExecutor executor = new CustomContextClassLoaderExecutor(Optional.empty());

		int result = executor.invoke(() -> {
			assertSame(originalClassLoader, Thread.currentThread().getContextClassLoader());
			return 42;
		});

		assertEquals(42, result);
		assertSame(originalClassLoader, Thread.currentThread().getContextClassLoader());
	}

	@Test
	void invokeWithCustomClassLoaderSetsCustomAndResetsToOriginal() throws Exception {
		ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		ClassLoader customClassLoader = URLClassLoader.newInstance(new URL[0]);
		CustomContextClassLoaderExecutor executor = new CustomContextClassLoaderExecutor(
			Optional.of(customClassLoader));

		int result = executor.invoke(() -> {
			assertSame(customClassLoader, Thread.currentThread().getContextClassLoader());
			return 23;
		});

		assertEquals(23, result);
		assertSame(originalClassLoader, Thread.currentThread().getContextClassLoader());
	}
}
