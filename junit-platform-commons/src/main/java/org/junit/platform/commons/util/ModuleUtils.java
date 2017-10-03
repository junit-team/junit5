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

import java.util.Collections;
import java.util.List;

import org.apiguardian.api.API;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

/**
 * Collection of utilities for working with {@code java.lang.Module}
 * and friends.
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
public class ModuleUtils {

	/**
	 * Version hint is set to {@code "base"} here.
	 */
	public static final String VERSION = "base";

	/**
	 * Special module name to scan all resolved modules found in the boot layer configuration.
	 */
	public static final String ALL_MODULES = "ALL-MODULES";

	private static final Logger logger = LoggerFactory.getLogger(ModuleUtils.class);

	/**
	 * Find all classes for the given module name.
	 *
	 * @param moduleName name of the module to scan
	 * @param filter class filter to apply
	 * @return an immutable list of all such classes found; never {@code null}
	 * but potentially empty
	 */
	public static List<Class<?>> findAllClassesInModule(String moduleName, ClassFilter filter) {
		logger.config(() -> "Basic version of findAllClassesInModule() always returns an empty list!");
		return Collections.emptyList();
	}
}
