/*
 * Copyright 2015-2017 the original author or authors.
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
import static org.junit.platform.commons.util.CollectionUtils.toUnmodifiableList;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
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

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode;

/**
 * Collection of utilities for working with {@linkplain Annotation annotations}.
 *
 * <h3>DISCLAIMER</h3>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * <p>Some utilities are published via the maintained {@code AnnotationSupport}
 * class.
 *
 * @since 1.0
 * @see Annotation
 * @see AnnotatedElement
 * @see org.junit.platform.commons.support.AnnotationSupport
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
	 * Get the <em>default</em> value of the named attribute from the supplied
	 * {@link Annotation}.
	 *
	 * @param annotation the annotation from which to retrieve the default
	 * value; never {@code null}
	 * @param attributeName the name of the attribute for which the default
	 * value should be retrieved; never {@code null} or empty
	 * @param attributeType the required type of the attribute; never {@code null}
	 * @return an {@code Optional} containing the default value; potentially
	 * <em>empty</em> if the attribute does not have a default value.
	 */
	public static <T> Optional<T> getDefaultValue(Annotation annotation, String attributeName, Class<T> attributeType) {
		Preconditions.notNull(annotation, "Annotation must not be null");
		Preconditions.notBlank(attributeName, "attributeName must not be null or blank");
		Preconditions.notNull(attributeType, "attributeType must not be null");

		Class<? extends Annotation> annotationType = annotation.annotationType();
		Method attribute = null;
		try {
			attribute = annotationType.getDeclaredMethod(attributeName);
		}
		catch (Exception ex) {
			return Optional.empty();
		}

		Object defaultValue = attribute.getDefaultValue();
		if (defaultValue == null) {
			return Optional.empty();
		}

		Preconditions.condition(attributeType.isInstance(defaultValue),
			() -> String.format("Attribute '%s' in annotation %s is of type %s, not %s", attributeName,
				annotationType.getName(), defaultValue.getClass().getName(), attributeType.getName()));

		return Optional.of(attributeType.cast(defaultValue));

	}

	/**
	 * Determine if an annotation of {@code annotationType} is either
	 * <em>present</em> or <em>meta-present</em> on the supplied optional
	 * {@code element}.
	 *
	 * @see #findAnnotation(Optional, Class)
	 */
	public static boolean isAnnotated(Optional<? extends AnnotatedElement> element,
			Class<? extends Annotation> annotationType) {

		return findAnnotation(element, annotationType).isPresent();
	}

	/**
	 * Determine if an annotation of {@code annotationType} is either
	 * <em>present</em> or <em>meta-present</em> on the supplied
	 * {@code element}.
	 *
	 * @see #findAnnotation(AnnotatedElement, Class)
	 * @see org.junit.platform.commons.support.AnnotationSupport#isAnnotated(AnnotatedElement, Class)
	 */
	public static boolean isAnnotated(AnnotatedElement element, Class<? extends Annotation> annotationType) {
		return findAnnotation(element, annotationType).isPresent();
	}

	/**
	 * Find the first annotation of {@code annotationType} that is either
	 * <em>present</em> or <em>meta-present</em> on the supplied optional
	 * {@code element}.
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
	 * @see org.junit.platform.commons.support.AnnotationSupport#findAnnotation(AnnotatedElement, Class)
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

		// Search on interfaces
		if (element instanceof Class) {
			Class<?> clazz = (Class<?>) element;
			for (Class<?> ifc : clazz.getInterfaces()) {
				if (ifc != Annotation.class) {
					Optional<A> annotationOnInterface = findAnnotation(ifc, annotationType, visited);
					if (annotationOnInterface.isPresent()) {
						return annotationOnInterface;
					}
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
		Optional<A> indirectMetaAnnotation = findMetaAnnotation(annotationType, element.getAnnotations(), key, visited);
		if (indirectMetaAnnotation.isPresent()) {
			return indirectMetaAnnotation;
		}

		return Optional.empty();
	}

	private static <A extends Annotation> Optional<A> findMetaAnnotation(Class<A> annotationType,
			Annotation[] candidates, AnnotationCacheKey key, Set<Annotation> visited) {

		for (Annotation candidateAnnotation : candidates) {
			Class<? extends Annotation> candidateAnnotationType = candidateAnnotation.annotationType();
			if (!isInJavaLangAnnotationPackage(candidateAnnotationType) && visited.add(candidateAnnotation)) {
				Optional<A> metaAnnotation = findAnnotation(candidateAnnotationType, annotationType, visited);
				if (metaAnnotation.isPresent()) {
					annotationCache.put(key, metaAnnotation.get());
					return metaAnnotation;
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * @see org.junit.platform.commons.support.AnnotationSupport#findRepeatableAnnotations(AnnotatedElement, Class)
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
		// unmodifiable since returned from public, non-internal method(s)
		return Collections.unmodifiableList(new ArrayList<>(found));
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
			Class<? extends Annotation> candidateAnnotationType = candidate.annotationType();
			if (!isInJavaLangAnnotationPackage(candidateAnnotationType) && visited.add(candidate)) {
				// Exact match?
				if (candidateAnnotationType.equals(annotationType)) {
					found.add(annotationType.cast(candidate));
				}
				// Container?
				else if (candidateAnnotationType.equals(containerType)) {
					// Note: it's not a legitimate containing annotation type if it doesn't declare
					// a 'value' attribute that returns an array of the contained annotation type.
					// See https://docs.oracle.com/javase/specs/jls/se8/html/jls-9.html#jls-9.6.3
					Method method = ReflectionUtils.getMethod(containerType, "value").orElseThrow(
						() -> new JUnitException(String.format(
							"Container annotation type '%s' must declare a 'value' attribute of type %s[].",
							containerType, annotationType)));

					Annotation[] containedAnnotations = (Annotation[]) ReflectionUtils.invokeMethod(method, candidate);
					found.addAll((Collection<? extends A>) asList(containedAnnotations));
				}
				// Otherwise search recursively through the meta-annotation hierarchy...
				else {
					findRepeatableAnnotations(candidateAnnotationType, annotationType, containerType, inherited, found,
						visited);
				}
			}
		}
	}

	/**
	 * @see org.junit.platform.commons.support.AnnotationSupport#findPublicAnnotatedFields(Class, Class, Class)
	 */
	public static List<Field> findPublicAnnotatedFields(Class<?> clazz, Class<?> fieldType,
			Class<? extends Annotation> annotationType) {

		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(fieldType, "fieldType must not be null");
		Preconditions.notNull(annotationType, "annotationType must not be null");

		// @formatter:off
		return Arrays.stream(clazz.getFields())
				.filter(field -> fieldType.isAssignableFrom(field.getType()) && isAnnotated(field, annotationType))
				.collect(toUnmodifiableList());
		// @formatter:on
	}

	/**
	 * @see org.junit.platform.commons.support.AnnotationSupport#findAnnotatedMethods(Class, Class, org.junit.platform.commons.support.HierarchyTraversalMode)
	 */
	public static List<Method> findAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotationType,
			HierarchyTraversalMode traversalMode) {

		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(annotationType, "annotationType must not be null");

		return ReflectionUtils.findMethods(clazz, method -> isAnnotated(method, annotationType), traversalMode);
	}

	private static boolean isInJavaLangAnnotationPackage(Class<? extends Annotation> annotationType) {
		return (annotationType != null && annotationType.getName().startsWith("java.lang.annotation"));
	}

	private static class AnnotationCacheKey {

		private final AnnotatedElement element;
		private final Class<? extends Annotation> annotationType;

		AnnotationCacheKey(AnnotatedElement element, Class<? extends Annotation> annotationType) {
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
