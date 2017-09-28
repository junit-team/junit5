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

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.Function;

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
	 * Find all classes in the specified module.
	 *
	 * @param filter filter to apply to each candidate
	 * @param moduleName name of the module to inspect
	 * @return list of classes matching the passed-in criteria
	 */
	public static List<Class<?>> findAllClassesInModule(ClassFilter filter, String moduleName) {
		Preconditions.notNull(filter, "class filter must not be null");
		Preconditions.notBlank(moduleName, "module name must not be null or blank");

		return find(finder -> finder.findAllClassesInModule(filter, moduleName));
	}

	/**
	 * Find all classes in all modules that are on the boot module-path.
	 *
	 * @param filter filter to apply to each candidate
	 * @return list of classes matching the passed-in criteria
	 */
	public static List<Class<?>> findAllClassesOnModulePath(ClassFilter filter) {
		Preconditions.notNull(filter, "class filter must not be null");

		return find(finder -> finder.findAllClassesOnModulePath(filter));
	}

	/**
	 * Find all classes in all modules that are locatable on the given path entries.
	 *
	 * @param filter filter to apply to each candidate
	 * @param parent class loader parent
	 * @return list of classes matching the passed-in criteria
	 */
	public static List<Class<?>> findAllClassesOnModulePath(ClassFilter filter, ClassLoader parent, Path... entries) {
		Preconditions.notNull(filter, "class filter must not be null");
		Preconditions.notNull(filter, "class loader must not be null");

		return find(finder -> finder.findAllClassesOnModulePath(filter, parent, entries));
	}

	private static List<Class<?>> find(Function<ModuleClassFinder, List<Class<?>>> function) {
		ClassLoader classLoader = ClassLoaderUtils.getDefaultClassLoader();
		List<Class<?>> classes = new ArrayList<>();

		logger.config(() -> "Loading auto-detected class finders...");
		int serviceCounter = 0;
		for (ModuleClassFinder classFinder : ServiceLoader.load(ModuleClassFinder.class, classLoader)) {
			classes.addAll(function.apply(classFinder));
			serviceCounter++;
		}
		if (serviceCounter == 0) {
			logger.warn(() -> "No module class finder service registered! No test classes found.");
		}
		return Collections.unmodifiableList(classes);
	}

}
