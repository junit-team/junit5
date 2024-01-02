/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apiguardian.api.API;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

/**
 * Collection of utilities for working with {@code java.lang.Module}
 * and friends.
 *
 * <h2>DISCLAIMER</h2>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 1.1
 */
@API(status = INTERNAL, since = "1.1")
public class ModuleUtils {

	private static final Logger logger = LoggerFactory.getLogger(ModuleUtils.class);

	/**
	 * Find all non-system boot modules names.
	 *
	 * @return a set of all such module names; never {@code null} but
	 * potentially empty
	 */
	public static Set<String> findAllNonSystemBootModuleNames() {
		logger.config(() -> "Basic version of findAllNonSystemBootModuleNames() always returns an empty set!");
		return emptySet();
	}

	/**
	 * Determine if the current Java runtime supports the Java Platform Module System.
	 *
	 * @return {@code true} if the Java Platform Module System is available,
	 * otherwise {@code false}
	 */
	public static boolean isJavaPlatformModuleSystemAvailable() {
		return false;
	}

	/**
	 * Return the name of the module that the class or interface is a member of.
	 *
	 * @param type class or interface to analyze
	 * @return the module name; never {@code null} but potentially empty
	 */
	public static Optional<String> getModuleName(Class<?> type) {
		return Optional.empty();
	}

	/**
	 * Return the raw version of the module that the class or interface is a member of.
	 *
	 * @param type class or interface to analyze
	 * @return the raw module version; never {@code null} but potentially empty
	 */
	public static Optional<String> getModuleVersion(Class<?> type) {
		return Optional.empty();
	}

	/**
	 * Find all classes for the given module name.
	 *
	 * @param moduleName the name of the module to scan; never {@code null} or
	 * <em>empty</em>
	 * @param filter the class filter to apply; never {@code null}
	 * @return an immutable list of all such classes found; never {@code null}
	 * but potentially empty
	 */
	public static List<Class<?>> findAllClassesInModule(String moduleName, ClassFilter filter) {
		Preconditions.notBlank(moduleName, "Module name must not be null or empty");
		Preconditions.notNull(filter, "Class filter must not be null");

		logger.config(() -> "Basic version of findAllClassesInModule() always returns an empty list!");
		return emptyList();
	}

}
