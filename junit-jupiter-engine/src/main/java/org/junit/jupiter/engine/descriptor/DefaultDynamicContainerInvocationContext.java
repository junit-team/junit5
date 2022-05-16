/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import java.util.stream.Stream;

import org.junit.jupiter.api.extension.DynamicContainerInvocationContext;
import org.junit.jupiter.api.function.Executable;

/**
 * Default implementation of the {@link DynamicContainerInvocationContext} API.
 *
 * @since 5.8
 */
class DefaultDynamicContainerInvocationContext implements DynamicContainerInvocationContext {

	private final Stream<Executable> executable;

	DefaultDynamicContainerInvocationContext(Stream<Executable> executable) {
		this.executable = executable;
	}

	@Override
	public Stream<Executable> getExecutable() {
		return this.executable;
	}

}
