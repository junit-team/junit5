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
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

/**
 * @since 5.0
 */
class ClasspathScanner {

	private static final String CLASS_FILE_SUFFIX = ".class";

	private final String basePackageName;

	ClasspathScanner(String basePackageName) {
		Preconditions.notBlank(basePackageName, "basePackageName must not be null");
		this.basePackageName = basePackageName;
	}

	Class<?>[] scanForClassesRecursively() {
		try {
			List<File> dirs = allSourceDirsForPackage();
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
		ClassLoader classLoader = ReflectionUtils.getClassLoader();
		String path = this.basePackageName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		return dirs;
	}

	private static List<Class<?>> findClassesInSourceDirRecursively(File sourceDir, String packageName) {
		List<Class<?>> classes = new ArrayList<>();
		if (!sourceDir.exists()) {
			return classes;
		}
		for (File file : sourceDir.listFiles()) {
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
