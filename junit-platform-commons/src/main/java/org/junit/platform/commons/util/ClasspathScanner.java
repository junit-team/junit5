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
import static org.junit.platform.commons.meta.API.Usage.Internal;
import static org.junit.platform.commons.util.BlacklistedExceptions.rethrowIfBlacklisted;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
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

	private static final String ROOT_PACKAGE_NAME = "";

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
		Preconditions.notBlank(packageName, "package name must not be null or blank");

		try {
			return getClassLoader().getResources(packagePath(packageName)).hasMoreElements();
		}
		catch (Exception ex) {
			return false;
		}
	}

	List<Class<?>> scanForClassesInPackage(String basePackageName, Predicate<Class<?>> classFilter) {
		Preconditions.notBlank(basePackageName, "basePackageName must not be null or blank");

		List<File> dirs = allSourceDirsForPackage(basePackageName);
		return allClassesInSourceDirs(dirs, basePackageName, classFilter);
	}

	List<Class<?>> scanForClassesInClasspathRoot(File root, Predicate<Class<?>> classFilter) {
		Preconditions.notNull(root, "root must not be null");
		Preconditions.condition(root.isDirectory(),
			() -> "root must be an existing directory: " + root.getAbsolutePath());

		return findClassesInSourceDirRecursively(ROOT_PACKAGE_NAME, root, classFilter);
	}

	private List<Class<?>> allClassesInSourceDirs(List<File> sourceDirs, String basePackageName,
			Predicate<Class<?>> classFilter) {

		List<Class<?>> classes = new ArrayList<>();
		for (File sourceDir : sourceDirs) {
			classes.addAll(findClassesInSourceDirRecursively(basePackageName, sourceDir, classFilter));
		}
		return classes;
	}

	private List<File> allSourceDirsForPackage(String basePackageName) {
		try {
			Enumeration<URL> resources = getClassLoader().getResources(packagePath(basePackageName));
			List<File> dirs = new ArrayList<>();
			while (resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				dirs.add(new File(resource.getFile()));
			}
			return dirs;
		}
		catch (Exception ex) {
			return Collections.emptyList();
		}
	}

	private List<Class<?>> findClassesInSourceDirRecursively(String packageName, File sourceDir,
			Predicate<Class<?>> classFilter) {

		Preconditions.notNull(classFilter, "classFilter must not be null");

		List<Class<?>> classes = new ArrayList<>();
		collectClassesRecursively(packageName, sourceDir, classFilter, classes);
		return classes;
	}

	private void collectClassesRecursively(String packageName, File sourceDir, Predicate<Class<?>> classFilter,
			List<Class<?>> classes) {

		File[] files = sourceDir.listFiles();
		if (files == null) {
			return;
		}

		for (File file : files) {
			if (isClassFile(file)) {
				processClassFileSafely(packageName, file, classFilter, classes);
			}
			else if (file.isDirectory()) {
				collectClassesRecursively(appendSubpackageName(packageName, file.getName()), file, classFilter,
					classes);
			}
		}
	}

	private void processClassFileSafely(String packageName, File classFile, Predicate<Class<?>> classFilter,
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

	private Optional<Class<?>> loadClassFromFile(String packageName, File classFile) {
		String className = packageName + '.'
				+ classFile.getName().substring(0, classFile.getName().length() - CLASS_FILE_SUFFIX.length());
		return this.loadClass.apply(className, getClassLoader());
	}

	private void handleInternalError(File classFile, Optional<Class<?>> clazz, InternalError ex) {
		if (MALFORMED_CLASS_NAME_ERROR_MESSAGE.equals(ex.getMessage())) {
			logMalformedClassName(classFile, clazz, ex);
		}
		else {
			logGenericFileProcessingException(classFile, ex);
		}
	}

	private void handleThrowable(File classFile, Throwable throwable) {
		rethrowIfBlacklisted(throwable);
		logGenericFileProcessingException(classFile, throwable);
	}

	private void logMalformedClassName(File classFile, Optional<Class<?>> clazz, InternalError ex) {
		try {

			if (clazz.isPresent()) {
				// Do not use getSimpleName() or getCanonicalName() here because they will likely
				// throw another exception due to the underlying error.
				logWarning(ex,
					() -> format("The java.lang.Class loaded from file [%s] has a malformed class name [%s].",
						classFile.getAbsolutePath(), clazz.get().getName()));
			}
			else {
				logWarning(ex, () -> format("The java.lang.Class loaded from file [%s] has a malformed class name.",
					classFile.getAbsolutePath()));
			}
		}
		catch (Throwable t) {
			ex.addSuppressed(t);
			logGenericFileProcessingException(classFile, ex);
		}
	}

	private void logGenericFileProcessingException(File classFile, Throwable throwable) {
		logWarning(throwable, () -> format("Failed to load java.lang.Class for file [%s] during classpath scanning.",
			classFile.getAbsolutePath()));
	}

	private String appendSubpackageName(String packageName, String subpackageName) {
		return (!packageName.isEmpty() ? packageName + "." + subpackageName : subpackageName);
	}

	private ClassLoader getClassLoader() {
		return this.classLoaderSupplier.get();
	}

	private static boolean isClassFile(File file) {
		return file.isFile() && file.getName().endsWith(CLASS_FILE_SUFFIX);
	}

	private static String packagePath(String packageName) {
		return packageName.replace('.', '/');
	}

	private static void logWarning(Throwable throwable, Supplier<String> msgSupplier) {
		LOG.log(Level.WARNING, throwable, msgSupplier);
	}

}
