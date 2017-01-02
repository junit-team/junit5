/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console.tasks;

import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * @since 1.0
 */
class CustomContextClassLoaderExecutor {

	private final Optional<ClassLoader> customClassLoader;

	CustomContextClassLoaderExecutor(Optional<ClassLoader> customClassLoader) {
		this.customClassLoader = customClassLoader;
	}

	<T> T invoke(Callable<T> callable) throws Exception {
		if (customClassLoader.isPresent()) {
			// Only get/set context class loader when necessary to prevent problems with
			// security managers
			return replaceThreadContextClassLoaderAndInvoke(customClassLoader.get(), callable);
		}
		return callable.call();
	}

	private <T> T replaceThreadContextClassLoaderAndInvoke(ClassLoader customClassLoader, Callable<T> callable)
			throws Exception {
		ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(customClassLoader);
			return callable.call();
		}
		finally {
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		}
	}

}
