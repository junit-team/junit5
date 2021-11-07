/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.io;

/**
 * Enumeration of cleanup modes for a {@code TempDir}.
 *
 * <p>When a test with a temporary directory completes, it might be useful in
 * some cases to be able to view the contents of the directory resulting from
 * the test. {@code CleanupMode} allows control of how a {@code TempDir}
 * is cleaned up.
 *
 * @since 5.4
 * @see TempDir
 */
public enum CleanupMode {

	/**
	 * Always clean up a temporary directory after the test has completed.
	 *
	 * @see TempDir
	 * @see #ON_SUCCESS
	 * @see #NEVER
	 */
	ALWAYS,

	/**
	 * Don't clean up a temporary directory after the test has completed.
	 *
	 * @see TempDir
	 * @see #ALWAYS
	 * @see #NEVER
	 */
	NEVER,

	/**
	 * Only clean up a temporary directory if the test completed successfully.
	 *
	 * @see TempDir
	 * @see #ALWAYS
	 * @see #ON_SUCCESS
	 */
	ON_SUCCESS
}
