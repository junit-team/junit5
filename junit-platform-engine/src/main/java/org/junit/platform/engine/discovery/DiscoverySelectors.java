/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.discovery;

import static java.util.Arrays.stream;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.meta.API.Usage.Deprecated;
import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.UniqueId;

/**
 * Collection of {@code static} factory methods for creating
 * {@link DiscoverySelector DiscoverySelectors}.
 *
 * @since 1.0
 * @see ClasspathSelector
 * @see ClassSelector
 * @see MethodSelector
 * @see PackageSelector
 */
@API(Experimental)
public final class DiscoverySelectors {

	///CLOVER:OFF
	private DiscoverySelectors() {
		/* no-op */
	}
	///CLOVER:ON

	/**
	 * Create a list of {@code ClasspathSelectors} for the supplied {@code directories}.
	 *
	 * @param directories set of directories in the filesystem that represent classpath roots;
	 * never {@code null}
	 * @return a list of selectors for the supplied directories; directories which
	 * do not physically exist in the filesystem will be filtered out
	 * @see ClasspathSelector
	 */
	public static List<DiscoverySelector> selectClasspathRoots(Set<File> directories) {
		Preconditions.notNull(directories, "directories must not be null");

		// @formatter:off
		return directories.stream()
				.filter(File::isDirectory)
				.map(ClasspathSelector::new)
				.collect(toList());
		// @formatter:on
	}

	/**
	 * Create a {@code PackageSelector} for the supplied package name.
	 *
	 * @param packageName the package name to select; never {@code null} or blank
	 * @see PackageSelector
	 */
	public static PackageSelector selectPackage(String packageName) {
		Preconditions.notBlank(packageName, "Package name must not be null or blank");
		return new PackageSelector(packageName);
	}

	/**
	 * Create a {@code ClassSelector} for the supplied {@link Class}.
	 *
	 * @param clazz the class to select; never {@code null}
	 * @see ClassSelector
	 */
	public static ClassSelector selectClass(Class<?> clazz) {
		Preconditions.notNull(clazz, "Class must not be null");
		return new ClassSelector(clazz);
	}

	/**
	 * Create a {@code ClassSelector} for the supplied class name.
	 *
	 * @param className the fully qualified name of the class to select;
	 * never {@code null} or blank
	 * @see ClassSelector
	 */
	public static ClassSelector selectClass(String className) {
		Preconditions.notBlank(className, "className must not be null or blank");

		return selectClass(ReflectionUtils.loadClass(className).orElseThrow(
			() -> new PreconditionViolationException("Could not load class with name: " + className)));
	}

	/**
	 * Create a {@code MethodSelector} for the supplied <em>fully qualified
	 * method name</em>.
	 *
	 * <p>The supported format for a <em>fully qualified method name</em> is
	 * {@code [fully qualified class name]#[methodName]}. For example, the
	 * fully qualified name for the {@code chars()} method in
	 * {@code java.lang.String} is {@code "java.lang.String#chars"}.
	 *
	 * <p><strong>WARNING</strong>: Overloaded methods and methods that accept
	 * arguments are not currently supported.
	 *
	 * @param name the fully qualified name of the method to select; never
	 * {@code null} or blank
	 * @throws PreconditionViolationException if the supplied name is {@code null},
	 * blank, or does not specify a unique method
	 * @see MethodSelector
	 */
	public static MethodSelector selectMethod(String name) throws PreconditionViolationException {
		Preconditions.notBlank(name, "name must not be null or blank");

		Optional<Method> methodOptional = ReflectionUtils.loadMethod(name);
		Method method = methodOptional.orElseThrow(() -> new PreconditionViolationException(
			String.format("'%s' could not be resolved to a unique method", name)));

		return selectMethod(method.getDeclaringClass(), method);
	}

	/**
	 * Create a {@code MethodSelector} for the supplied class name and method name.
	 *
	 * @param className the fully qualified name of the class in which the method
	 * is declared, or a subclass thereof; never {@code null} or blank
	 * @param methodName the name of the method to select; never {@code null} or blank
	 * @see MethodSelector
	 */
	public static MethodSelector selectMethod(String className, String methodName) {
		Preconditions.notBlank(className, "Class name must not be null or blank");
		Preconditions.notBlank(methodName, "Method name must not be null or blank");
		Class<?> clazz = loadClass(className);
		return selectMethod(clazz, findMethod(clazz, methodName));
	}

	/**
	 * Create a {@code MethodSelector} for the supplied {@link Class} and method name.
	 *
	 * @param clazz the class in which the method is declared, or a subclass thereof;
	 * never {@code null}
	 * @param methodName the name of the method to select; never {@code null} or blank
	 * @see MethodSelector
	 */
	public static MethodSelector selectMethod(Class<?> clazz, String methodName) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notBlank(methodName, "Method name must not be null or blank");
		return selectMethod(clazz, findMethod(clazz, methodName));
	}

	/**
	 * Create a {@code MethodSelector} for the supplied {@link Class} and {@link Method}.
	 *
	 * @param clazz the class in which the method is declared, or a subclass thereof;
	 * never {@code null}
	 * @param method the method to select; never {@code null}
	 * @see MethodSelector
	 */
	public static MethodSelector selectMethod(Class<?> clazz, Method method) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(method, "Method must not be null");
		return new MethodSelector(clazz, method);
	}

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
	 * @param name the name to select; never {@code null} or blank
	 * @return an instance of {@link ClassSelector}, {@link MethodSelector}, or
	 * {@link PackageSelector}
	 * @throws PreconditionViolationException if the supplied name is {@code null},
	 * blank, or does not specify a class, method, or package
	 * @see #selectPackage(String)
	 * @see #selectClass(String)
	 * @see #selectMethod(String)
	 * @deprecated This method will be removed in 5.0 M3; use
	 * {@link #selectPackage(String)}, {@link #selectClass(String)}, or
	 * {@link #selectMethod(String)} instead.
	 */
	@Deprecated
	@API(Deprecated)
	public static DiscoverySelector selectName(String name) throws PreconditionViolationException {
		Preconditions.notBlank(name, "name must not be null or blank");

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
	 * <p>Consult the documentation for {@link #selectName(String)} for details
	 * on what types of names are supported.
	 *
	 * @param names the names to select; never {@code null}
	 * @return a list of {@code DiscoverySelectors} for the supplied names;
	 * potentially empty
	 * @see #selectPackage(String)
	 * @see #selectClass(String)
	 * @see #selectMethod(String)
	 * @deprecated This method will be removed in 5.0 M3; use
	 * {@link #selectPackage(String)}, {@link #selectClass(String)}, or
	 * {@link #selectMethod(String)} instead.
	 */
	@Deprecated
	@API(Deprecated)
	public static List<DiscoverySelector> selectNames(String... names) {
		Preconditions.notNull(names, "names array must not be null");
		if (names.length == 0) {
			return emptyList();
		}
		return stream(names).map(DiscoverySelectors::selectName).collect(toList());
	}

	/**
	 * Create a list of {@link DiscoverySelector DiscoverySelectors} for the
	 * supplied names.
	 *
	 * <p>Consult the documentation for {@link #selectName(String)} for details
	 * on what types of names are supported.
	 *
	 * @param names the names to select; never {@code null}
	 * @return a list of {@code DiscoverySelectors} for the supplied names;
	 * potentially empty
	 * @see #selectPackage(String)
	 * @see #selectClass(String)
	 * @see #selectMethod(String)
	 * @deprecated This method will be removed in 5.0 M3; use
	 * {@link #selectPackage(String)}, {@link #selectClass(String)}, or
	 * {@link #selectMethod(String)} instead.
	 */
	@Deprecated
	@API(Deprecated)
	public static List<DiscoverySelector> selectNames(Collection<String> names) {
		Preconditions.notNull(names, "names collection must not be null");
		return names.stream().map(DiscoverySelectors::selectName).collect(toList());
	}

	/**
	 * Create a {@code UniqueIdSelector} for the supplied {@link UniqueId}.
	 *
	 * @param uniqueId the {@code UniqueId} to select; never {@code null}
	 * @see UniqueIdSelector
	 */
	public static UniqueIdSelector selectUniqueId(UniqueId uniqueId) {
		Preconditions.notNull(uniqueId, "UniqueId must not be null");
		return new UniqueIdSelector(uniqueId);
	}

	/**
	 * Create a {@code UniqueIdSelector} for the supplied unique ID.
	 *
	 * @param uniqueId the unique ID to select; never {@code null} or blank
	 * @see UniqueIdSelector
	 */
	public static UniqueIdSelector selectUniqueId(String uniqueId) {
		Preconditions.notBlank(uniqueId, "Unique ID must not be null or blank");
		return new UniqueIdSelector(UniqueId.parse(uniqueId));
	}

	private static Class<?> loadClass(String className) {
		return ReflectionUtils.loadClass(className).orElseThrow(
			() -> new PreconditionViolationException("Could not load class with name: " + className));
	}

	private static Method findMethod(Class<?> clazz, String methodName) {
		return ReflectionUtils.findMethod(clazz, methodName).orElseThrow(() -> new PreconditionViolationException(
			String.format("Could not find method with name [%s] in class [%s].", methodName, clazz.getName())));
	}

}
