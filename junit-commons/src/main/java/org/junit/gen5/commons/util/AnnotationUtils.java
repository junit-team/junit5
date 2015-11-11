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

import static java.util.Arrays.asList;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.commons.util.ReflectionUtils.MethodSortOrder;

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
	 * Find all <em>repeatable</em> {@linkplain Annotation annotations} of
	 * {@code annotationType} that are either <em>present</em>,
	 * <em>indirectly present</em>, or <em>meta-present</em> on the supplied
	 * {@link AnnotatedElement}.
	 * <p>This method extends the functionality of
	 * {@link java.lang.reflect.AnnotatedElement#getAnnotationsByType(Class)}
	 * with additional support for meta-annotations.
	 * @return the list of all such annotations found; never {@code null}
	 */
	public static <A extends Annotation> List<A> findRepeatableAnnotations(AnnotatedElement element,
			Class<A> annotationType) {

		return findRepeatableAnnotations(element, annotationType, new HashSet<>());
	}

	private static <A extends Annotation> List<A> findRepeatableAnnotations(AnnotatedElement element,
			Class<A> annotationType, Set<Annotation> visited) {

		Preconditions.notNull(annotationType, "annotationType must not be null");

		if (element == null) {
			return Collections.emptyList();
		}

		List<A> collectedAnnotations = new ArrayList<>();

		// Directly present?
		collectedAnnotations.addAll(asList(element.getDeclaredAnnotationsByType(annotationType)));

		// Meta-present on directly present annotations?
		for (Annotation candidateAnnotation : element.getDeclaredAnnotations()) {
			if (!isInJavaLangAnnotationPackage(candidateAnnotation) && visited.add(candidateAnnotation)) {
				List<A> metaAnnotations = findRepeatableAnnotations(candidateAnnotation.annotationType(),
					annotationType, visited);
				collectedAnnotations.addAll(metaAnnotations);
			}
		}

		// TODO Ensure that all locally declared annotations are favored over inherited
		// annotations. "Locally declared" includes those that are meta-present on
		// annotations which are directly present.
		// The only way to ensure this is to add sufficient tests for all corner cases in
		// AnnotationUtilsTests.

		// Indirectly present?
		collectedAnnotations.addAll(asList(element.getAnnotationsByType(annotationType)));

		// Meta-present on indirectly present annotations?
		for (Annotation candidateAnnotation : element.getAnnotations()) {
			if (!isInJavaLangAnnotationPackage(candidateAnnotation) && visited.add(candidateAnnotation)) {
				List<A> metaAnnotations = findRepeatableAnnotations(candidateAnnotation.annotationType(),
					annotationType, visited);
				collectedAnnotations.addAll(metaAnnotations);
			}
		}

		return collectedAnnotations;
	}

	/**
	 * Find the first annotation of {@code annotationType} that is either
	 * <em>present</em> or <em>meta-present</em> on the supplied {@code element}.
	 */
	public static <A extends Annotation> Optional<A> findAnnotation(AnnotatedElement element, Class<A> annotationType) {
		return findAnnotation(element, annotationType, new HashSet<Annotation>());
	}

	private static <A extends Annotation> Optional<A> findAnnotation(AnnotatedElement element, Class<A> annotationType,
			Set<Annotation> visited) {

		Preconditions.notNull(annotationType, "annotationType must not be null");

		if (element == null) {
			return Optional.empty();
		}

		// Directly present?
		A annotation = element.getDeclaredAnnotation(annotationType);
		if (annotation != null) {
			return Optional.of(annotation);
		}

		// Meta-present on directly present annotations?
		for (Annotation candidateAnnotation : element.getDeclaredAnnotations()) {
			if (!isInJavaLangAnnotationPackage(candidateAnnotation) && visited.add(candidateAnnotation)) {
				Optional<A> metaAnnotation = findAnnotation(candidateAnnotation.annotationType(), annotationType,
					visited);
				if (metaAnnotation.isPresent()) {
					return metaAnnotation;
				}
			}
		}

		// Indirectly present?
		annotation = element.getAnnotation(annotationType);
		if (annotation != null) {
			return Optional.of(annotation);
		}

		// Meta-present on indirectly present annotations?
		for (Annotation candidateAnnotation : element.getAnnotations()) {
			if (!isInJavaLangAnnotationPackage(candidateAnnotation) && visited.add(candidateAnnotation)) {
				Optional<A> metaAnnotation = findAnnotation(candidateAnnotation.annotationType(), annotationType,
					visited);
				if (metaAnnotation.isPresent()) {
					return metaAnnotation;
				}
			}
		}

		return Optional.empty();
	}

	public static List<Method> findAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotationType,
			MethodSortOrder sortOrder) {

		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(annotationType, "annotationType must not be null");

		return ReflectionUtils.findMethods(clazz, method -> findAnnotation(method, annotationType).isPresent(),
			sortOrder);
	}

	public static boolean isInJavaLangAnnotationPackage(Annotation annotation) {
		return (annotation != null
				&& annotation.annotationType().getPackage().getName().startsWith("java.lang.annotation"));
	}

}
