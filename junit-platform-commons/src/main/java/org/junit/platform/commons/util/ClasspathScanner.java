/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static java.lang.String.format;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.util.ClassFileVisitor.CLASS_FILE_SUFFIX;
import static org.junit.platform.commons.util.StringUtils.isNotBlank;

import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.function.Try;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

/**
 * <h2>DISCLAIMER</h2>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 1.0
 */
class ClasspathScanner {

	private static final Logger logger = LoggerFactory.getLogger(ClasspathScanner.class);

	private static final char CLASSPATH_RESOURCE_PATH_SEPARATOR = '/';
	private static final String CLASSPATH_RESOURCE_PATH_SEPARATOR_STRING = String.valueOf(
		CLASSPATH_RESOURCE_PATH_SEPARATOR);
	private static final char PACKAGE_SEPARATOR_CHAR = '.';
	private static final String PACKAGE_SEPARATOR_STRING = String.valueOf(PACKAGE_SEPARATOR_CHAR);

	/**
	 * Malformed class name InternalError like reported in #401.
	 */
	private static final String MALFORMED_CLASS_NAME_ERROR_MESSAGE = "Malformed class name";

	private final Supplier<ClassLoader> classLoaderSupplier;

	private final BiFunction<String, ClassLoader, Try<Class<?>>> loadClass;

	ClasspathScanner(Supplier<ClassLoader> classLoaderSupplier,
			BiFunction<String, ClassLoader, Try<Class<?>>> loadClass) {

		this.classLoaderSupplier = classLoaderSupplier;
		this.loadClass = loadClass;
	}

	List<Class<?>> scanForClassesInPackage(String basePackageName, ClassFilter classFilter) {
		Preconditions.condition(
			PackageUtils.DEFAULT_PACKAGE_NAME.equals(basePackageName) || isNotBlank(basePackageName),
			"basePackageName must not be null or blank");
		Preconditions.notNull(classFilter, "classFilter must not be null");
		basePackageName = basePackageName.trim();

		return findClassesForUris(getRootUrisForPackageNameOnClassPathAndModulePath(basePackageName), basePackageName,
			classFilter);
	}

	List<Class<?>> scanForClassesInClasspathRoot(URI root, ClassFilter classFilter) {
		Preconditions.notNull(root, "root must not be null");
		Preconditions.notNull(classFilter, "classFilter must not be null");

		return findClassesForUri(root, PackageUtils.DEFAULT_PACKAGE_NAME, classFilter);
	}

	/**
	 * Recursively scan for classes in all of the supplied source directories.
	 */
	private List<Class<?>> findClassesForUris(List<URI> baseUris, String basePackageName, ClassFilter classFilter) {
		// @formatter:off
		return baseUris.stream()
				.map(baseUri -> findClassesForUri(baseUri, basePackageName, classFilter))
				.flatMap(Collection::stream)
				.distinct()
				.collect(toList());
		// @formatter:on
	}

	private List<Class<?>> findClassesForUri(URI baseUri, String basePackageName, ClassFilter classFilter) {
		try (CloseablePath closeablePath = CloseablePath.create(baseUri)) {
			Path baseDir = closeablePath.getPath();
			return findClassesForPath(baseDir, basePackageName, classFilter);
		}
		catch (PreconditionViolationException ex) {
			throw ex;
		}
		catch (Exception ex) {
			logger.warn(ex, () -> "Error scanning files for URI " + baseUri);
			return emptyList();
		}
	}

	private List<Class<?>> findClassesForPath(Path baseDir, String basePackageName, ClassFilter classFilter) {
		Preconditions.condition(Files.exists(baseDir), () -> "baseDir must exist: " + baseDir);
		List<Class<?>> classes = new ArrayList<>();
		try {
			Files.walkFileTree(baseDir, new ClassFileVisitor(
				classFile -> processClassFileSafely(baseDir, basePackageName, classFilter, classFile, classes::add)));
		}
		catch (IOException ex) {
			logger.warn(ex, () -> "I/O error scanning files in " + baseDir);
		}
		return classes;
	}

	private void processClassFileSafely(Path baseDir, String basePackageName, ClassFilter classFilter, Path classFile,
			Consumer<Class<?>> classConsumer) {
		try {
			String fullyQualifiedClassName = determineFullyQualifiedClassName(baseDir, basePackageName, classFile);
			if (classFilter.match(fullyQualifiedClassName)) {
				try {
					// @formatter:off
					loadClass.apply(fullyQualifiedClassName, getClassLoader())
							.toOptional()
							.filter(classFilter) // Always use ".filter(classFilter)" to include future predicates.
							.ifPresent(classConsumer);
					// @formatter:on
				}
				catch (InternalError internalError) {
					handleInternalError(classFile, fullyQualifiedClassName, internalError);
				}
			}
		}
		catch (Throwable throwable) {
			handleThrowable(classFile, throwable);
		}
	}

	private String determineFullyQualifiedClassName(Path baseDir, String basePackageName, Path classFile) {
		// @formatter:off
		return Stream.of(
					basePackageName,
					determineSubpackageName(baseDir, classFile),
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

	private String determineSubpackageName(Path baseDir, Path classFile) {
		Path relativePath = baseDir.relativize(classFile.getParent());
		String pathSeparator = baseDir.getFileSystem().getSeparator();
		String subpackageName = relativePath.toString().replace(pathSeparator, PACKAGE_SEPARATOR_STRING);
		if (subpackageName.endsWith(pathSeparator)) {
			// Workaround for JDK bug: https://bugs.openjdk.java.net/browse/JDK-8153248
			subpackageName = subpackageName.substring(0, subpackageName.length() - pathSeparator.length());
		}
		return subpackageName;
	}

	private void handleInternalError(Path classFile, String fullyQualifiedClassName, InternalError ex) {
		if (MALFORMED_CLASS_NAME_ERROR_MESSAGE.equals(ex.getMessage())) {
			logMalformedClassName(classFile, fullyQualifiedClassName, ex);
		}
		else {
			logGenericFileProcessingException(classFile, ex);
		}
	}

	private void handleThrowable(Path classFile, Throwable throwable) {
		UnrecoverableExceptions.rethrowIfUnrecoverable(throwable);
		logGenericFileProcessingException(classFile, throwable);
	}

	private void logMalformedClassName(Path classFile, String fullyQualifiedClassName, InternalError ex) {
		try {
			logger.debug(ex, () -> format("The java.lang.Class loaded from path [%s] has a malformed class name [%s].",
				classFile.toAbsolutePath(), fullyQualifiedClassName));
		}
		catch (Throwable t) {
			UnrecoverableExceptions.rethrowIfUnrecoverable(t);
			ex.addSuppressed(t);
			logGenericFileProcessingException(classFile, ex);
		}
	}

	private void logGenericFileProcessingException(Path classFile, Throwable throwable) {
		logger.debug(throwable, () -> format("Failed to load java.lang.Class for path [%s] during classpath scanning.",
			classFile.toAbsolutePath()));
	}

	private ClassLoader getClassLoader() {
		return this.classLoaderSupplier.get();
	}

	private List<URI> getRootUrisForPackageNameOnClassPathAndModulePath(String basePackageName) {
		Set<URI> uriSet = new LinkedHashSet<>(getRootUrisForPackage(basePackageName));
		if (!basePackageName.isEmpty() && !basePackageName.endsWith(PACKAGE_SEPARATOR_STRING)) {
			getRootUrisForPackage(basePackageName + PACKAGE_SEPARATOR_STRING).stream() //
					.map(ClasspathScanner::removeTrailingClasspathResourcePathSeparator) //
					.forEach(uriSet::add);
		}
		return new ArrayList<>(uriSet);
	}

	private static URI removeTrailingClasspathResourcePathSeparator(URI uri) {
		String string = uri.toString();
		if (string.endsWith(CLASSPATH_RESOURCE_PATH_SEPARATOR_STRING)) {
			return URI.create(string.substring(0, string.length() - 1));
		}
		return uri;
	}

	private static String packagePath(String packageName) {
		if (packageName.isEmpty()) {
			return "";
		}
		return packageName.replace(PACKAGE_SEPARATOR_CHAR, CLASSPATH_RESOURCE_PATH_SEPARATOR);
	}

	private List<URI> getRootUrisForPackage(String basePackageName) {
		try {
			Enumeration<URL> resources = getClassLoader().getResources(packagePath(basePackageName));
			List<URI> uris = new ArrayList<>();
			while (resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				uris.add(resource.toURI());
			}
			return uris;
		}
		catch (Exception ex) {
			logger.warn(ex, () -> "Error reading URIs from class loader for base package " + basePackageName);
			return emptyList();
		}
	}

}
