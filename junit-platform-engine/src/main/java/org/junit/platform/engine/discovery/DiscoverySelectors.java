/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.discovery;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.apiguardian.api.API.Status.STABLE;
import static org.junit.platform.commons.util.CollectionUtils.toUnmodifiableList;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;
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
@API(status = STABLE, since = "1.0")
public final class DiscoverySelectors {

	private static final Pattern fullyQualifiedMethodNamePattern = Pattern.compile("([^#]+)#([^(]+)(?:\\((.*)\\))?");

	///CLOVER:OFF
	private DiscoverySelectors() {
		/* no-op */
	}
	///CLOVER:ON

	/**
	 * Create a {@code UriSelector} for the supplied URI.
	 *
	 * @param uri the URI to select; never {@code null} or blank
	 * @see UriSelector
	 * @see #selectUri(URI)
	 * @see #selectFile(String)
	 * @see #selectFile(File)
	 * @see #selectDirectory(String)
	 * @see #selectDirectory(File)
	 */
	public static UriSelector selectUri(String uri) {
		Preconditions.notBlank(uri, "URI must not be null or blank");
		try {
			return new UriSelector(new URI(uri));
		}
		catch (URISyntaxException ex) {
			throw new PreconditionViolationException("Failed to create a java.net.URI from: " + uri, ex);
		}
	}

	/**
	 * Create a {@code UriSelector} for the supplied {@link URI}.
	 *
	 * @param uri the URI to select; never {@code null}
	 * @see UriSelector
	 * @see #selectUri(String)
	 * @see #selectFile(String)
	 * @see #selectFile(File)
	 * @see #selectDirectory(String)
	 * @see #selectDirectory(File)
	 */
	public static UriSelector selectUri(URI uri) {
		Preconditions.notNull(uri, "URI must not be null");
		return new UriSelector(uri);
	}

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
	 * Create a list of {@code ClasspathRootSelectors} for the supplied
	 * <em>classpath roots</em> (directories or JAR files).
	 *
	 * <p>Since the supplied paths are converted to {@link URI URIs}, the
	 * {@link java.nio.file.FileSystem} that created them must be the
	 * {@linkplain java.nio.file.FileSystems#getDefault() default} or one that
	 * has been created by an installed
	 * {@link java.nio.file.spi.FileSystemProvider}.
	 *
	 * <p>Since {@linkplain org.junit.platform.engine.TestEngine engines} are not
	 * expected to modify the classpath, the classpath roots represented by the
	 * resulting selectors must be on the classpath of the
	 * {@linkplain Thread#getContextClassLoader() context class loader} of the
	 * {@linkplain Thread thread} that uses these selectors.
	 *
	 * @param classpathRoots set of directories and JAR files in the filesystem
	 * that represent classpath roots; never {@code null}
	 * @return a list of selectors for the supplied classpath roots; elements
	 * which do not physically exist in the filesystem will be filtered out
	 * @see ClasspathRootSelector
	 * @see Thread#getContextClassLoader()
	 */
	public static List<ClasspathRootSelector> selectClasspathRoots(Set<Path> classpathRoots) {
		Preconditions.notNull(classpathRoots, "classpathRoots must not be null");

		// @formatter:off
		return classpathRoots.stream()
				.filter(Files::exists)
				.map(Path::toUri)
				.map(ClasspathRootSelector::new)
				// unmodifiable since selectClasspathRoots is a public, non-internal method
				.collect(toUnmodifiableList());
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
	 * <p>Since {@linkplain org.junit.platform.engine.TestEngine engines} are not
	 * expected to modify the classpath, the supplied classpath resource must be
	 * on the classpath of the
	 * {@linkplain Thread#getContextClassLoader() context class loader} of the
	 * {@linkplain Thread thread} that uses the resulting selector.
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
	 * Create a {@code ModulepathSelector} for scanning all modules on the module-path.
	 *
	 * @see ModulepathSelector
	 */
	@API(status = EXPERIMENTAL, since = "1.1")
	public static ModulepathSelector selectModulepath() {
		return new ModulepathSelector();
	}

	/**
	 * Create a {@code ModuleSelector} for the supplied module name.
	 *
	 * <p>The unnamed module is not supported.
	 *
	 * @param moduleName the module name to select; never {@code null} and
	 * never blank
	 * @see ModuleSelector
	 */
	@API(status = EXPERIMENTAL, since = "1.1")
	public static ModuleSelector selectModule(String moduleName) {
		Preconditions.notBlank(moduleName, "Module name must not be null or blank");
		return new ModuleSelector(moduleName.trim());
	}

	/**
	 * Create a {@code PackageSelector} for the supplied package name.
	 *
	 * <p>The default package is represented by an empty string ({@code ""}).
	 *
	 * @param packageName the package name to select; never {@code null} and
	 * never containing whitespace only
	 * @see PackageSelector
	 */
	public static PackageSelector selectPackage(String packageName) {
		Preconditions.notNull(packageName, "Package name must not be null");
		Preconditions.condition(packageName.isEmpty() || !packageName.trim().isEmpty(),
			"Package name must not contain only whitespace");
		return new PackageSelector(packageName.trim());
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
		return new ClassSelector(className);
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
	 * </ul>
	 *
	 * <p>The <em>parameter type list</em> is a comma-separated list of primitive
	 * names or fully qualified class names for the types of parameters accepted
	 * by the method.
	 *
	 * <p>Array parameter types may be specified using either the JVM's internal
	 * String representation (e.g., {@code [[I} for {@code int[][]},
	 * {@code [Ljava.lang.String;} for {@code java.lang.String[]}, etc.) or
	 * <em>source code syntax</em> (e.g., {@code int[][]}, {@code java.lang.String[]},
	 * etc.).
	 *
	 * <h3>Examples</h3>
	 *
	 * <table border="1">
	 * <tr><th>Method</th><th>Fully Qualified Method Name</th></tr>
	 * <tr><td>{@code java.lang.String.chars()}</td><td>{@code java.lang.String#chars}</td></tr>
	 * <tr><td>{@code java.lang.String.chars()}</td><td>{@code java.lang.String#chars()}</td></tr>
	 * <tr><td>{@code java.lang.String.equalsIgnoreCase(String)}</td><td>{@code java.lang.String#equalsIgnoreCase(java.lang.String)}</td></tr>
	 * <tr><td>{@code java.lang.String.substring(int, int)}</td><td>{@code java.lang.String#substring(int, int)}</td></tr>
	 * <tr><td>{@code example.Calc.avg(int[])}</td><td>{@code example.Calc#avg([I)}</td></tr>
	 * <tr><td>{@code example.Calc.avg(int[])}</td><td>{@code example.Calc#avg(int[])}</td></tr>
	 * <tr><td>{@code example.Matrix.multiply(double[][])}</td><td>{@code example.Matrix#multiply([[D)}</td></tr>
	 * <tr><td>{@code example.Matrix.multiply(double[][])}</td><td>{@code example.Matrix#multiply(double[][])}</td></tr>
	 * <tr><td>{@code example.Service.process(String[])}</td><td>{@code example.Service#process([Ljava.lang.String;)}</td></tr>
	 * <tr><td>{@code example.Service.process(String[])}</td><td>{@code example.Service#process(java.lang.String[])}</td></tr>
	 * <tr><td>{@code example.Service.process(String[][])}</td><td>{@code example.Service#process([[Ljava.lang.String;)}</td></tr>
	 * <tr><td>{@code example.Service.process(String[][])}</td><td>{@code example.Service#process(java.lang.String[][])}</td></tr>
	 * </table>
	 *
	 * @param fullyQualifiedMethodName the fully qualified name of the method to select; never
	 * {@code null} or blank
	 * @see MethodSelector
	 */
	public static MethodSelector selectMethod(String fullyQualifiedMethodName) throws PreconditionViolationException {
		Preconditions.notBlank(fullyQualifiedMethodName, "fullyQualifiedMethodName must not be null or blank");

		Matcher matcher = fullyQualifiedMethodNamePattern.matcher(fullyQualifiedMethodName);
		Preconditions.condition(matcher.matches(),
			fullyQualifiedMethodName + " is not a valid fully qualified method name");

		String className = matcher.group(1);
		String methodName = matcher.group(2);
		String methodParameters = matcher.group(3);
		if (StringUtils.isNotBlank(methodParameters)) {
			return selectMethod(className, methodName, methodParameters);
		}
		return selectMethod(className, methodName);
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
		return new MethodSelector(className, methodName);
	}

	/**
	 * Create a {@code MethodSelector} for the supplied class name, method name,
	 * and method parameter types.
	 *
	 * <p>The parameter types {@code String} is typically a comma-separated list
	 * of atomic types, fully qualified class names, or array types; however,
	 * the exact syntax depends on the underlying test engine.
	 *
	 * @param className the fully qualified name of the class in which the method
	 * is declared, or a subclass thereof; never {@code null} or blank
	 * @param methodName the name of the method to select; never {@code null} or blank
	 * @param methodParameterTypes the method parameter types as a single string; never
	 * {@code null} though potentially an empty string if the method does not accept
	 * arguments
	 * @see MethodSelector
	 */
	public static MethodSelector selectMethod(String className, String methodName, String methodParameterTypes) {
		Preconditions.notBlank(className, "Class name must not be null or blank");
		Preconditions.notBlank(methodName, "Method name must not be null or blank");
		Preconditions.notNull(methodParameterTypes, "Parameter types must not be null");
		return new MethodSelector(className, methodName, methodParameterTypes.trim());
	}

	/**
	 * Create a {@code MethodSelector} for the supplied {@link Class} and method name.
	 *
	 * @param javaClass the class in which the method is declared, or a subclass thereof;
	 * never {@code null}
	 * @param methodName the name of the method to select; never {@code null} or blank
	 * @see MethodSelector
	 */
	public static MethodSelector selectMethod(Class<?> javaClass, String methodName) {
		Preconditions.notNull(javaClass, "Class must not be null");
		Preconditions.notBlank(methodName, "Method name must not be null or blank");
		return new MethodSelector(javaClass, methodName);
	}

	/**
	 * Create a {@code MethodSelector} for the supplied {@link Class}, method name,
	 * and method parameter types.
	 *
	 * <p>The parameter types {@code String} is typically a comma-separated list
	 * of atomic types, fully qualified class names, or array types; however,
	 * the exact syntax depends on the underlying test engine.
	 *
	 * @param javaClass the class in which the method is declared, or a subclass thereof;
	 * never {@code null}
	 * @param methodName the name of the method to select; never {@code null} or blank
	 * @param methodParameterTypes the method parameter types as a single string; never
	 * {@code null} though potentially an empty string if the method does not accept
	 * arguments
	 * @see MethodSelector
	 */
	public static MethodSelector selectMethod(Class<?> javaClass, String methodName, String methodParameterTypes) {
		Preconditions.notNull(javaClass, "Class must not be null");
		Preconditions.notBlank(methodName, "Method name must not be null or blank");
		Preconditions.notNull(methodParameterTypes, "Parameter types must not be null");
		return new MethodSelector(javaClass, methodName, methodParameterTypes.trim());
	}

	/**
	 * Create a {@code MethodSelector} for the supplied {@link Class} and {@link Method}.
	 *
	 * @param javaClass the class in which the method is declared, or a subclass thereof;
	 * never {@code null}
	 * @param method the method to select; never {@code null}
	 * @see MethodSelector
	 */
	public static MethodSelector selectMethod(Class<?> javaClass, Method method) {
		Preconditions.notNull(javaClass, "Class must not be null");
		Preconditions.notNull(method, "Method must not be null");
		return new MethodSelector(javaClass, method);
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

}
