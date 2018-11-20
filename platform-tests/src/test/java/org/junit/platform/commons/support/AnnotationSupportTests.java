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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.platform.commons.support.PreconditionViolationChecker.assertPreconditionViolationException;
import static org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode.TOP_DOWN;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * @since 1.0
 */
class AnnotationSupportTests {

	@Test
	void isAnnotated_argument2IsValidated() {
		Optional<Class<Probe>> optional = Optional.of(Probe.class);
		assertPreconditionViolationException("annotationType", () -> AnnotationSupport.isAnnotated(optional, null));
		assertPreconditionViolationException("annotationType", () -> AnnotationSupport.isAnnotated(Probe.class, null));
	}

	@Test
	void findAnnotation_argument2IsValidated() {
		Optional<Class<Probe>> optional = Optional.of(Probe.class);
		assertPreconditionViolationException("annotationType", () -> AnnotationSupport.findAnnotation(optional, null));
		assertPreconditionViolationException("annotationType",
			() -> AnnotationSupport.findAnnotation(Probe.class, null));
	}

	@Test
	void findRepeatableAnnotations_argument2IsValidated() {
		assertPreconditionViolationException("annotationType",
			() -> AnnotationSupport.findRepeatableAnnotations(Probe.class, null));
	}

	@Test
	void findPublicAnnotatedFields_argument1IsValidated() {
		assertPreconditionViolationException("Class",
			() -> AnnotationUtils.findPublicAnnotatedFields(null, String.class, FieldMarker.class));
	}

	@Test
	void findPublicAnnotatedFields_argument2IsValidated() {
		assertPreconditionViolationException("fieldType",
			() -> AnnotationUtils.findPublicAnnotatedFields(Probe.class, null, FieldMarker.class));
	}

	@Test
	void findPublicAnnotatedFields_argument3IsValidated() {
		assertPreconditionViolationException("annotationType",
			() -> AnnotationUtils.findPublicAnnotatedFields(Probe.class, String.class, null));
	}

	@Test
	void findAnnotatedFields_argument1IsValidated() {
		assertPreconditionViolationException("Class",
			() -> AnnotationUtils.findAnnotatedFields(null, Tag.class, arg -> true));
		assertPreconditionViolationException("Class",
			() -> AnnotationUtils.findAnnotatedFields(null, Tag.class, arg -> true, TOP_DOWN));
	}

	@Test
	void findAnnotatedFields_argument2IsValidated() {
		assertPreconditionViolationException("annotationType",
			() -> AnnotationUtils.findAnnotatedFields(Probe.class, null, arg -> true));
		assertPreconditionViolationException("annotationType",
			() -> AnnotationUtils.findAnnotatedFields(Probe.class, null, arg -> true, TOP_DOWN));
	}

	@Test
	void findAnnotatedFields_argument3IsValidated() {
		assertPreconditionViolationException("Predicate",
			() -> AnnotationUtils.findAnnotatedFields(Probe.class, Tag.class, null));
		assertPreconditionViolationException("Predicate",
			() -> AnnotationUtils.findAnnotatedFields(Probe.class, Tag.class, null, TOP_DOWN));
	}

	@Test
	void findAnnotatedMethods_argument1IsValidated() {
		assertPreconditionViolationException("Class",
			() -> AnnotationUtils.findAnnotatedMethods(null, Tag.class, TOP_DOWN));
	}

	@Test
	void findAnnotatedMethods_argument2IsValidated() {
		assertPreconditionViolationException("annotationType",
			() -> AnnotationUtils.findAnnotatedMethods(Probe.class, null, TOP_DOWN));
	}

	@Test
	void findAnnotatedMethods_argument3IsValidated() {
		assertPreconditionViolationException("HierarchyTraversalMode",
			() -> AnnotationUtils.findAnnotatedMethods(Probe.class, Tag.class, null));
	}

	@Test
	void isAnnotatedDelegates() {
		Class<Probe> element = Probe.class;
		Optional<Class<Probe>> optional = Optional.of(element);

		assertEquals(AnnotationUtils.isAnnotated(optional, Tag.class),
			AnnotationSupport.isAnnotated(optional, Tag.class));
		assertEquals(AnnotationUtils.isAnnotated(optional, Override.class),
			AnnotationSupport.isAnnotated(optional, Override.class));

		assertEquals(AnnotationUtils.isAnnotated(element, Tag.class),
			AnnotationSupport.isAnnotated(element, Tag.class));
		assertEquals(AnnotationUtils.isAnnotated(element, Override.class),
			AnnotationSupport.isAnnotated(element, Override.class));
	}

	@Test
	void findAnnotationDelegates() {
		Class<Probe> element = Probe.class;
		Optional<Class<Probe>> optional = Optional.of(element);

		assertEquals(AnnotationUtils.findAnnotation(optional, Tag.class),
			AnnotationSupport.findAnnotation(optional, Tag.class));
		assertEquals(AnnotationUtils.findAnnotation(optional, Override.class),
			AnnotationSupport.findAnnotation(optional, Override.class));

		assertEquals(AnnotationUtils.findAnnotation(element, Tag.class),
			AnnotationSupport.findAnnotation(element, Tag.class));
		assertEquals(AnnotationUtils.findAnnotation(element, Override.class),
			AnnotationSupport.findAnnotation(element, Override.class));
	}

	@Test
	void findRepeatableAnnotationsDelegates() throws Throwable {
		Method bMethod = Probe.class.getDeclaredMethod("bMethod");
		assertEquals(AnnotationUtils.findRepeatableAnnotations(bMethod, Tag.class),
			AnnotationSupport.findRepeatableAnnotations(bMethod, Tag.class));
		Object expected = assertThrows(PreconditionViolationException.class,
			() -> AnnotationUtils.findRepeatableAnnotations(bMethod, Override.class));
		Object actual = assertThrows(PreconditionViolationException.class,
			() -> AnnotationSupport.findRepeatableAnnotations(bMethod, Override.class));
		assertSame(expected.getClass(), actual.getClass(), "expected same exception class");
		assertEquals(expected.toString(), actual.toString(), "expected equal exception toString representation");
	}

	@Test
	void findAnnotatedMethodsDelegates() {
		assertEquals(
			AnnotationUtils.findAnnotatedMethods(Probe.class, Tag.class,
				ReflectionUtils.HierarchyTraversalMode.TOP_DOWN),
			AnnotationSupport.findAnnotatedMethods(Probe.class, Tag.class, HierarchyTraversalMode.TOP_DOWN));
		assertEquals(
			AnnotationUtils.findAnnotatedMethods(Probe.class, Tag.class,
				ReflectionUtils.HierarchyTraversalMode.BOTTOM_UP),
			AnnotationSupport.findAnnotatedMethods(Probe.class, Tag.class, HierarchyTraversalMode.BOTTOM_UP));

		assertEquals(
			AnnotationUtils.findAnnotatedMethods(Probe.class, Override.class,
				ReflectionUtils.HierarchyTraversalMode.TOP_DOWN),
			AnnotationSupport.findAnnotatedMethods(Probe.class, Override.class, HierarchyTraversalMode.TOP_DOWN));
		assertEquals(
			AnnotationUtils.findAnnotatedMethods(Probe.class, Override.class,
				ReflectionUtils.HierarchyTraversalMode.BOTTOM_UP),
			AnnotationSupport.findAnnotatedMethods(Probe.class, Override.class, HierarchyTraversalMode.BOTTOM_UP));
	}

	@Test
	void findPublicAnnotatedFieldsDelegates() {
		assertEquals(AnnotationUtils.findPublicAnnotatedFields(Probe.class, String.class, FieldMarker.class),
			AnnotationSupport.findPublicAnnotatedFields(Probe.class, String.class, FieldMarker.class));
		assertEquals(AnnotationUtils.findPublicAnnotatedFields(Probe.class, Throwable.class, Override.class),
			AnnotationSupport.findPublicAnnotatedFields(Probe.class, Throwable.class, Override.class));
	}

	@Target(ElementType.FIELD)
	@Retention(RetentionPolicy.RUNTIME)
	@interface FieldMarker {
	}

	@Tag("class-tag")
	static class Probe {

		@FieldMarker
		public static String publicStaticAnnotatedField = "static";

		@FieldMarker
		public String publicNormalAnnotatedField = "normal";

		@Tag("method-tag")
		void aMethod() {
		}

		@Tag("method-tag-1")
		@Tag("method-tag-2")
		void bMethod() {
		}
	}

}
