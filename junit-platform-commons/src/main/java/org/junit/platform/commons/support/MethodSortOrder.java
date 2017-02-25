/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.commons.support;

import static org.junit.platform.commons.meta.API.Usage.Maintained;

import org.junit.platform.commons.meta.API;

/**
 * @since 1.0
 */
@API(Maintained)
public enum MethodSortOrder {
	/**
	 * Sort methods from top to bottom.
	 */
	HierarchyDown,

	/**
	 * Sort methods from bottom to top.
	 */
	HierarchyUp
}
