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

import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
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
 * @see ClasspathRootSelector
 * @see ClasspathResourceSelector
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
	 * Create a {@code FileSelector} for the supplied file path.
	 *
	 * <p>This method selects the file using the supplied path <em>as is</em>,
	 * without verifying if the file exists.
	 *
	 * @param path the path to the file to select; never {@code null} or blank
	 * @see FileSelector
	 * @see #selectFile(File)
	 * @see #selectDirectory(String)
	 * @see #selectDirectory(File)
	 */
	public static FileSelector selectFile(String path) {
		Preconditions.notBlank(path, "File path must not be null or blank");
		return new FileSelector(path);
	}

	/**
	 * Create a {@code FileSelector} for the supplied {@linkplain File file}.
	 *
	 * <p>This method selects the file in its {@linkplain File#getCanonicalPath()
	 * canonical} form and throws a {@link PreconditionViolationException} if the
	 * file does not exist.
	 *
	 * @param file the file to select; never {@code null}
	 * @see FileSelector
	 * @see #selectFile(String)
	 * @see #selectDirectory(String)
	 * @see #selectDirectory(File)
	 */
	public static FileSelector selectFile(File file) {
		Preconditions.notNull(file, "File must not be null");
		Preconditions.condition(file.isFile(),
			() -> String.format("The supplied java.io.File [%s] must represent an existing file", file));
		try {
			return new FileSelector(file.getCanonicalPath());
		}
		catch (IOException ex) {
			throw new PreconditionViolationException("Failed to retrieve canonical path for file: " + file, ex);
		}
	}

	/**
	 * Create a {@code DirectorySelector} for the supplied directory path.
	 *
	 * <p>This method selects the directory using the supplied path <em>as is</em>,
	 * without verifying if the directory exists.
	 *
	 * @param path the path to the directory to select; never {@code null} or blank
	 * @see DirectorySelector
	 * @see #selectDirectory(File)
	 * @see #selectFile(String)
	 * @see #selectFile(File)
	 */
	public static DirectorySelector selectDirectory(String path) {
		Preconditions.notBlank(path, "Directory path must not be null or blank");
		return new DirectorySelector(path);
	}

	/**
	 * Create a {@code DirectorySelector} for the supplied {@linkplain File directory}.
	 *
	 * <p>This method selects the directory in its {@linkplain File#getCanonicalPath()
	 * canonical} form and throws a {@link PreconditionViolationException} if the
	 * directory does not exist.
	 *
	 * @param directory the directory to select; never {@code null}
	 * @see DirectorySelector
	 * @see #selectDirectory(String)
	 * @see #selectFile(String)
	 * @see #selectFile(File)
	 */
	public static DirectorySelector selectDirectory(File directory) {
		Preconditions.notNull(directory, "Directory must not be null");
		Preconditions.condition(directory.isDirectory(),
			() -> String.format("The supplied java.io.File [%s] must represent an existing directory", directory));
		try {
			return new DirectorySelector(directory.getCanonicalPath());
		}
		catch (IOException ex) {
			throw new PreconditionViolationException("Failed to retrieve canonical path for directory: " + directory,
				ex);
		}
	}

	/**
	 * Create a list of {@code ClasspathRootSelectors} for the supplied {@code directories}.
	 *
	 * @param directories set of directories in the filesystem that represent classpath roots;
	 * never {@code null}
	 * @return a list of selectors for the supplied directories; directories which
	 * do not physically exist in the filesystem will be filtered out
	 * @see ClasspathRootSelector
	 */
	public static List<ClasspathRootSelector> selectClasspathRoots(Set<File> directories) {
		Preconditions.notNull(directories, "directories must not be null");

		// @formatter:off
		return directories.stream()
				.filter(File::isDirectory)
				.map(ClasspathRootSelector::new)
				.collect(toList());
		// @formatter:on
	}

	/**
	 * Create a {@code ClasspathResourceSelector} for the supplied classpath
	 * resource name.
	 *
	 * <p>The name of a <em>classpath resource</em> must follow the semantics
	 * for resource paths as defined in {@link ClassLoader#getResource(String)}.
	 *
	 * <p>If the supplied classpath resource name is prefixed with a slash
	 * ({@code /}), the slash will be removed.
	 *
	 * @param classpathResourceName the name of the classpath resource; never
	 * {@code null} or blank
	 * @see ClasspathResourceSelector
	 * @see ClassLoader#getResource(String)
	 * @see ClassLoader#getResourceAsStream(String)
	 * @see ClassLoader#getResources(String)
	 */
	public static ClasspathResourceSelector selectClasspathResource(String classpathResourceName) {
		Preconditions.notBlank(classpathResourceName, "Classpath resource name must not be null or blank");
		return new ClasspathResourceSelector(classpathResourceName);
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
		Preconditions.notBlank(className, "Class name must not be null or blank");
		return new ClassSelector(loadClass(className));
	}

	/**
	 * Create a {@code MethodSelector} for the supplied <em>fully qualified
	 * method name</em>.
	 *
	 * <p>The following formats are supported.
	 *
	 * <ul>
	 * <li>{@code [fully qualified class name]#[methodName]}</li>
	 * <li>{@code [fully qualified class name]#[methodName](parameter type list)}
	 * <ul><li>The <em>parameter type list</em> is a comma-separated list of
	 * fully qualified class names for the types of parameters accepted by
	 * the method.</li></ul>
	 * </li>
	 * </ul>
	 *
	 * <h3>Examples</h3>
	 *
	 * <table border="1">
	 * <tr><th>Method</th><th>Fully Qualified Method Name</th></tr>
	 * <tr><td>{@link String#chars()}</td><td>{@code java.lang.String#chars}</td></tr>
	 * <tr><td>{@link String#chars()}</td><td>{@code java.lang.String#chars()}</td></tr>
	 * <tr><td>{@link String#equalsIgnoreCase(String)}</td><td>{@code java.lang.String#equalsIgnoreCase(java.lang.String)}</td></tr>
	 * <tr><td>{@link String#substring(int, int)}</td><td>{@code java.lang.String#substring(int, int)}</td></tr>
	 * </table>
	 *
	 * @param name the fully qualified name of the method to select; never
	 * {@code null} or blank
	 * @throws PreconditionViolationException if the supplied name is {@code null},
	 * blank, or does not specify a unique method
	 * @see MethodSelector
	 */
	public static MethodSelector selectMethod(String name) throws PreconditionViolationException {
		Preconditions.notBlank(name, "Method name must not be null or blank");

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
