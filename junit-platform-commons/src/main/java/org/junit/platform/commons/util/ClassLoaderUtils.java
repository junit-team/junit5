/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.commons.util;

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.net.URL;
import java.security.CodeSource;
import java.util.Optional;

import org.junit.platform.commons.meta.API;

/**
 * Collection of utilities for working with {@linkplain ClassLoader} and associated tasks.
 *
 * <h3>DISCLAIMER</h3>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 1.0
 */
@API(Internal)
public final class ClassLoaderUtils {

	///CLOVER:OFF
	private ClassLoaderUtils() {
		/* no-op */
	}
	///CLOVER:ON

	public static ClassLoader getDefaultClassLoader() {
		try {
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			if (contextClassLoader != null) {
				return contextClassLoader;
			}
		}
		catch (Throwable ex) {
			/* ignore */
		}
		return ClassLoader.getSystemClassLoader();
	}

	/**
	 * Get the location from which the supplied object's class was loaded.
	 *
	 * @param object the object for whose class the location should be retrieved
	 * @return an {@code Optional} containing the URL of the class' location; never
	 * {@code null} but potentially empty
	 */
	public static Optional<URL> getLocation(Object object) {
		Preconditions.notNull(object, "object must not be null");
		// determine class loader
		ClassLoader loader = object.getClass().getClassLoader();
		if (loader == null) {
			loader = ClassLoader.getSystemClassLoader();
			while (loader != null && loader.getParent() != null) {
				loader = loader.getParent();
			}
		}
		// try finding resource by name
		if (loader != null) {
			String name = object.getClass().getName();
			name = name.replace(".", "/") + ".class";
			try {
				return Optional.ofNullable(loader.getResource(name));
			}
			catch (Throwable ignore) {
				/* ignore */
			}
		}
		// try protection domain
		try {
			CodeSource codeSource = object.getClass().getProtectionDomain().getCodeSource();
			if (codeSource != null) {
				return Optional.ofNullable(codeSource.getLocation());
			}
		}
		catch (SecurityException ignore) {
			/* ignore */
		}
		return Optional.empty();
	}
}
