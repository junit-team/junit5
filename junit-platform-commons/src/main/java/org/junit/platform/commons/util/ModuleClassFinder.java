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

import java.util.List;
import java.util.function.Predicate;

import org.apiguardian.api.API;

/**
 * Class finder service providing interface.
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
public interface ModuleClassFinder {

	/**
	 * Special name indicating a search for all classes in all modules on the module-path.
	 */
	String ALL_MODULE_PATH = "ALL-MODULE-PATH";

	/**
	 * Return list of classes found in the named module that may contain testable methods.
	 *
	 * @param moduleName name of the module to inspect or {@code ALL-MODULE-PATH}
	 * @param classTester filter to apply to each class instance
	 * @param classNameFilter filter to apply to the fully qualified class name
	 * @return list of test classes
	 *
	 * @see #ALL_MODULE_PATH
	 */
	List<Class<?>> findAllClassesInModule(String moduleName, Predicate<Class<?>> classTester,
			Predicate<String> classNameFilter);
}
