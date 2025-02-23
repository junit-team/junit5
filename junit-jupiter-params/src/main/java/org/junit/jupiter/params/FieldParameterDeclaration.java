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

import org.junit.jupiter.params.provider.ParameterDeclaration;
import org.junit.jupiter.params.support.FieldContext;

/**
 * @since 5.13
 */
class FieldParameterDeclaration implements ParameterDeclaration, FieldContext {

	private final Field field;
	private final Parameter annotation;
	private final int index;

	FieldParameterDeclaration(Field field, Parameter annotation) {
		this(field, annotation, annotation.value());
	}

	FieldParameterDeclaration(Field field, Parameter annotation, int index) {
		this.field = field;
		this.annotation = annotation;
		this.index = index;
	}

	// --- ParameterDeclaration ------------------------------------------------

	@Override
	public Field getAnnotatedElement() {
		return this.field;
	}

	@Override
	public Class<?> getType() {
		return this.field.getType();
	}

	@Override
	public int getIndex() {
		return index;
	}

	@Override
	public Optional<String> getName() {
		return Optional.of(this.field.getName());
	}

	// --- FieldContext --------------------------------------------------------

	@Override
	public Field getField() {
		return this.field;
	}

	@Override
	public Parameter getParameterAnnotation() {
		return this.annotation;
	}

	@Override
	public int getParameterIndex() {
		return getIndex();
	}
}
