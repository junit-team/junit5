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

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

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

	private static final String CLASS_FILE_SUFFIX = ".class";

	private final Supplier<ClassLoader> classLoaderSupplier;

	private final BiFunction<String, ClassLoader, Optional<Class<?>>> loadClass;

	ClasspathScanner(Supplier<ClassLoader> classLoaderSupplier,
			BiFunction<String, ClassLoader, Optional<Class<?>>> loadClass) {
		this.classLoaderSupplier = classLoaderSupplier;
		this.loadClass = loadClass;
	}

	boolean isPackage(String packageName) {
		Preconditions.notBlank(packageName, "package name must not be null or blank");

		String path = packagePath(packageName);
		try {
			Enumeration<URL> resource = classLoaderSupplier.get().getResources(path);
			return resource.hasMoreElements();
		}
		catch (IOException e) {
			return false;
		}
	}

	List<Class<?>> scanForClassesInPackage(String basePackageName, Predicate<Class<?>> classFilter) {
		Preconditions.notBlank(basePackageName, "basePackageName must not be blank");

		List<File> dirs = allSourceDirsForPackage(basePackageName);
		return allClassesInSourceDirs(dirs, basePackageName, classFilter);
	}

	private List<Class<?>> allClassesInSourceDirs(List<File> sourceDirs, String basePackageName,
			Predicate<Class<?>> classFilter) {
		List<Class<?>> classes = new ArrayList<>();
		for (File aSourceDir : sourceDirs) {
			classes.addAll(findClassesInSourceDirRecursively(aSourceDir, basePackageName, classFilter));
		}
		return classes;
	}

	List<Class<?>> scanForClassesInClasspathRoot(File root, Predicate<Class<?>> classFilter) {
		Preconditions.notNull(root, "root must not be null");
		Preconditions.condition(root.isDirectory(),
			() -> "root must be an existing directory: " + root.getAbsolutePath());

		return findClassesInSourceDirRecursively(root, "", classFilter);
	}

	private List<File> allSourceDirsForPackage(String basePackageName) {
		try {
			ClassLoader classLoader = classLoaderSupplier.get();
			String path = packagePath(basePackageName);
			Enumeration<URL> resources = classLoader.getResources(path);
			List<File> dirs = new ArrayList<>();
			while (resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				dirs.add(new File(resource.getFile()));
			}
			return dirs;
		}
		catch (IOException e) {
			return Collections.emptyList();
		}
	}

	private String packagePath(String basePackageName) {
		return basePackageName.replace('.', '/');
	}

	private List<Class<?>> findClassesInSourceDirRecursively(File sourceDir, String packageName,
			Predicate<Class<?>> classFilter) {
		Preconditions.notNull(classFilter, "classFilter must not be null");

		List<Class<?>> classesCollector = new ArrayList<>();
		collectClassesRecursively(sourceDir, packageName, classesCollector, classFilter);
		return classesCollector;
	}

	private void collectClassesRecursively(File sourceDir, String packageName, List<Class<?>> classesCollector,
			Predicate<Class<?>> classFilter) {
		File[] files = sourceDir.listFiles();
		if (files == null) {
			return;
		}
		for (File file : files) {
			if (isClassFile(file)) {
				Optional<Class<?>> classForClassFile = loadClassForClassFile(file, packageName);
				classForClassFile.filter(classFilter).ifPresent(classesCollector::add);
			}
			else if (file.isDirectory()) {
				collectClassesRecursively(file, appendPackageName(packageName, file.getName()), classesCollector,
					classFilter);
			}
		}
	}

	private String appendPackageName(String packageName, String subpackageName) {
		if (packageName.isEmpty())
			return subpackageName;
		else
			return packageName + "." + subpackageName;
	}

	private Optional<Class<?>> loadClassForClassFile(File file, String packageName) {
		String className = packageName + '.'
				+ file.getName().substring(0, file.getName().length() - CLASS_FILE_SUFFIX.length());
		return loadClass.apply(className, classLoaderSupplier.get());
	}

	private static boolean isClassFile(File file) {
		return file.isFile() && file.getName().endsWith(CLASS_FILE_SUFFIX);
	}

}
