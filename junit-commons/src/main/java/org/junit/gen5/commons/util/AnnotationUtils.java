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

import static java.util.stream.Collectors.toList;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

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

	public static enum MethodSortOrder {
		HierarchyDown, HierarchyUp
	}

	private AnnotationUtils() {
		/* no-op */
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

		Preconditions.notNull(element, "AnnotatedElement must not be null");
		Preconditions.notNull(annotationType, "annotationType must not be null");

		// Directly present?
		A annotation = element.getDeclaredAnnotation(annotationType);
		if (annotation != null) {
			return Optional.of(annotation);
		}

		// Meta-present on directly present annotations?
		for (Annotation candiateAnnotation : element.getDeclaredAnnotations()) {
			if (!isInJavaLangAnnotationPackage(candiateAnnotation) && visited.add(candiateAnnotation)) {
				Optional<A> metaAnnotation = findAnnotation(candiateAnnotation.annotationType(), annotationType,
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

		return findMethods(clazz, method -> findAnnotation(method, annotationType).isPresent(), sortOrder);
	}

	public static List<Method> findMethods(Class<?> clazz, Predicate<Method> predicate, MethodSortOrder sortOrder) {
		Preconditions.notNull(clazz, "Class must not be null");
		Preconditions.notNull(predicate, "predicate must not be null");

		// @formatter:off
		return findAllMethodsInHierarchy(clazz, sortOrder).stream()
				.filter(predicate::test)
				.collect(toList());
		// @formatter:on
	}

	/**
	 * Return all methods in superclass hierarchy except from Object.
	 * Superclass methods are first.
	 */
	public static List<Method> findAllMethodsInHierarchy(Class<?> clazz, MethodSortOrder sortOrder) {
		// TODO Support interface default methods.
		// TODO Determine if we need to support bridged methods.
		List<Method> methods = new ArrayList<>();
		if (sortOrder == MethodSortOrder.HierarchyDown) {
			if (clazz.getSuperclass() != Object.class) {
				methods.addAll(findAllMethodsInHierarchy(clazz.getSuperclass(), sortOrder));
			}
		}
		methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
		if (sortOrder == MethodSortOrder.HierarchyUp) {
			if (clazz.getSuperclass() != Object.class) {
				methods.addAll(findAllMethodsInHierarchy(clazz.getSuperclass(), sortOrder));
			}
		}
		return methods;
	}

	public static boolean isInJavaLangAnnotationPackage(Annotation annotation) {
		return (annotation != null
				&& annotation.annotationType().getPackage().getName().startsWith("java.lang.annotation"));
	}

}
