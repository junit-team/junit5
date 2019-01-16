/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.support.io;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.support.io.TempDirectory.TempDirContext;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.Preconditions;

/**
 * Default implementation of the {@link TempDirContext} API.
 *
 * @since 5.4
 */
class DefaultTempDirContext implements TempDirContext {

	private final Field field;
	private final ParameterContext parameterContext;

	static DefaultTempDirContext from(Field field) {
		Preconditions.notNull(field, "Field must not be null");
		return new DefaultTempDirContext(field, null);
	}

	static DefaultTempDirContext from(ParameterContext parameterContext) {
		Preconditions.notNull(parameterContext, "ParameterContext must not be null");
		return new DefaultTempDirContext(null, parameterContext);
	}

	private DefaultTempDirContext(Field field, ParameterContext parameterContext) {
		this.field = field;
		this.parameterContext = parameterContext;
	}

	@Override
	public AnnotatedElement getElement() {
		return this.field != null ? this.field : this.parameterContext.getParameter();
	}

	@Override
	public Optional<Field> getField() {
		return Optional.ofNullable(this.field);
	}

	@Override
	public Optional<ParameterContext> getParameterContext() {
		return Optional.ofNullable(this.parameterContext);
	}

	@Override
	public boolean isAnnotated(Class<? extends Annotation> annotationType) {
		if (this.field != null) {
			return AnnotationUtils.isAnnotated(this.field, annotationType);
		}
		return this.parameterContext.isAnnotated(annotationType);
	}

	@Override
	public <A extends Annotation> Optional<A> findAnnotation(Class<A> annotationType) {
		if (this.field != null) {
			return AnnotationUtils.findAnnotation(this.field, annotationType);
		}
		return this.parameterContext.findAnnotation(annotationType);
	}

	@Override
	public <A extends Annotation> List<A> findRepeatableAnnotations(Class<A> annotationType) {
		if (this.field != null) {
			return AnnotationUtils.findRepeatableAnnotations(this.field, annotationType);
		}
		return this.parameterContext.findRepeatableAnnotations(annotationType);
	}

}
