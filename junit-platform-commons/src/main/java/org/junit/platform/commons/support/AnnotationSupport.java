/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.support;

import static org.apiguardian.api.API.Status.MAINTAINED;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * Common annotation support.
 *
 * @since 1.0
 * @see ClassSupport
 * @see ReflectionSupport
 */
@API(status = MAINTAINED, since = "1.0")
public final class AnnotationSupport {

	///CLOVER:OFF
	private AnnotationSupport() {
		/* no-op */
	}
	///CLOVER:ON

	/**
	 * Determine if an annotation of {@code annotationType} is either
	 * <em>present</em> or <em>meta-present</em> on the supplied
	 * {@code element}.
	 *
	 * @see #findAnnotation(AnnotatedElement, Class)
	 */
	public static boolean isAnnotated(AnnotatedElement element, Class<? extends Annotation> annotationType) {
		return AnnotationUtils.isAnnotated(element, annotationType);
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

		return AnnotationUtils.findAnnotation(element, annotationType);
	}

	/**
	 * Find the first annotation of {@code annotationType} that is either
	 * <em>directly present</em>, <em>meta-present</em>, or <em>indirectly
	 * present</em> on the supplied {@code element}.
	 *
	 * <p>If the element is a class and the annotation is neither <em>directly
	 * present</em> nor <em>meta-present</em> on the class, this method will
	 * additionally search on interfaces implemented by the class before
	 * finding an annotation that is <em>indirectly present</em> on the class.
	 *
	 * @return an {@code Optional} containing the annotation; never {@code null} but
	 * potentially empty
	 */
	public static <A extends Annotation> Optional<A> findAnnotation(AnnotatedElement element, Class<A> annotationType) {
		return AnnotationUtils.findAnnotation(element, annotationType);
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
	 * is {@link java.lang.annotation.Inherited @Inherited}, this method will
	 * search on superclasses first in order to support top-down semantics.
	 * The result is that this algorithm finds repeatable annotations that
	 * would be <em>shadowed</em> and therefore not visible according to Java's
	 * standard semantics for inherited, repeatable annotations, but most
	 * developers will naturally assume that all repeatable annotations in JUnit
	 * are discovered regardless of whether they are declared stand-alone, in a
	 * container, or as a meta-annotation (e.g., multiple declarations of
	 * {@code @ExtendWith} within a test class hierarchy).
	 *
	 * <p>If the element is a class and the repeatable annotation is not
	 * discovered within the class hierarchy, this method will additionally
	 * search on interfaces implemented by each class in the hierarchy.
	 *
	 * <p>If the supplied {@code element} is {@code null}, this method simply
	 * returns an empty list.
	 *
	 * @param element the element to search on, potentially {@code null}
	 * @param annotationType the repeatable annotation type to search for; never {@code null}
	 * @return the list of all such annotations found; neither {@code null} nor mutable
	 * @see java.lang.annotation.Repeatable
	 * @see java.lang.annotation.Inherited
	 */
	public static <A extends Annotation> List<A> findRepeatableAnnotations(AnnotatedElement element,
			Class<A> annotationType) {

		return AnnotationUtils.findRepeatableAnnotations(element, annotationType);
	}

	/**
	 * Find all {@code public} {@linkplain Field fields} of the supplied class
	 * or interface that are of the specified {@code fieldType} and annotated
	 * or <em>meta-annotated</em> with the specified {@code annotationType}.
	 *
	 * <p>Consult the Javadoc for {@link Class#getFields()} for details on
	 * inheritance and ordering.
	 *
	 * @param clazz the class or interface in which to find the fields; never {@code null}
	 * @param fieldType the type of field to find; never {@code null}
	 * @param annotationType the annotation type to search for; never {@code null}
	 * @return the list of all such fields found; neither {@code null} nor mutable
	 * @see Class#getFields()
	 */
	public static List<Field> findPublicAnnotatedFields(Class<?> clazz, Class<?> fieldType,
			Class<? extends Annotation> annotationType) {

		return AnnotationUtils.findPublicAnnotatedFields(clazz, fieldType, annotationType);
	}

	/**
	 * Find all {@linkplain Method methods} of the supplied class or interface
	 * that are annotated or <em>meta-annotated</em> with the specified
	 * {@code annotationType}.
	 *
	 * @param clazz the class or interface in which to find the methods; never {@code null}
	 * @param annotationType the annotation type to search for; never {@code null}
	 * @param traversalMode the hierarchy traversal mode; never {@code null}
	 * @return the list of all such methods found; neither {@code null} nor mutable
	 */
	public static List<Method> findAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotationType,
			HierarchyTraversalMode traversalMode) {

		return AnnotationUtils.findAnnotatedMethods(clazz, annotationType,
			ReflectionUtils.HierarchyTraversalMode.valueOf(traversalMode.name()));
	}

}
