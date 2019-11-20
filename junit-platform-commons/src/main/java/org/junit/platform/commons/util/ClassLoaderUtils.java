/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.apiguardian.api.API;

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
@API(status = INTERNAL, since = "1.0")
public final class ClassLoaderUtils {

	private ClassLoaderUtils() {
		/* no-op */
	}

	public static ClassLoader getDefaultClassLoader() {
		try {
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			if (contextClassLoader != null) {
				return contextClassLoader;
			}
		}
		catch (Throwable t) {
			BlacklistedExceptions.rethrowIfBlacklisted(t);
			/* otherwise ignore */
		}
		return ClassLoader.getSystemClassLoader();
	}

	static URL toURL(String filePath) {
		try {
			return Paths.get(filePath).toUri().toURL();
		}
		catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Returns a classloader that has visibility to the current classpath, but won't delegate to the parent for classes that
	 * match the supplied filter. If you attempt to load a class using this loader that matches the filter, then
	 * it will be loaded directly from the classpath and a new {@link Class} object created.
	 *
	 * <p>This is sometimes useful for testing if you have a class in your JVM that's already visible to the current
	 * class loader, but for testing purposes you need to load it from the context of another class loader. This allows
	 * you to emulate environments that have multiple classloaders (eg, OSGi).
	 *
	 * <p>For an example, see {@link ReflectionUtilsTest#findMethodByParameterTypesFromForeignClassLoader}.
	 *
	 * @param filter predicate to match on the type names. If the predicate returns true, then the classloader won't
	 * delegate to the parent classloader but will load the class directly from the classpath.
	 * @return The excluding class loader.
	 */
	public static URLClassLoader excludingClassLoader(Predicate<String> filter) {
		return new URLClassLoader(Stream.of(System.getProperty("java.class.path").split(File.pathSeparator)).map(
			ClassLoaderUtils::toURL).toArray(URL[]::new), new ClassLoader() {
				@Override
				protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
					if (filter.test(name)) {
						throw new ClassNotFoundException("Skipping " + name + " for testing");
					}
					return super.loadClass(name, resolve);
				}
			});
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
			catch (Throwable t) {
				BlacklistedExceptions.rethrowIfBlacklisted(t);
				/* otherwise ignore */
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
