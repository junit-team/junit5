/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static java.util.Arrays.asList;
import static org.apiguardian.api.API.Status.INTERNAL;
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
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.platform.commons.JUnitException;
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
@API(status = INTERNAL, since = "1.0")
public final class AnnotationUtils {

	private AnnotationUtils() {
		/* no-op */
	}

	private static final ConcurrentHashMap<Class<? extends Annotation>, Boolean> repeatableAnnotationContainerCache = //
		new ConcurrentHashMap<>(16);

	/**
	 * Determine if an annotation of {@code annotationType} is either
	 * <em>present</em> or <em>meta-present</em> on the supplied optional
	 * {@code element}.
	 *
	 * @see #findAnnotation(Optional, Class)
	 * @see org.junit.platform.commons.support.AnnotationSupport#isAnnotated(Optional, Class)
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
	 * @param element the element on which to search for the annotation; may be
	 * {@code null}
	 * @param annotationType the annotation type to search for; never {@code null}
	 * @return {@code true} if the annotation is present or meta-present
	 * @see #findAnnotation(AnnotatedElement, Class)
	 * @see org.junit.platform.commons.support.AnnotationSupport#isAnnotated(AnnotatedElement, Class)
	 */
	public static boolean isAnnotated(AnnotatedElement element, Class<? extends Annotation> annotationType) {
		return findAnnotation(element, annotationType).isPresent();
	}

	/**
	 * @see org.junit.platform.commons.support.AnnotationSupport#findAnnotation(Optional, Class)
	 */
	public static <A extends Annotation> Optional<A> findAnnotation(Optional<? extends AnnotatedElement> element,
			Class<A> annotationType) {

		if (element == null || !element.isPresent()) {
			return Optional.empty();
		}

		return findAnnotation(element.get(), annotationType);
	}

	/**
	 * @see org.junit.platform.commons.support.AnnotationSupport#findAnnotation(AnnotatedElement, Class)
	 */
	public static <A extends Annotation> Optional<A> findAnnotation(AnnotatedElement element, Class<A> annotationType) {
		Preconditions.notNull(annotationType, "annotationType must not be null");
		boolean inherited = annotationType.isAnnotationPresent(Inherited.class);
		return findAnnotation(element, annotationType, inherited, new HashSet<>());
	}

	private static <A extends Annotation> Optional<A> findAnnotation(AnnotatedElement element, Class<A> annotationType,
			boolean inherited, Set<Annotation> visited) {

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
		Optional<A> directMetaAnnotation = findMetaAnnotation(annotationType, element.getDeclaredAnnotations(),
			inherited, visited);
		if (directMetaAnnotation.isPresent()) {
			return directMetaAnnotation;
		}

		if (element instanceof Class) {
			Class<?> clazz = (Class<?>) element;

			// Search on interfaces
			for (Class<?> ifc : clazz.getInterfaces()) {
				if (ifc != Annotation.class) {
					Optional<A> annotationOnInterface = findAnnotation(ifc, annotationType, inherited, visited);
					if (annotationOnInterface.isPresent()) {
						return annotationOnInterface;
					}
				}
			}

			// Indirectly present?
			// Search in class hierarchy
			if (inherited) {
				Class<?> superclass = clazz.getSuperclass();
				if (superclass != null && superclass != Object.class) {
					Optional<A> annotationOnSuperclass = findAnnotation(superclass, annotationType, inherited, visited);
					if (annotationOnSuperclass.isPresent()) {
						return annotationOnSuperclass;
					}
				}
			}
		}

		// Meta-present on indirectly present annotations?
		return findMetaAnnotation(annotationType, element.getAnnotations(), inherited, visited);
	}

	private static <A extends Annotation> Optional<A> findMetaAnnotation(Class<A> annotationType,
			Annotation[] candidates, boolean inherited, Set<Annotation> visited) {

		for (Annotation candidateAnnotation : candidates) {
			Class<? extends Annotation> candidateAnnotationType = candidateAnnotation.annotationType();
			if (!isInJavaLangAnnotationPackage(candidateAnnotationType) && visited.add(candidateAnnotation)) {
				Optional<A> metaAnnotation = findAnnotation(candidateAnnotationType, annotationType, inherited,
					visited);
				if (metaAnnotation.isPresent()) {
					return metaAnnotation;
				}
			}
		}
		return Optional.empty();
	}

	/**
	 * @since 1.5
	 * @see org.junit.platform.commons.support.AnnotationSupport#findRepeatableAnnotations(Optional, Class)
	 */
	public static <A extends Annotation> List<A> findRepeatableAnnotations(Optional<? extends AnnotatedElement> element,
			Class<A> annotationType) {

		if (element == null || !element.isPresent()) {
			return Collections.emptyList();
		}

		return findRepeatableAnnotations(element.get(), annotationType);
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
					Method method = ReflectionUtils.tryToGetMethod(containerType, "value").getOrThrow(
						cause -> new JUnitException(String.format(
							"Container annotation type '%s' must declare a 'value' attribute of type %s[].",
							containerType, annotationType), cause));

					Annotation[] containedAnnotations = (Annotation[]) ReflectionUtils.invokeMethod(method, candidate);
					found.addAll((Collection<? extends A>) asList(containedAnnotations));
				}
				// Nested container annotation?
				else if (isRepeatableAnnotationContainer(candidateAnnotationType)) {
					Method method = ReflectionUtils.tryToGetMethod(candidateAnnotationType, "value").toOptional().get();
					Annotation[] containedAnnotations = (Annotation[]) ReflectionUtils.invokeMethod(method, candidate);

					for (Annotation containedAnnotation : containedAnnotations) {
						findRepeatableAnnotations(containedAnnotation.getClass(), annotationType, containerType,
							inherited, found, visited);
					}
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
	 * Determine if the supplied annotation type is a container for a repeatable
	 * annotation.
	 *
	 * @since 1.5
	 */
	private static boolean isRepeatableAnnotationContainer(Class<? extends Annotation> candidateContainerType) {
		return repeatableAnnotationContainerCache.computeIfAbsent(candidateContainerType, candidate -> {
			// @formatter:off
			Repeatable repeatable = Arrays.stream(candidate.getMethods())
					.filter(attribute -> attribute.getName().equals("value") && attribute.getReturnType().isArray())
					.findFirst()
					.map(attribute -> attribute.getReturnType().getComponentType().getAnnotation(Repeatable.class))
					.orElse(null);
			// @formatter:on

			return repeatable != null && candidate.equals(repeatable.value());
		});
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
	 * Find all {@linkplain Field fields} of the supplied class or interface
	 * that are annotated or <em>meta-annotated</em> with the specified
	 * {@code annotationType} and match the specified {@code predicate}, using
	 * top-down search semantics within the type hierarchy.
	 *
	 * @see #findAnnotatedFields(Class, Class, Predicate, HierarchyTraversalMode)
	 */
	public static List<Field> findAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annotationType,
			Predicate<Field> predicate) {

		return findAnnotatedFields(clazz, annotationType, predicate, HierarchyTraversalMode.TOP_DOWN);
	}

	/**
	 * Find all {@linkplain Field fields} of the supplied class or interface
	 * that are annotated or <em>meta-annotated</em> with the specified
	 * {@code annotationType} and match the specified {@code predicate}.
	 *
	 * @param clazz the class or interface in which to find the fields; never {@code null}
	 * @param annotationType the annotation type to search for; never {@code null}
	 * @param predicate the field filter; never {@code null}
	 * @param traversalMode the hierarchy traversal mode; never {@code null}
	 * @return the list of all such fields found; neither {@code null} nor mutable
	 */
	public static List<Field> findAnnotatedFields(Class<?> clazz, Class<? extends Annotation> annotationType,
			Predicate<Field> predicate, HierarchyTraversalMode traversalMode) {

		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(annotationType, "annotationType must not be null");
		Preconditions.notNull(predicate, "Predicate must not be null");

		Predicate<Field> annotated = field -> isAnnotated(field, annotationType);

		return ReflectionUtils.findFields(clazz, annotated.and(predicate), traversalMode);
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

}
