/*
 * Copyright 2015-2024 the original author or authors.
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
 * {@code ResourceLockTarget} is used to define the target of a shared resource.
 *
 * @since 5.12
 * @see ResourceLock#target()
 */
@API(status = EXPERIMENTAL, since = "5.12")
public enum ResourceLockTarget {

	/**
	 * Add a shared resource to the current node.
	 */
	SELF,

	/**
	 * Add a shared resource to the <em>direct</em> children of the current node.
	 *
	 * <p>Examples of "parent - child" relationship in the context of
	 * {@link ResourceLockTarget}:
	 * <ul>
	 *     <li>a test class
	 *     - test methods and nested test classes declared in the class.</li>
	 *     <li>a nested test class
	 *     - test methods declared in the nested class.</li>
	 *     <li>a test method - considered to have no children.</li>
	 * </ul>
	 */
	CHILDREN

}
