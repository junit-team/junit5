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

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.util.BlacklistedExceptions.rethrowIfBlacklisted;

import java.net.URI;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;

import org.junit.platform.commons.function.Try;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

/**
 * <h3>DISCLAIMER</h3>
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

		PackageUtils.assertPackageNameIsValid(basePackageName);
		Preconditions.notNull(classFilter, "classFilter must not be null");

		try (ScanResult scan = new ClassGraph().addClassLoader(getClassLoader()).whitelistPackages(
			basePackageName.trim()).ignoreClassVisibility().disableNestedJarScanning().scan()) {
			return filterScanResult(scan, classFilter);
		}
	}

	List<Class<?>> scanForClassesInClasspathRoot(URI root, ClassFilter classFilter) {
		Preconditions.notNull(root, "root must not be null");
		Preconditions.notNull(classFilter, "classFilter must not be null");

		try (ScanResult scan = new ClassGraph().addClassLoader(getClassLoader()).overrideClasspath(
			root).ignoreClassVisibility().disableNestedJarScanning().scan()) {
			return filterScanResult(scan, classFilter);
		}
	}

	private List<Class<?>> filterScanResult(ScanResult scanResult, ClassFilter classFilter) {
		return scanResult.getAllClasses().filter(info -> classFilter.match(info.getName())).stream().map(
			classInfo -> loadClassSafely(classInfo, classFilter)).filter(Objects::nonNull).collect(toList());
	}

	private Class<?> loadClassSafely(ClassInfo classInfo, ClassFilter classFilter) {
		try {
			return loadClass.apply(classInfo.getName(), getClassLoader()).toOptional().filter(classFilter) // Always use ".filter(classFilter)" to include future predicates.
					.orElse(null);
		}
		catch (InternalError internalError) {
			handleInternalError(classInfo, internalError);
		}
		catch (Throwable throwable) {
			handleThrowable(classInfo, throwable);
		}
		return null;
	}

	private void handleInternalError(ClassInfo classInfo, InternalError ex) {
		if (MALFORMED_CLASS_NAME_ERROR_MESSAGE.equals(ex.getMessage())) {
			logMalformedClassName(classInfo, ex);
		}
		else {
			logGenericFileProcessingException(classInfo, ex);
		}
	}

	private void handleThrowable(ClassInfo classInfo, Throwable throwable) {
		rethrowIfBlacklisted(throwable);
		logGenericFileProcessingException(classInfo, throwable);
	}

	private void logMalformedClassName(ClassInfo classInfo, InternalError ex) {
		try {
			logger.debug(ex, () -> format("The java.lang.Class loaded from path [%s] has a malformed class name [%s].",
				classInfo.getResource(), classInfo.getName()));
		}
		catch (Throwable t) {
			rethrowIfBlacklisted(t);
			ex.addSuppressed(t);
			logGenericFileProcessingException(classInfo, ex);
		}
	}

	private void logGenericFileProcessingException(ClassInfo classInfo, Throwable throwable) {
		logger.debug(throwable, () -> format("Failed to load java.lang.Class for path [%s] during classpath scanning.",
			classInfo.getResource()));
	}

	private ClassLoader getClassLoader() {
		return this.classLoaderSupplier.get();
	}

}
