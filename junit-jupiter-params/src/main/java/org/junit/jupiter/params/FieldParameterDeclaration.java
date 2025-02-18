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

/**
 * @since 5.13
 */
class FieldParameterDeclaration implements ParameterDeclaration {

	private final Field field;
	private final Parameter annotation;

	FieldParameterDeclaration(Field field, Parameter annotation) {
		this.field = field;
		this.annotation = annotation;
	}

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
		return this.annotation.value();
	}

	Parameter getAnnotation() {
		return annotation;
	}
}
