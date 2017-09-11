/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

/**
 * Collection of utilities for working with modules.
 *
 * <h3>DISCLAIMER</h3>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 1.1
 */
@API(status = INTERNAL, since = "1.1")
public final class ModuleUtils {

	private static final Logger logger = LoggerFactory.getLogger(ModuleUtils.class);

	///CLOVER:OFF
	private ModuleUtils() {
		/* no-op */
	}
	///CLOVER:ON

	/**
	 * Convenient short-cut for finding all classes in all modules that are on the module-path.
	 */
	public static List<Class<?>> findAllClassesInModulepath(Predicate<Class<?>> classTester,
			Predicate<String> classNameFilter) {
		return findAllClassesInModule(ModuleClassFinder.ALL_MODULE_PATH, classTester, classNameFilter);
	}

	/**
	 * Find all classes in the specified module.
	 *
	 * @param moduleName name of the module to inspect or {@code ALL-MODULE-PATH}
	 * @param classTester filter to apply to each class instance
	 * @param classNameFilter filter to apply to the fully qualified class name
	 * @return list of classes matching the passed-in criteria
	 */
	public static List<Class<?>> findAllClassesInModule(String moduleName, Predicate<Class<?>> classTester,
			Predicate<String> classNameFilter) {
		Preconditions.notBlank(moduleName, "module name must not be null or blank");
		Preconditions.notNull(classTester, "class tester must not be null");
		Preconditions.notNull(classNameFilter, "class name filter must not be null");

		ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();
		List<Class<?>> classes = new ArrayList<>();

		logger.config(() -> "Loading auto-detected class finders...");
		int serviceCounter = 0;
		for (ModuleClassFinder classFinder : ServiceLoader.load(ModuleClassFinder.class, classLoader)) {
			classes.addAll(classFinder.findAllClassesInModule(moduleName, classTester, classNameFilter));
			serviceCounter++;
		}
		if (serviceCounter == 0) {
			logger.warn(() -> "No module class finder service registered! No test classes found.");
		}
		return Collections.unmodifiableList(classes);
	}

}
