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
import java.util.function.Predicate;

import org.junit.platform.commons.util.ClassLoaderUtils;

/**
 * Custom {@link ClassLoader} which accepts a class name {@link Predicate} to
 * filter classes that should be loaded by this {@code ClassLoader} instead of
 * the {@linkplain ClassLoaderUtils#getDefaultClassLoader() default ClassLoader}.
 *
 * <p>This class loader is only suitable for specific testing scenarios, where
 * you need to load particular classes from a different class loader.
 *
 * @since 5.10
 */
public class FilteringClassLoader extends URLClassLoader {

	private static final Predicate<Class<?>> notFilteringClassLoader = //
		clazz -> !clazz.equals(FilteringClassLoader.class);

	private final Predicate<String> classNameFilter;

	public FilteringClassLoader(Predicate<String> classNameFilter) {
		super(new URL[] { getCodeSourceUrl() }, ClassLoaderUtils.getDefaultClassLoader());

		this.classNameFilter = classNameFilter;
	}

	@Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
		return (this.classNameFilter.test(name) ? findClass(name) : super.loadClass(name));
	}

	/**
	 * Get the {@link CodeSource} {@link URL} of the class that instantiated the
	 * {@code FilteringClassLoader}.
	 */
	private static URL getCodeSourceUrl() {
		StackWalker walker = StackWalker.getInstance(Option.RETAIN_CLASS_REFERENCE);

		// @formatter:off
		Class<?> callerClass = walker.walk(stream -> stream
				.map(StackFrame::getDeclaringClass)
				.filter(notFilteringClassLoader)
				.findFirst()
				.get()
			);
		// @formatter:on

		return callerClass.getProtectionDomain().getCodeSource().getLocation();
	}

}
