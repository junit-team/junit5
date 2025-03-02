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

import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.jupiter.params.support.FieldContext;
import org.junit.jupiter.params.support.ParameterDeclaration;

/**
 * @since 5.13
 */
class FieldParameterDeclaration implements ParameterDeclaration, FieldContext {

	private final Field field;
	private final int index;

	FieldParameterDeclaration(Field field, int index) {
		this.field = field;
		this.index = index;
	}

	@Override
	public Field getField() {
		return this.field;
	}

	@Override
	public Field getAnnotatedElement() {
		return this.field;
	}

	@Override
	public Class<?> getParameterType() {
		return this.field.getType();
	}

	@Override
	public int getParameterIndex() {
		return index;
	}

	@Override
	public Optional<String> getParameterName() {
		return Optional.of(this.field.getName());
	}

}
