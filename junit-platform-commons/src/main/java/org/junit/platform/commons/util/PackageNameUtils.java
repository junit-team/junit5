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

import static org.junit.platform.commons.util.PackageUtils.DEFAULT_PACKAGE_NAME;

/**
 * Collection of utilities for working with package names.
 *
 * <h2>DISCLAIMER</h2>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 1.11.3
 */
class PackageNameUtils {

	static String getPackageName(Class<?> clazz) {
		Package p = clazz.getPackage();
		if (p != null) {
			return p.getName();
		}
		String className = clazz.getName();
		int index = className.lastIndexOf('.');
		return index == -1 ? DEFAULT_PACKAGE_NAME : className.substring(0, index);
	}

}
