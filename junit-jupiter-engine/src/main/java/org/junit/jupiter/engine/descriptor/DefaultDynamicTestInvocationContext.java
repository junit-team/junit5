/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import org.junit.jupiter.api.extension.DynamicTestInvocationContext;
import org.junit.jupiter.api.function.Executable;

/**
 * Default implementation of the {@link DynamicTestInvocationContext} API.
 *
 * @since 5.8
 */
class DefaultDynamicTestInvocationContext implements DynamicTestInvocationContext {

	private final Executable executable;

	DefaultDynamicTestInvocationContext(Executable executable) {
		this.executable = executable;
	}

	@Override
	public Executable getExecutable() {
		return this.executable;
	}

}
