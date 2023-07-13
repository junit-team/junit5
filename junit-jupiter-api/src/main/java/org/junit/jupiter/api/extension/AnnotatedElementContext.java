/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.util.List;
import java.util.Optional;

import org.apiguardian.api.API;

@API(status = EXPERIMENTAL, since = "5.10")
public interface AnnotatedElementContext {

	/**
	 * Determine if an annotation of {@code annotationType} is either
	 * <em>present</em> or <em>meta-present</em> on the {@link AnnotatedElement} for
	 * this context.
	 *
	 * <h4>WARNING</h4>
	 * <p>Favor the use of this method over directly invoking
	 * {@link AnnotatedElement#isAnnotationPresent(Class)} due to a bug in {@code javac}
	 * on JDK versions prior to JDK 9.
	 *
	 * @param annotationType the annotation type to search for; never {@code null}
	 * @return {@code true} if the annotation is present or meta-present
	 * @since 5.1.1
	 * @see #findAnnotation(Class)
	 * @see #findRepeatableAnnotations(Class)
	 */
	boolean isAnnotated(Class<? extends Annotation> annotationType);

	/**
	 * Find the first annotation of {@code annotationType} that is either
	 * <em>present</em> or <em>meta-present</em> on the {@link AnnotatedElement} for
	 * this context.
	 *
	 * <h4>WARNING</h4>
	 * <p>Favor the use of this method over directly invoking annotation lookup
	 * methods in the {@link AnnotatedElement} API due to a bug in {@code javac} on JDK
	 * versions prior to JDK 9.
	 *
	 * @param <A> the annotation type
	 * @param annotationType the annotation type to search for; never {@code null}
	 * @return an {@code Optional} containing the annotation; never {@code null} but
	 * potentially empty
	 * @since 5.1.1
	 * @see #isAnnotated(Class)
	 * @see #findRepeatableAnnotations(Class)
	 */
	<A extends Annotation> Optional<A> findAnnotation(Class<A> annotationType);

	/**
	 * Find all <em>repeatable</em> {@linkplain Annotation annotations} of
	 * {@code annotationType} that are either <em>present</em> or
	 * <em>meta-present</em> on the {@link AnnotatedElement} for this context.
	 *
	 * <h4>WARNING</h4>
	 * <p>Favor the use of this method over directly invoking annotation lookup
	 * methods in the {@link AnnotatedElement} API due to a bug in {@code javac} on JDK
	 * versions prior to JDK 9.
	 *
	 * @param <A> the annotation type
	 * @param annotationType the repeatable annotation type to search for; never
	 * {@code null}
	 * @return the list of all such annotations found; neither {@code null} nor
	 * mutable, but potentially empty
	 * @since 5.1.1
	 * @see #isAnnotated(Class)
	 * @see #findAnnotation(Class)
	 * @see java.lang.annotation.Repeatable
	 */
	<A extends Annotation> List<A> findRepeatableAnnotations(Class<A> annotationType);

}
