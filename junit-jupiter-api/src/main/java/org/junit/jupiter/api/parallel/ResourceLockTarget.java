/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.parallel;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;

/**
 * Indicates the target of a {@link ResourceLock}.
 *
 * @since 5.10
 * @see ResourceLock
 */
@API(status = EXPERIMENTAL, since = "5.10")
public enum ResourceLockTarget {

	/**
	 * Point to the test descriptor itself
	 */
	SELF,

	/**
	 * Skip the test descriptor itself and apply annotation {@link ResourceLock} to its direct children
	 */
	CHILDREN

}
