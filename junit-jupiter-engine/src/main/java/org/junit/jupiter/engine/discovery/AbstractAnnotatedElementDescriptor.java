/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Optional;

import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestDescriptor;

abstract class AbstractAnnotatedElementDescriptor<E extends AnnotatedElement> {

	private final TestDescriptor testDescriptor;
	private final E annotatedElement;

	AbstractAnnotatedElementDescriptor(TestDescriptor testDescriptor, E annotatedElement) {
		this.testDescriptor = testDescriptor;
		this.annotatedElement = annotatedElement;
	}

	protected E getAnnotatedElement() {
		return annotatedElement;
	}

	TestDescriptor getTestDescriptor() {
		return testDescriptor;
	}

	public final String getDisplayName() {
		return this.testDescriptor.getDisplayName();
	}

	public boolean isAnnotated(Class<? extends Annotation> annotationType) {
		Preconditions.notNull(annotationType, "annotationType must not be null");
		return AnnotationUtils.isAnnotated(getAnnotatedElement(), annotationType);
	}

	public <A extends Annotation> Optional<A> findAnnotation(Class<A> annotationType) {
		Preconditions.notNull(annotationType, "annotationType must not be null");
		return AnnotationUtils.findAnnotation(getAnnotatedElement(), annotationType);
	}

	public <A extends Annotation> List<A> findRepeatableAnnotations(Class<A> annotationType) {
		Preconditions.notNull(annotationType, "annotationType must not be null");
		return AnnotationUtils.findRepeatableAnnotations(getAnnotatedElement(), annotationType);
	}
}
