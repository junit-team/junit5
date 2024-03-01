/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.support;

import static org.apiguardian.api.API.Status.STABLE;

import org.apiguardian.api.API;

/**
 * Search options for finding an annotation within a class hierarchy.
 *
 * @since 1.8
 * @see #DEFAULT
 * @see #INCLUDE_ENCLOSING_CLASSES
 */
@API(status = STABLE, since = "1.10")
public enum SearchOption {

	/**
	 * Search the inheritance hierarchy (i.e., the current class, implemented
	 * interfaces, and superclasses), but do not search on enclosing classes.
	 *
	 * @see Class#getSuperclass()
	 * @see Class#getInterfaces()
	 */
	DEFAULT,

	/**
	 * Search the inheritance hierarchy as with the {@link #DEFAULT} search option
	 * but also search the {@linkplain Class#getEnclosingClass() enclosing class}
	 * hierarchy for <em>inner classes</em> (i.e., a non-static member classes).
	 */
	INCLUDE_ENCLOSING_CLASSES

}
