/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.discovery;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.junit.gen5.commons.meta.API.Usage.Experimental;
import static org.junit.gen5.engine.discovery.ClassSelector.selectClass;
import static org.junit.gen5.engine.discovery.MethodSelector.selectMethod;
import static org.junit.gen5.engine.discovery.PackageSelector.selectPackage;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.PreconditionViolationException;
import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.DiscoverySelector;

/**
 * Collection of {@code static} factory methods for creating
 * {@link DiscoverySelector DiscoverySelectors} that select elements by name.
 *
 * <p>Consult the documentation for {@link #selectName(String)} for details
 * on what types of names are supported.
 *
 * @since 5.0
 * @see ClassSelector
 * @see MethodSelector
 * @see PackageSelector
 */
@API(Experimental)
public final class NameBasedSelectors {

	///CLOVER:OFF
	private NameBasedSelectors() {
		/* no-op */
	}
	///CLOVER:ON

	/**
	 * Create a {@link DiscoverySelector} for the supplied name.
	 *
	 * <h3>Supported Name Types</h3>
	 * <ul>
	 * <li>package: fully qualified package name</li>
	 * <li>class: fully qualified class name</li>
	 * <li>method: fully qualified method name</li>
	 * </ul>
	 *
	 * <p>The supported format for a <em>fully qualified method name</em> is
	 * {@code [fully qualified class name]#[methodName]}. For example, the
	 * fully qualified name for the {@code chars()} method in
	 * {@code java.lang.String} is {@code java.lang.String#chars}. Names for
	 * overloaded methods are not supported.
	 *
	 * @param name the name to select; never {@code null} or empty
	 * @return an instance of {@link ClassSelector}, {@link MethodSelector}, or
	 * {@link PackageSelector}
	 * @throws PreconditionViolationException if the supplied name is {@code null},
	 * empty, or does not specify a class, method, or package
	 * @see #selectNames(String...)
	 * @see #selectNames(Collection)
	 */
	public static DiscoverySelector selectName(String name) throws PreconditionViolationException {
		Preconditions.notBlank(name, "name must not be null or empty");

		Optional<Class<?>> classOptional = ReflectionUtils.loadClass(name);
		if (classOptional.isPresent()) {
			return selectClass(classOptional.get());
		}

		Optional<Method> methodOptional = ReflectionUtils.loadMethod(name);
		if (methodOptional.isPresent()) {
			Method method = methodOptional.get();
			return selectMethod(method.getDeclaringClass(), method);
		}

		if (ReflectionUtils.isPackage(name)) {
			return selectPackage(name);
		}

		throw new PreconditionViolationException(
			String.format("'%s' specifies neither a class, a method, nor a package.", name));
	}

	/**
	 * Create a list of {@link DiscoverySelector DiscoverySelectors} for the
	 * supplied names.
	 *
	 * @param names the names to select; never {@code null}
	 * @return a list of {@code DiscoverySelectors} for the supplied names;
	 * potentially empty
	 * @see #selectName(String)
	 * @see #selectNames(Collection)
	 */
	public static List<DiscoverySelector> selectNames(String... names) {
		Preconditions.notNull(names, "names array must not be null");
		if (names.length == 0) {
			return emptyList();
		}
		return stream(names).map(NameBasedSelectors::selectName).collect(toList());
	}

	/**
	 * Create a list of {@link DiscoverySelector DiscoverySelectors} for the
	 * supplied names.
	 *
	 * @param names the names to select; never {@code null}
	 * @return a list of {@code DiscoverySelectors} for the supplied names;
	 * potentially empty
	 * @see #selectName(String)
	 * @see #selectNames(String...)
	 */
	public static List<DiscoverySelector> selectNames(Collection<String> names) {
		Preconditions.notNull(names, "names collection must not be null");
		return names.stream().map(NameBasedSelectors::selectName).collect(toList());
	}

}
