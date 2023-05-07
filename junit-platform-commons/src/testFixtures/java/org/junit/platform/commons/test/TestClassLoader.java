/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.test;

import java.lang.StackWalker.Option;
import java.lang.StackWalker.StackFrame;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.junit.platform.commons.util.ClassLoaderUtils;

/**
 * Test {@link ClassLoader} which accepts a class name {@link Predicate} to
 * filter classes that should be loaded by this {@code ClassLoader} instead of
 * the {@linkplain ClassLoaderUtils#getDefaultClassLoader() default ClassLoader}.
 *
 * <p>This class loader is only suitable for specific testing scenarios, where
 * you need to load particular classes from a different class loader.
 *
 * @since 5.10
 */
public class TestClassLoader extends URLClassLoader {

	private static final Predicate<Class<?>> notTestClassLoader = clazz -> !clazz.equals(TestClassLoader.class);

	private final Map<String, Class<?>> cachedClasses = new ConcurrentHashMap<>();

	private final Predicate<String> classNameFilter;

	/**
	 * Create a {@link TestClassLoader} that filters the provided classes.
	 *
	 * @see #forClassNamePrefix(String)
	 */
	public static TestClassLoader forClasses(Class<?>... classes) {
		Predicate<String> classNameFilter = name -> Arrays.stream(classes).map(Class::getName).anyMatch(name::equals);
		return new TestClassLoader(classNameFilter);
	}

	/**
	 * Create a {@link TestClassLoader} that filters classes whose fully
	 * qualified names start with the provided prefix.
	 *
	 * @see #forClasses(Class...)
	 */
	public static TestClassLoader forClassNamePrefix(String prefix) {
		return new TestClassLoader(name -> name.startsWith(prefix));
	}

	public TestClassLoader(Predicate<String> classNameFilter) {
		super(new URL[] { getCodeSourceUrl() }, ClassLoaderUtils.getDefaultClassLoader());

		this.classNameFilter = classNameFilter;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		Class<?> clazz = this.cachedClasses.get(name);
		if (clazz != null) {
			return clazz;
		}
		synchronized (this.cachedClasses) {
			clazz = this.cachedClasses.get(name);
			if (clazz != null) {
				return clazz;
			}
			clazz = this.classNameFilter.test(name) ? findClass(name) : super.loadClass(name);
			this.cachedClasses.put(name, clazz);
			return clazz;
		}
	}

	/**
	 * Get the {@link CodeSource} {@link URL} of the class that instantiated the
	 * {@code TestClassLoader}.
	 */
	private static URL getCodeSourceUrl() {
		StackWalker walker = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

		// @formatter:off
		Class<?> callerClass = walker.walk(stream -> stream
				.map(StackFrame::getDeclaringClass)
				.filter(notTestClassLoader)
				.findFirst()
				.get()
			);
		// @formatter:on

		return callerClass.getProtectionDomain().getCodeSource().getLocation();
	}

}
