/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.tasks;

import java.net.URLClassLoader;
import java.util.concurrent.Callable;

import org.junit.platform.commons.util.BlacklistedExceptions;

/**
 * @since 1.0
 */
class CustomContextClassLoaderExecutor {

	private final URLClassLoader customClassLoaderOrNull;

	CustomContextClassLoaderExecutor(URLClassLoader customClassLoaderOrNull) {
		this.customClassLoaderOrNull = customClassLoaderOrNull;
	}

	<T> T invoke(Callable<T> callable) throws Exception {
		if (customClassLoaderOrNull != null) {
			// Only get/set context class loader when necessary to prevent problems with
			// security managers
			return replaceThreadContextClassLoaderAndInvoke(callable);
		}
		return callable.call();
	}

	private <T> T replaceThreadContextClassLoaderAndInvoke(Callable<T> callable) throws Exception {
		ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(customClassLoaderOrNull);
			return callable.call();
		}
		finally {
			Thread.currentThread().setContextClassLoader(originalClassLoader);
			try {
				customClassLoaderOrNull.close();
			}
			catch (Throwable t) {
				BlacklistedExceptions.rethrowIfBlacklisted(t);
				// Ignore or log unchecked exceptions and errors?
			}
		}
	}

}
