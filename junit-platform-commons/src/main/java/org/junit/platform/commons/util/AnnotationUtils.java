/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.commons.util;

import static java.util.Arrays.asList;
import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.ReflectionUtils.MethodSortOrder;

/**
 * Collection of utilities for working with {@linkplain Annotation annotations}.
 *
 * <h3>DISCLAIMER</h3>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 1.0
 * @see Annotation
 * @see AnnotatedElement
 */
@API(Internal)
public final class AnnotationUtils {

	///CLOVER:OFF
	private AnnotationUtils() {
		/* no-op */
	}
	///CLOVER:ON

	private static final Map<AnnotationCacheKey, Annotation> annotationCache = new ConcurrentHashMap<>(256);

	/**
	 * Determine if an annotation of {@code annotationType} is either <em>present</em> or <em>meta-present</em> on the
	 * supplied {@code element}.
	 *
	 * @see #findAnnotation(AnnotatedElement, Class)
	 */
	public static boolean isAnnotated(AnnotatedElement element, Class<? extends Annotation> annotationType) {
		return findAnnotation(element, annotationType).isPresent();
	}

	/**
	 * Find the first annotation of {@code annotationType} that is either <em>present</em> or <em>meta-present</em> on
	 * the supplied optional {@code element}.
	 *
	 * @see #findAnnotation(AnnotatedElement, Class)
	 */
	public static <A extends Annotation> Optional<A> findAnnotation(Optional<? extends AnnotatedElement> element,
			Class<A> annotationType) {

		if (element == null || !element.isPresent()) {
			return Optional.empty();
		}

		return findAnnotation(element.get(), annotationType, new HashSet<>());
	}

	/**
	 * Find the first annotation of {@code annotationType} that is either <em>present</em> or <em>meta-present</em> on
	 * the supplied {@code element}.
	 */
	public static <A extends Annotation> Optional<A> findAnnotation(AnnotatedElement element, Class<A> annotationType) {
		return findAnnotation(element, annotationType, new HashSet<>());
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
		Optional<A> directMetaAnnotation = findMetaAnnotation(annotationType, element.getDeclaredAnnotations(), key,
			visited);
		if (directMetaAnnotation.isPresent()) {
			return directMetaAnnotation;
		}

		// Indirectly present?
		annotation = element.getAnnotation(annotationType);
		if (annotation != null) {
			annotationCache.put(key, annotation);
			return Optional.of(annotation);
		}

		// Meta-present on indirectly present annotations?
		Optional<A> indirectMetaAnnotation = findMetaAnnotation(annotationType, element.getAnnotations(), key, visited);
		if (indirectMetaAnnotation.isPresent()) {
			return indirectMetaAnnotation;
		}

		return Optional.empty();
	}

	private static <A extends Annotation> Optional<A> findMetaAnnotation(Class<A> annotationType,
			Annotation[] candidates, AnnotationCacheKey key, Set<Annotation> visited) {

		for (Annotation candidateAnnotation : candidates) {
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
	 * {@code annotationType} that are either <em>present</em>, <em>indirectly
	 * present</em>, or <em>meta-present</em> on the supplied {@link AnnotatedElement}.
	 *
	 * <p>This method extends the functionality of
	 * {@link java.lang.reflect.AnnotatedElement#getAnnotationsByType(Class)}
	 * with additional support for meta-annotations.
	 *
	 * <p>In addition, if the element is a class and the repeatable annotation
	 * is {@link Inherited @Inherited}, this method will search on superclasses
	 * first in order to support top-down semantics. The result is that this
	 * algorithm finds repeatable annotations that would be <em>shadowed</em>
	 * and therefore not visible according to Java's standard semantics for
	 * inherited, repeatable annotations, but most developers will naturally
	 * assume that all repeatable annotations in JUnit are discovered regardless
	 * of whether they are declared stand-alone, in a container, or as a
	 * meta-annotation (e.g., multiple declarations of {@code @ExtendWith}
	 * within a test class hierarchy).
	 *
	 * <p>If the supplied {@code element} is {@code null}, this method simply
	 * returns an empty list.
	 *
	 * @param element the element to search on, potentially {@code null}
	 * @param annotationType the repeatable annotation type to search for; never {@code null}
	 * @return the list of all such annotations found; never {@code null}
	 * @see Repeatable
	 * @see Inherited
	 */
	public static <A extends Annotation> List<A> findRepeatableAnnotations(AnnotatedElement element,
			Class<A> annotationType) {

		Preconditions.notNull(annotationType, "annotationType must not be null");
		Repeatable repeatable = annotationType.getAnnotation(Repeatable.class);
		Preconditions.notNull(repeatable, () -> annotationType.getName() + " must be @Repeatable");
		Class<? extends Annotation> containerType = repeatable.value();
		boolean inherited = containerType.isAnnotationPresent(Inherited.class);

		// Short circuit the search algorithm.
		if (element == null) {
			return Collections.emptyList();
		}

		// We use a LinkedHashSet because the search algorithm may discover
		// duplicates, but we need to maintain the original order.
		Set<A> found = new LinkedHashSet<>(16);
		findRepeatableAnnotations(element, annotationType, containerType, inherited, found, new HashSet<>(16));
		return new ArrayList<>(found);
	}

	private static <A extends Annotation> void findRepeatableAnnotations(AnnotatedElement element,
			Class<A> annotationType, Class<? extends Annotation> containerType, boolean inherited, Set<A> found,
			Set<Annotation> visited) {

		if (element instanceof Class) {
			Class<?> clazz = (Class<?>) element;

			// Recurse first in order to support top-down semantics for inherited, repeatable annotations.
			if (inherited) {
				Class<?> superclass = clazz.getSuperclass();
				if (superclass != null && superclass != Object.class) {
					findRepeatableAnnotations(superclass, annotationType, containerType, inherited, found, visited);
				}
			}

			// Search on interfaces
			for (Class<?> ifc : clazz.getInterfaces()) {
				if (ifc != Annotation.class) {
					findRepeatableAnnotations(ifc, annotationType, containerType, inherited, found, visited);
				}
			}
		}

		// Find annotations that are directly present or meta-present on directly present annotations.
		findRepeatableAnnotations(element.getDeclaredAnnotations(), annotationType, containerType, inherited, found,
			visited);

		// Find annotations that are indirectly present or meta-present on indirectly present annotations.
		findRepeatableAnnotations(element.getAnnotations(), annotationType, containerType, inherited, found, visited);
	}

	@SuppressWarnings("unchecked")
	private static <A extends Annotation> void findRepeatableAnnotations(Annotation[] candidates,
			Class<A> annotationType, Class<? extends Annotation> containerType, boolean inherited, Set<A> found,
			Set<Annotation> visited) {

		for (Annotation candidate : candidates) {
			if (!isInJavaLangAnnotationPackage(candidate) && visited.add(candidate)) {
				// Exact match?
				if (candidate.annotationType().equals(annotationType)) {
					found.add(annotationType.cast(candidate));
				}
				// Container?
				else if (candidate.annotationType().equals(containerType)) {
					// Note: it's not a legitimate container annotation if it doesn't declare
					// a 'value' attribute that returns an array of the contained annotation type.
					// Thus we proceed without verifying this assumption.
					Method method = ReflectionUtils.getMethod(containerType, "value").get();
					Annotation[] containedAnnotations = (Annotation[]) ReflectionUtils.invokeMethod(method, candidate);
					found.addAll((Collection<? extends A>) asList(containedAnnotations));
				}
				// Otherwise search recursively through the meta-annotation hierarchy...
				else {
					findRepeatableAnnotations(candidate.annotationType(), annotationType, containerType, inherited,
						found, visited);
				}
			}
		}
	}

	public static List<Method> findAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotationType,
			MethodSortOrder sortOrder) {

		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(annotationType, "annotationType must not be null");

		return ReflectionUtils.findMethods(clazz, method -> isAnnotated(method, annotationType), sortOrder);
	}

	private static boolean isInJavaLangAnnotationPackage(Annotation annotation) {
		return (annotation != null && annotation.annotationType().getName().startsWith("java.lang.annotation"));
	}

	private static class AnnotationCacheKey implements Serializable {

		private static final long serialVersionUID = 1L;

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
