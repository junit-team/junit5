/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.commons.util;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.meta.API.Usage.Internal;
import static org.junit.platform.commons.util.BlacklistedExceptions.rethrowIfBlacklisted;
import static org.junit.platform.commons.util.ClassFileVisitor.CLASS_FILE_SUFFIX;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.junit.platform.commons.meta.API;

/**
 * <h3>DISCLAIMER</h3>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 1.0
 */
@API(Internal)
class ClasspathScanner {

	private static final Logger LOG = Logger.getLogger(ClasspathScanner.class.getName());

	private static final String DEFAULT_PACKAGE_NAME = "";
	private static final char CLASSPATH_RESOURCE_PATH_SEPARATOR = '/';
	private static final char PACKAGE_SEPARATOR_CHAR = '.';
	private static final String PACKAGE_SEPARATOR_STRING = String.valueOf(PACKAGE_SEPARATOR_CHAR);

	/** Malformed class name InternalError like reported in #401. */
	private static final String MALFORMED_CLASS_NAME_ERROR_MESSAGE = "Malformed class name";

	private final Supplier<ClassLoader> classLoaderSupplier;

	private final BiFunction<String, ClassLoader, Optional<Class<?>>> loadClass;

	ClasspathScanner(Supplier<ClassLoader> classLoaderSupplier,
			BiFunction<String, ClassLoader, Optional<Class<?>>> loadClass) {

		this.classLoaderSupplier = classLoaderSupplier;
		this.loadClass = loadClass;
	}

	boolean isPackage(String packageName) {
		assertPackageNameIsPlausible(packageName);

		try {
			return packageName.length() == 0 // default package
					|| getClassLoader().getResources(packagePath(packageName.trim())).hasMoreElements();
		}
		catch (Exception ex) {
			return false;
		}
	}

	List<Class<?>> scanForClassesInPackage(String basePackageName, Predicate<Class<?>> classFilter) {
		assertPackageNameIsPlausible(basePackageName);
		Preconditions.notNull(classFilter, "classFilter must not be null");
		basePackageName = basePackageName.trim();

		return findClassesForUris(getRootUrisForPackage(basePackageName), basePackageName, classFilter);
	}

	List<Class<?>> scanForClassesInClasspathRoot(Path root, Predicate<Class<?>> classFilter) {
		Preconditions.notNull(root, "root must not be null");
		Preconditions.condition(Files.isDirectory(root),
			() -> "root must be an existing directory: " + root.toAbsolutePath());
		Preconditions.notNull(classFilter, "classFilter must not be null");

		return findClassesInSourcePath(DEFAULT_PACKAGE_NAME, root, classFilter);
	}

	/**
	 * Recursively scan for classes in all of the supplied source directories.
	 */
	private List<Class<?>> findClassesForUris(List<URI> sourceUris, String basePackageName,
			Predicate<Class<?>> classFilter) {

		// @formatter:off
		return sourceUris.stream()
				.map(uri -> findClassesForUri(basePackageName, uri, classFilter))
				.flatMap(Collection::stream)
				.distinct()
				.collect(toList());
		// @formatter:on
	}

	interface PathProvider {

		CloseablePath toPath(URI uri) throws IOException;

	}

	class JarPathProvider implements PathProvider {

		@Override
		public CloseablePath toPath(URI uri) throws IOException {
			String[] parts = uri.toString().split("!");
			FileSystem fileSystem = FileSystems.newFileSystem(URI.create(parts[0]), emptyMap());
			return new CloseablePath(fileSystem.getPath(parts[1]), fileSystem);
		}
	}

	class RegularPathProvider implements PathProvider {
		@Override
		public CloseablePath toPath(URI uri) throws IOException {
			Path path = Paths.get(uri);
			return new CloseablePath(path, () -> {
			});
		}
	}

	final static class CloseablePath implements Closeable {

		private final Path path;
		private final Closeable delegate;

		public CloseablePath(Path path, Closeable delegate) {
			this.path = path;
			this.delegate = delegate;
		}

		public Path getPath() {
			return path;
		}

		@Override
		public void close() throws IOException {
			delegate.close();
		}
	}

	private List<Class<?>> findClassesForUri(String packageName, URI sourceUri, Predicate<Class<?>> classFilter) {
		PathProvider provider = determineProvider(sourceUri);
		try (CloseablePath closeablePath = provider.toPath(sourceUri)) {
			return findClassesInSourcePath(packageName, closeablePath.getPath(), classFilter);
		}
		catch (Exception ex) {
			logWarning(ex, () -> "Error reading Path from URI " + sourceUri);
			return emptyList();
		}
	}

	private PathProvider determineProvider(URI sourceUri) {
		if ("jar".equals(sourceUri.getScheme())) {
			return new JarPathProvider();
		}
		return new RegularPathProvider();
	}

	private List<Class<?>> findClassesInSourcePath(String rootPackageName, Path rootDir,
			Predicate<Class<?>> classFilter) {
		List<Class<?>> classes = new ArrayList<>();
		try {
			Files.walkFileTree(rootDir, new ClassFileVisitor(
				classFile -> processClassFileSafely(rootPackageName, rootDir, classFilter, classes, classFile)));
		}
		catch (IOException ex) {
			logWarning(ex, () -> "I/O error scanning files in " + rootDir);
		}
		return classes;
	}

	private void processClassFileSafely(String rootPackageName, Path rootDir, Predicate<Class<?>> classFilter,
			List<Class<?>> classes, Path classFile) {
		Optional<Class<?>> clazz = Optional.empty();
		try {
			String fullyQualifiedClassName = determineFullyQualifiedClassName(rootPackageName, rootDir, classFile);
			clazz = this.loadClass.apply(fullyQualifiedClassName, getClassLoader());
			clazz.filter(classFilter).ifPresent(classes::add);
		}
		catch (InternalError internalError) {
			handleInternalError(classFile, clazz, internalError);
		}
		catch (Throwable throwable) {
			handleThrowable(classFile, throwable);
		}
	}

	private String determineFullyQualifiedClassName(String rootPackageName, Path rootDir, Path classFile) {
		// @formatter:off
		return Stream.of(
					rootPackageName,
					determineSubpackageName(rootDir, classFile),
					determineSimpleClassName(classFile)
				)
				.filter(value -> !value.isEmpty()) // Handle default package appropriately.
				.collect(joining(PACKAGE_SEPARATOR_STRING));
		// @formatter:on
	}

	private String determineSimpleClassName(Path classFile) {
		String fileName = classFile.getFileName().toString();
		return fileName.substring(0, fileName.length() - CLASS_FILE_SUFFIX.length());
	}

	private String determineSubpackageName(Path rootDir, Path classFile) {
		Path relativePath = rootDir.relativize(classFile.getParent());
		String pathSeparator = rootDir.getFileSystem().getSeparator();
		String subpackageName = relativePath.toString().replace(pathSeparator, PACKAGE_SEPARATOR_STRING);
		if (subpackageName.endsWith(pathSeparator)) {
			// Workaround for JDK bug: https://bugs.openjdk.java.net/browse/JDK-8153248
			subpackageName = subpackageName.substring(0, subpackageName.length() - pathSeparator.length());
		}
		return subpackageName;
	}

	private void handleInternalError(Path classFile, Optional<Class<?>> clazz, InternalError ex) {
		if (MALFORMED_CLASS_NAME_ERROR_MESSAGE.equals(ex.getMessage())) {
			logMalformedClassName(classFile, clazz, ex);
		}
		else {
			logGenericFileProcessingException(classFile, ex);
		}
	}

	private void handleThrowable(Path classFile, Throwable throwable) {
		rethrowIfBlacklisted(throwable);
		logGenericFileProcessingException(classFile, throwable);
	}

	private void logMalformedClassName(Path classFile, Optional<Class<?>> clazz, InternalError ex) {
		try {

			if (clazz.isPresent()) {
				// Do not use getSimpleName() or getCanonicalName() here because they will likely
				// throw another exception due to the underlying error.
				logWarning(ex,
					() -> format("The java.lang.Class loaded from path [%s] has a malformed class name [%s].",
						classFile.toAbsolutePath(), clazz.get().getName()));
			}
			else {
				logWarning(ex, () -> format("The java.lang.Class loaded from path [%s] has a malformed class name.",
					classFile.toAbsolutePath()));
			}
		}
		catch (Throwable t) {
			ex.addSuppressed(t);
			logGenericFileProcessingException(classFile, ex);
		}
	}

	private void logGenericFileProcessingException(Path classFile, Throwable throwable) {
		logWarning(throwable, () -> format("Failed to load java.lang.Class for path [%s] during classpath scanning.",
			classFile.toAbsolutePath()));
	}

	private ClassLoader getClassLoader() {
		return this.classLoaderSupplier.get();
	}

	private static void assertPackageNameIsPlausible(String packageName) {
		Preconditions.notNull(packageName, "package name must not be null");
		Preconditions.condition(DEFAULT_PACKAGE_NAME.equals(packageName) || packageName.trim().length() != 0,
			"package name must not contain only whitespace");
	}

	private static String packagePath(String packageName) {
		return packageName.replace(PACKAGE_SEPARATOR_CHAR, CLASSPATH_RESOURCE_PATH_SEPARATOR);
	}

	private List<URI> getRootUrisForPackage(String packageName) {
		try {
			Enumeration<URL> resources = getClassLoader().getResources(packagePath(packageName));
			List<URI> uris = new ArrayList<>();
			while (resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				uris.add(resource.toURI());
			}
			return uris;
		}
		catch (Exception ex) {
			logWarning(ex, () -> "Error reading directories from class loader for package " + packageName);
			return emptyList();
		}
	}

	private static void logWarning(Throwable throwable, Supplier<String> msgSupplier) {
		LOG.log(Level.WARNING, throwable, msgSupplier);
	}

}
