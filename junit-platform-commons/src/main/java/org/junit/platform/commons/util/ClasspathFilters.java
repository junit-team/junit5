/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import java.nio.file.Path;
import java.util.function.Predicate;

/**
 * @since 1.11
 */
class ClasspathFilters {

	static final String CLASS_FILE_SUFFIX = ".class";
	private static final String PACKAGE_INFO_FILE_NAME = "package-info" + CLASS_FILE_SUFFIX;
	private static final String MODULE_INFO_FILE_NAME = "module-info" + CLASS_FILE_SUFFIX;

	static Predicate<Path> classFiles() {
		return file -> isNotPackageInfo(file) && isNotModuleInfo(file) && isClassFile(file);
	}

	static Predicate<Path> resourceFiles() {
		return file -> !isClassFile(file);
	}

	private static boolean isNotPackageInfo(Path path) {
		return !path.endsWith(PACKAGE_INFO_FILE_NAME);
	}

	private static boolean isNotModuleInfo(Path path) {
		return !path.endsWith(MODULE_INFO_FILE_NAME);
	}

	private static boolean isClassFile(Path file) {
		return file.getFileName().toString().endsWith(CLASS_FILE_SUFFIX);
	}

	private ClasspathFilters() {
	}

}
