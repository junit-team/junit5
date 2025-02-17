/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.support;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.reflect.Field;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.AnnotatedElementContext;
import org.junit.jupiter.params.Parameter;

/**
 * @since 5.13
 */
@API(status = EXPERIMENTAL, since = "5.13")
public interface FieldContext extends AnnotatedElementContext {

	Field getField();

	default int getParameterIndex() {
		return getParameterAnnotation().value();
	}

	Parameter getParameterAnnotation();

	@Override
	default Field getAnnotatedElement() {
		return getField();
	}
}
