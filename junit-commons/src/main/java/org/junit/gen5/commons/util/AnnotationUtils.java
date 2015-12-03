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

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.gen5.commons.util.ReflectionUtils.MethodSortOrder;

/**
 * Collection of utilities for working with {@linkplain Annotation annotations}.
 *
 * @since 5.0
 * @see Annotation
 * @see AnnotatedElement
 */
public final class AnnotationUtils {

	private static final Map<AnnotationCacheKey, Annotation> annotationCache = new ConcurrentHashMap<>(256);

	private AnnotationUtils() {
		/* no-op */
	}

	/**
	 * Determine if an annotation of {@code annotationType} is either
	 * <em>present</em> or <em>meta-present</em> on the supplied optional
	 * {@code element}.
	 * @see #findAnnotation(Optional, Class)
	 */
	public static boolean isAnnotated(Optional<? extends AnnotatedElement> element,
			Class<? extends Annotation> annotationType) {

		return findAnnotation(element, annotationType).isPresent();
	}

	/**
	 * Determine if an annotation of {@code annotationType} is either
	 * <em>present</em> or <em>meta-present</em> on the supplied {@code element}.
	 * @see #findAnnotation(AnnotatedElement, Class)
	 */
	public static boolean isAnnotated(AnnotatedElement element, Class<? extends Annotation> annotationType) {
		return findAnnotation(element, annotationType).isPresent();
	}

	/**
	 * Find the first annotation of {@code annotationType} that is either
	 * <em>present</em> or <em>meta-present</em> on the supplied optional
	 * {@code element}.
	 * @see #findAnnotation(AnnotatedElement, Class)
	 */
	public static <A extends Annotation> Optional<A> findAnnotation(Optional<? extends AnnotatedElement> element,
			Class<A> annotationType) {

		if (element == null || !element.isPresent()) {
			return Optional.empty();
		}

		return findAnnotation(element.get(), annotationType, new HashSet<Annotation>());
	}

	/**
	 * Find the first annotation of {@code annotationType} that is either
	 * <em>present</em> or <em>meta-present</em> on the supplied {@code element}.
	 * @see #findAnnotation(Optional, Class)
	 */
	public static <A extends Annotation> Optional<A> findAnnotation(AnnotatedElement element, Class<A> annotationType) {
		return findAnnotation(element, annotationType, new HashSet<Annotation>());
	}

	@SuppressWarnings("unchecked")
	private static <A extends Annotation> Optional<A> findAnnotation(AnnotatedElement element, Class<A> annotationType,
			Set<Annotation> visited) {

		Preconditions.notNull(annotationType, "annotationType must not be null");

		if (element == null) {
			return Optional.empty();
		}

		// Cached?
		AnnotationCacheKey key = new AnnotationCacheKey(element, annotationType);
		A annotation = (A) annotationCache.get(key);
		if (annotation != null) {
			return Optional.of(annotation);
		}

		// Directly present?
		annotation = element.getDeclaredAnnotation(annotationType);
		if (annotation != null) {
			annotationCache.put(key, annotation);
			return Optional.of(annotation);
		}

		// Meta-present on directly present annotations?
		for (Annotation candidateAnnotation : element.getDeclaredAnnotations()) {
			if (!isInJavaLangAnnotationPackage(candidateAnnotation) && visited.add(candidateAnnotation)) {
				Optional<A> metaAnnotation = findAnnotation(candidateAnnotation.annotationType(), annotationType,
					visited);
				if (metaAnnotation.isPresent()) {
					annotationCache.put(key, metaAnnotation.get());
					return metaAnnotation;
				}
			}
		}

		// Indirectly present?
		annotation = element.getAnnotation(annotationType);
		if (annotation != null) {
			annotationCache.put(key, annotation);
			return Optional.of(annotation);
		}

		// Meta-present on indirectly present annotations?
		for (Annotation candidateAnnotation : element.getAnnotations()) {
			if (!isInJavaLangAnnotationPackage(candidateAnnotation) && visited.add(candidateAnnotation)) {
				Optional<A> metaAnnotation = findAnnotation(candidateAnnotation.annotationType(), annotationType,
					visited);
				if (metaAnnotation.isPresent()) {
					annotationCache.put(key, metaAnnotation.get());
					return metaAnnotation;
				}
			}
		}

		return Optional.empty();
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
		//
		// The only way to ensure this is to add sufficient tests for all corner cases in
		// AnnotationUtilsTests.
		//
		// Indirectly present?
		// collectedAnnotations.addAll(asList(element.getAnnotationsByType(annotationType)));

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

	public static List<Method> findAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotationType,
			MethodSortOrder sortOrder) {

		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(annotationType, "annotationType must not be null");

		return ReflectionUtils.findMethods(clazz, method -> isAnnotated(method, annotationType), sortOrder);
	}

	public static boolean isInJavaLangAnnotationPackage(Annotation annotation) {
		return (annotation != null
				&& annotation.annotationType().getPackage().getName().startsWith("java.lang.annotation"));
	}

	private static class AnnotationCacheKey implements Serializable {

		private static final long serialVersionUID = 4611807332019442648L;

		private final AnnotatedElement element;
		private final Class<? extends Annotation> annotationType;

		public AnnotationCacheKey(AnnotatedElement element, Class<? extends Annotation> annotationType) {
			this.element = element;
			this.annotationType = annotationType;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof AnnotationCacheKey) {
				AnnotationCacheKey that = (AnnotationCacheKey) obj;
				return Objects.equals(this.element, that.element)
						&& Objects.equals(this.annotationType, that.annotationType);
			}
			return false;
		}

		@Override
		public int hashCode() {
			return Objects.hash(this.element, this.annotationType);
		}

	}

}
