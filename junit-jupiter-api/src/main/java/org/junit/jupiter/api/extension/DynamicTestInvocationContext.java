/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;
import org.junit.jupiter.api.function.Executable;

/**
 * {@code DynamicTestInvocationContext} represents the <em>context</em> of a
 * single invocation of a {@linkplain org.junit.jupiter.api.DynamicTest test}.
 *
 * @since 5.8
 * @see org.junit.jupiter.api.DynamicTest
 */
@API(status = EXPERIMENTAL, since = "5.8")
public class DynamicTestInvocationContext {
	private Executable executable;

	public DynamicTestInvocationContext(Executable executable) {
		this.executable = executable;
	}

	/**
	 * Get the {@linkplain Executable} of this dynamic test invocation.
	 *
	 * @return the executable of the dynamic test invocation, never null.
	 */
	public Executable getExecutable() {
		return executable;
	}
}
