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

import org.junit.jupiter.params.support.FieldContext;

class DefaultFieldContext implements FieldContext {

	private final Field field;
	private final Parameter annotation;

	DefaultFieldContext(Field field, Parameter annotation) {
		this.field = field;
		this.annotation = annotation;
	}

	@Override
	public Field getField() {
		return this.field;
	}

	@Override
	public Parameter getParameterAnnotation() {
		return this.annotation;
	}
}
