/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import java.util.Optional;

import org.junit.jupiter.params.provider.ParameterDeclaration;

/**
 * @since 5.13
 */
class ExecutableParameterDeclaration implements ParameterDeclaration {

	private final java.lang.reflect.Parameter parameter;
	private final int index;

	ExecutableParameterDeclaration(java.lang.reflect.Parameter parameter, int index) {
		this.parameter = parameter;
		this.index = index;
	}

	@Override
	public java.lang.reflect.Parameter getAnnotatedElement() {
		return this.parameter;
	}

	@Override
	public Class<?> getParameterType() {
		return this.parameter.getType();
	}

	@Override
	public int getParameterIndex() {
		return this.index;
	}

	@Override
	public Optional<String> getParameterName() {
		return this.parameter.isNamePresent() ? Optional.of(this.parameter.getName()) : Optional.empty();
	}
}
