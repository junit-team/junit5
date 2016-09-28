/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console.options;

/**
 * @since 1.0
 */
public enum Details {
	/** No test plan execution details are printed. */
	HIDDEN,

	/** Test plan execution details are rendered in a flat, line-per-line mode. */
	FLAT,

	/** Test plan execution details are rendered in a simple, compact manner. */
	TREE,

	/** Combines tree and flat mode. */
	VERBOSE
}
