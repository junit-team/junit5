/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import java.util.Optional;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.MethodOrdererContext;
import org.junit.jupiter.api.parallel.ExecutionMode;

public class CustomMethodOrderer implements MethodOrderer {
	@Override
	public void orderMethods(MethodOrdererContext context) {
	}

	@Override
	public Optional<ExecutionMode> getDefaultExecutionMode() {
		return Optional.empty();
	}
}
