/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.commons.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * @since 5.0
 */
class ClasspathScanner {

	private static final String CLASS_FILE_SUFFIX = ".class";
	private static final Logger LOG = Logger.getLogger(ClasspathScanner.class.getName());

	private final String basePackageName;
	private final Supplier<ClassLoader> classLoaderSupplier;

	ClasspathScanner(String basePackageName, Supplier<ClassLoader> classLoaderSupplier) {
		this.classLoaderSupplier = classLoaderSupplier;
		Preconditions.notBlank(basePackageName, "basePackageName must not be null");
		this.basePackageName = basePackageName;
	}

	boolean isPackage() {
		ClassLoader classLoader = classLoaderSupplier.get();
		String path = basePackagePath();
		try {
			classLoader.getResources(path);
			return true;
		}
		catch (IOException e) {
			return false;
		}
	}

	Class<?>[] scanForClassesRecursively() {
		try {
			List<File> dirs = allSourceDirsForPackage();
			LOG.fine(() -> "Directories found: " + dirs);
			List<Class<?>> classes = allClassesInSourceDirs(dirs);
			return classes.toArray(new Class[classes.size()]);
		}
		catch (IOException e) {
			e.printStackTrace();
			return new Class[0];
		}
	}

	private List<Class<?>> allClassesInSourceDirs(List<File> sourceDirs) {
		List<Class<?>> classes = new ArrayList<>();
		for (File aSourceDir : sourceDirs) {
			classes.addAll(findClassesInSourceDirRecursively(aSourceDir, this.basePackageName));
		}
		return classes;
	}

	private List<File> allSourceDirsForPackage() throws IOException {
		ClassLoader classLoader = classLoaderSupplier.get();
		LOG.fine(() -> "ClassLoader: " + classLoader);
		String path = basePackagePath();
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		return dirs;
	}

	private String basePackagePath() {
		return this.basePackageName.replace('.', '/');
	}

	private static List<Class<?>> findClassesInSourceDirRecursively(File sourceDir, String packageName) {
		LOG.finer(() -> "Searching for classes in package: " + packageName);
		List<Class<?>> classes = new ArrayList<>();
		if (!sourceDir.exists()) {
			return classes;
		}
		File[] files = sourceDir.listFiles();
		LOG.finer(() -> "Files found: " + Arrays.toString(files));
		for (File file : files) {
			if (file.isDirectory()) {
				classes.addAll(findClassesInSourceDirRecursively(file, packageName + "." + file.getName()));
			}
			else if (isClassFile(file)) {
				Optional<Class<?>> classForClassFile = loadClassForClassFile(file, packageName);
				classForClassFile.ifPresent(clazz -> classes.add(clazz));
			}
		}
		return classes;
	}

	private static Optional<Class<?>> loadClassForClassFile(File file, String packageName) {
		String className = packageName + '.'
				+ file.getName().substring(0, file.getName().length() - CLASS_FILE_SUFFIX.length());
		return ReflectionUtils.loadClass(className);
	}

	private static boolean isClassFile(File file) {
		return file.getName().endsWith(CLASS_FILE_SUFFIX);
	}

}
