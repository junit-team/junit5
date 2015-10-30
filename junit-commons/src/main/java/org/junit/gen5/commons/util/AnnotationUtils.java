/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.commons.util;

import static java.util.stream.Collectors.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Collection of utilities for working with {@linkplain Annotation annotations}.
 *
 * @author Sam Brannen
 * @author Stefan Bechtold
 * @since 5.0
 * @see Annotation
 * @see AnnotatedElement
 */
public final class AnnotationUtils {

	private AnnotationUtils() {
		/* no-op */
	}

	/**
	 * Find an annotation of {@code annotationType} that is either <em>present</em>
	 * or <em>meta-present</em> on the supplied {@code element}.
	 */
	public static <A extends Annotation> Optional<A> findAnnotation(AnnotatedElement element, Class<A> annotationType) {
		Preconditions.notNull(element, "AnnotatedElement must not be null");
		Preconditions.notNull(annotationType, "annotationType must not be null");

		A annotation = element.getDeclaredAnnotation(annotationType);

		// TODO Avoid infinite recursion by tracking visited annotations.
		// TODO Exclude annotations from the java.lang.annotation package from searches.
		if (annotation == null) {
			for (Annotation composedAnnotation : element.getDeclaredAnnotations()) {
				Optional<A> metaAnnotation = findAnnotation(composedAnnotation.annotationType(), annotationType);
				if (metaAnnotation.isPresent()) {
					return metaAnnotation;
				}
			}
		}

		return Optional.ofNullable(annotation);
	}

	public static List<Method> findAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotationType) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(annotationType, "annotationType must not be null");

		// TODO Support meta-annotations.

		// @formatter:off
		return Arrays.stream(clazz.getDeclaredMethods())
				.filter(method -> method.isAnnotationPresent(annotationType))
				.collect(toList());
		// @formatter:on
	}

}
