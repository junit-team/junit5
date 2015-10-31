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

import static org.junit.Assert.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;

import org.junit.Test;

/**
 * Unit tests for {@link AnnotationUtils}.
 *
 * @author Sam Brannen
 * @since 5.0
 */
public final class AnnotationUtilsTests {

	@Test
	public void findAnnotationOnClassWithoutAnnotation() {
		Optional<Annotation2> optionalAnnotation = AnnotationUtils.findAnnotation(Annotation1Class.class,
			Annotation2.class);
		assertNotNull(optionalAnnotation);
		assertFalse(optionalAnnotation.isPresent());
	}

	@Test
	public void findAnnotationIndirectlyPresentOnClass() {
		Optional<InheritedAnnotation> optionalAnnotation = AnnotationUtils.findAnnotation(
			SubInheritedAnnotationClass.class, InheritedAnnotation.class);
		assertNotNull(optionalAnnotation);
		assertTrue(optionalAnnotation.isPresent());
	}

	@Test
	public void findAnnotationDirectlyPresentOnClass() {
		Optional<Annotation1> optionalAnnotation = AnnotationUtils.findAnnotation(Annotation1Class.class,
			Annotation1.class);
		assertNotNull(optionalAnnotation);
		assertTrue(optionalAnnotation.isPresent());
	}

	@Test
	public void findAnnotationMetaPresentOnClass() {
		Optional<Annotation1> optionalAnnotation = AnnotationUtils.findAnnotation(ComposedAnnotationClass.class,
			Annotation1.class);
		assertNotNull(optionalAnnotation);
		assertTrue(optionalAnnotation.isPresent());
	}

	@Test
	public void findAnnotationDirectlyPresentOnMethod() throws Exception {
		Optional<Annotation1> optionalAnnotation = AnnotationUtils.findAnnotation(
			Annotation2Class.class.getDeclaredMethod("method"), Annotation1.class);
		assertNotNull(optionalAnnotation);
		assertTrue(optionalAnnotation.isPresent());
	}

	@Test
	public void findAnnotationMetaPresentOnMethod() throws Exception {
		Optional<Annotation1> optionalAnnotation = AnnotationUtils.findAnnotation(
			ComposedAnnotationClass.class.getDeclaredMethod("method"), Annotation1.class);
		assertNotNull(optionalAnnotation);
		assertTrue(optionalAnnotation.isPresent());
	}

	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	static @interface Annotation1 {
	}

	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	static @interface Annotation2 {
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	static @interface InheritedAnnotation {
	}

	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@Annotation1
	static @interface ComposedAnnotation {
	}

	@Annotation1
	static class Annotation1Class {
	}

	@Annotation2
	static class Annotation2Class {

		@Annotation1
		void method() {
		}
	}

	@InheritedAnnotation
	static class InheritedAnnotationClass {
	}

	static class SubInheritedAnnotationClass extends InheritedAnnotationClass {
	}

	@ComposedAnnotation
	static class ComposedAnnotationClass {

		@ComposedAnnotation
		void method() {
		}
	}

}
