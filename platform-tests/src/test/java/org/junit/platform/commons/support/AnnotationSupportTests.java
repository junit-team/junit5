/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.commons.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

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
	void isAnnotatedDelegates() throws Throwable {
		assertEquals(AnnotationUtils.isAnnotated(Probe.class, Tag.class),
			AnnotationSupport.isAnnotated(Probe.class, Tag.class));
		assertEquals(AnnotationUtils.isAnnotated(Probe.class, Override.class),
			AnnotationSupport.isAnnotated(Probe.class, Override.class));
	}

	@Test
	void findAnnotationDelegates() throws Throwable {
		assertEquals(AnnotationUtils.findAnnotation(Probe.class, Tag.class),
			AnnotationSupport.findAnnotation(Probe.class, Tag.class));
		assertEquals(AnnotationUtils.findAnnotation(Probe.class, Override.class),
			AnnotationSupport.findAnnotation(Probe.class, Override.class));
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
	void findAnnotatedMethodsDelegates() throws Throwable {
		assertEquals(
			AnnotationUtils.findAnnotatedMethods(Probe.class, Tag.class, ReflectionUtils.MethodSortOrder.HierarchyDown),
			AnnotationSupport.findAnnotatedMethods(Probe.class, Tag.class, MethodSortOrder.HierarchyDown));
		assertEquals(
			AnnotationUtils.findAnnotatedMethods(Probe.class, Tag.class, ReflectionUtils.MethodSortOrder.HierarchyUp),
			AnnotationSupport.findAnnotatedMethods(Probe.class, Tag.class, MethodSortOrder.HierarchyUp));

		assertEquals(
			AnnotationUtils.findAnnotatedMethods(Probe.class, Override.class,
				ReflectionUtils.MethodSortOrder.HierarchyDown),
			AnnotationSupport.findAnnotatedMethods(Probe.class, Override.class, MethodSortOrder.HierarchyDown));
		assertEquals(
			AnnotationUtils.findAnnotatedMethods(Probe.class, Override.class,
				ReflectionUtils.MethodSortOrder.HierarchyUp),
			AnnotationSupport.findAnnotatedMethods(Probe.class, Override.class, MethodSortOrder.HierarchyUp));
	}

	@Test
	void findPublicAnnotatedFieldsDelegates() throws Throwable {
		assertEquals(AnnotationUtils.findPublicAnnotatedFields(Probe.class, String.class, FieldMarker.class),
			AnnotationSupport.findPublicAnnotatedFields(Probe.class, String.class, FieldMarker.class));
		assertEquals(AnnotationUtils.findPublicAnnotatedFields(Probe.class, Throwable.class, Override.class),
			AnnotationSupport.findPublicAnnotatedFields(Probe.class, Throwable.class, Override.class));
	}

	@Target({ ElementType.FIELD })
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
