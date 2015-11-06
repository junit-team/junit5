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

public class ReflectionPackage {

	final static String CLASS_FILE_SUFFIX = ".class";

	private String packageBaseName;

	ReflectionPackage(String packageBaseName) {
		this.packageBaseName = packageBaseName;
	}

	Class[] findAllClasses() {
		try {
			List<File> dirs = allSourceDirsForPackage();
			List<Class> classes = allClassesInSourceDirs(dirs);
			return classes.toArray(new Class[classes.size()]);
		}
		catch (IOException e) {
			e.printStackTrace();
			return new Class[0];
		}
	}

	private List<Class> allClassesInSourceDirs(List<File> sourceDirs) {
		List<Class> classes = new ArrayList<>();
		for (File aSourceDir : sourceDirs) {
			classes.addAll(findClassesInSourceDirRecursively(aSourceDir, packageBaseName));
		}
		return classes;
	}

	private List<File> allSourceDirsForPackage() throws IOException {
		ClassLoader classLoader = ReflectionUtils.getClassLoader();
		String path = packageBaseName.replace('.', '/');
		Enumeration<URL> resources = classLoader.getResources(path);
		List<File> dirs = new ArrayList<>();
		while (resources.hasMoreElements()) {
			URL resource = resources.nextElement();
			dirs.add(new File(resource.getFile()));
		}
		return dirs;
	}

	private static List<Class> findClassesInSourceDirRecursively(File sourceDir, String packageName) {
		List<Class> classes = new ArrayList<>();
		if (!sourceDir.exists()) {
			return classes;
		}
		for (File file : sourceDir.listFiles()) {
			if (file.isDirectory()) {
				classes.addAll(findClassesInSourceDirRecursively(file, packageName + "." + file.getName()));
			}
			else if (isAClassFile(file)) {
				Optional<Class<?>> classForClassFile = getClassForClassFile(file, packageName);
				classForClassFile.ifPresent(clazz -> classes.add(clazz));
			}
		}
		return classes;
	}

	private static Optional<Class<?>> getClassForClassFile(File file, String packageName) {
		String className = packageName + '.'
				+ file.getName().substring(0, file.getName().length() - CLASS_FILE_SUFFIX.length());
		return ReflectionUtils.loadClass(className);
	}

	private static boolean isAClassFile(File file) {
		return file.getName().endsWith(CLASS_FILE_SUFFIX);
	}

}
