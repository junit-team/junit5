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
import java.util.List;

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
	 * Return list of classes found in the named module that may contain testable methods.
	 *
	 * @param filter filter to apply to each class candidate
	 * @param moduleName name of the module to inspect
	 * @return list of test classes
	 */
	List<Class<?>> findAllClassesInModule(ClassFilter filter, String moduleName);

	/**
	 * Return list of classes found on the boot module-path that may contain testable methods.
	 *
	 * @param filter filter to apply to each class candidate
	 * @return list of test classes
	 */
	List<Class<?>> findAllClassesOnModulePath(ClassFilter filter);

	/**
	 * Return list of classes found at the given path roots that may contain testable methods.
	 *
	 * @param filter filter to apply to each class candidate
	 * @param parent class loader instance to used the parent class loader
	 * @param entries a possibly-empty array of paths to directories of modules or paths to packaged or exploded modules
	 * @return list of test classes
	 * @see <a href="http://download.java.net/java/jdk9/docs/api/java/lang/module/ModuleFinder.html">ModuleFinder</a>
	 */
	List<Class<?>> findAllClassesOnModulePath(ClassFilter filter, ClassLoader parent, Path... entries);
}
