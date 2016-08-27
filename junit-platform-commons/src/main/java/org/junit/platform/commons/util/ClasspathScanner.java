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
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.meta.API.Usage.Internal;
import static org.junit.platform.commons.util.BlacklistedExceptions.rethrowIfBlacklisted;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
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

	private static final String CLASS_FILE_SUFFIX = ".class";
	private static final char PACKAGE_SEPARATOR_CHAR = '.';
	private static final String PACKAGE_SEPARATOR_STRING = String.valueOf(PACKAGE_SEPARATOR_CHAR);
	private static final String DEFAULT_PACKAGE_NAME = "";
	private static final char CLASSPATH_RESOURCE_PATH_SEPARATOR = '/';

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

		// TODO Don't convert to URI here
		return findClassesForUri(DEFAULT_PACKAGE_NAME, root.toUri(), classFilter);
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
			return new CloseablePath(fileSystem.getPath(parts[1])) {
				@Override
				public void close() throws IOException {
					fileSystem.close();
				}
			};
		}
	}

	class RegularPathProvider implements PathProvider {
		@Override
		public CloseablePath toPath(URI uri) throws IOException {
			return new CloseablePath(Paths.get(uri)) {
				@Override
				public void close() throws IOException {
					// nothing to do here
				}
			};
		}
	}

	abstract class CloseablePath implements Closeable {

		private final Path path;

		public CloseablePath(Path path) {
			this.path = path;
		}

		public Path getPath() {
			return path;
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

	private List<Class<?>> findClassesInSourcePath(String packageName, Path sourcePath,
			Predicate<Class<?>> classFilter) {
		List<Class<?>> classes = new ArrayList<>();
		try {
			Files.walkFileTree(sourcePath, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					if (isNotPackageInfo(file) && isClassFile(file)) {
						String subpackageName = buildPackageName(packageName, sourcePath, file);
						processClassFileSafely(subpackageName, file, classFilter, classes);
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult visitFileFailed(Path file, IOException exc) {
					logWarning(exc, () -> "I/O error visiting file: " + file);
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
					if (exc != null) {
						logWarning(exc, () -> "I/O error visiting directory: " + dir);
					}
					return FileVisitResult.CONTINUE;
				}

				private String buildPackageName(String rootPackageName, Path rootPath, Path file) {
					Path relativePath = rootPath.relativize(file.getParent());
					String pathSeparator = rootPath.getFileSystem().getSeparator();
					String subpackageName = relativePath.toString().replace(pathSeparator, PACKAGE_SEPARATOR_STRING);
					if (subpackageName.endsWith(pathSeparator)) {
						// Workaround for JDK bug: https://bugs.openjdk.java.net/browse/JDK-8153248
						subpackageName = subpackageName.substring(0, subpackageName.length() - pathSeparator.length());
					}
					if (subpackageName.isEmpty()) {
						return rootPackageName;
					}
					if (rootPackageName.isEmpty()) {
						return subpackageName;
					}
					return rootPackageName + PACKAGE_SEPARATOR_STRING + subpackageName;
				}

				private boolean isNotPackageInfo(Path path) {
					return !path.endsWith("package-info.class");
				}

				private boolean isClassFile(Path path) {
					return Files.isRegularFile(path) && path.getFileName().toString().endsWith(CLASS_FILE_SUFFIX);
				}
			});
		}
		catch (IOException ex) {
			logWarning(ex, () -> "I/O error scanning files in " + sourcePath);
		}
		return classes;
	}

	private void processClassFileSafely(String packageName, Path classFile, Predicate<Class<?>> classFilter,
			List<Class<?>> classes) {

		Optional<Class<?>> clazz = Optional.empty();
		try {
			clazz = loadClassFromFile(packageName, classFile);
			clazz.filter(classFilter).ifPresent(classes::add);
		}
		catch (InternalError internalError) {
			handleInternalError(classFile, clazz, internalError);
		}
		catch (Throwable throwable) {
			handleThrowable(classFile, throwable);
		}
	}

	private Optional<Class<?>> loadClassFromFile(String packageName, Path classFile) {
		String fileName = classFile.getFileName().toString();
		String className = fileName.substring(0, fileName.length() - CLASS_FILE_SUFFIX.length());

		// Handle default package appropriately.
		String fqcn = StringUtils.isBlank(packageName) ? className : packageName + PACKAGE_SEPARATOR_STRING + className;

		return this.loadClass.apply(fqcn, getClassLoader());
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
