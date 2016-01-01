/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.commons.util;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.junit.gen5.api.Assertions.*;
import static org.junit.gen5.commons.util.AnnotationUtils.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Optional;

import org.junit.Test;

/**
 * Unit tests for {@link AnnotationUtils}.
 *
 * @since 5.0
 */
public final class AnnotationUtilsTests {

	@Test
	public void findAnnotationOnClassWithoutAnnotation() {
		Optional<Annotation2> optionalAnnotation = findAnnotation(Annotation1Class.class, Annotation2.class);
		assertNotNull(optionalAnnotation);
		assertFalse(optionalAnnotation.isPresent());
	}

	@Test
	public void findAnnotationIndirectlyPresentOnClass() {
		Optional<InheritedAnnotation> optionalAnnotation = findAnnotation(SubInheritedAnnotationClass.class,
			InheritedAnnotation.class);
		assertNotNull(optionalAnnotation);
		assertTrue(optionalAnnotation.isPresent());
	}

	@Test
	public void findAnnotationDirectlyPresentOnClass() {
		Optional<Annotation1> optionalAnnotation = findAnnotation(Annotation1Class.class, Annotation1.class);
		assertNotNull(optionalAnnotation);
		assertTrue(optionalAnnotation.isPresent());
	}

	@Test
	public void findAnnotationMetaPresentOnClass() {
		Optional<Annotation1> optionalAnnotation = findAnnotation(ComposedAnnotationClass.class, Annotation1.class);
		assertNotNull(optionalAnnotation);
		assertTrue(optionalAnnotation.isPresent());
	}

	@Test
	public void findAnnotationDirectlyPresentOnMethod() throws Exception {
		Optional<Annotation1> optionalAnnotation = findAnnotation(Annotation2Class.class.getDeclaredMethod("method"),
			Annotation1.class);
		assertNotNull(optionalAnnotation);
		assertTrue(optionalAnnotation.isPresent());
	}

	@Test
	public void findAnnotationMetaPresentOnMethod() throws Exception {
		Optional<Annotation1> optionalAnnotation = findAnnotation(
			ComposedAnnotationClass.class.getDeclaredMethod("method"), Annotation1.class);
		assertNotNull(optionalAnnotation);
		assertTrue(optionalAnnotation.isPresent());
	}

	@Test
	public void findRepeatableAnnotationsWithSingleTag() throws Exception {
		assertTagsFound(SingleTaggedClass.class, "a");
	}

	@Test
	public void findRepeatableAnnotationsWithSingleComposedTag() throws Exception {
		assertTagsFound(SingleComposedTaggedClass.class, "fast");
	}

	@Test
	public void findRepeatableAnnotationsWithMultipleTags() throws Exception {
		assertTagsFound(MultiTaggedClass.class, "a", "b", "c");
	}

	@Test
	public void findRepeatableAnnotationsWithMultipleComposedTags() throws Exception {
		assertTagsFound(MultiComposedTaggedClass.class, "fast", "smoke");
		assertTagsFound(FastAndSmokyTaggedClass.class, "fast", "smoke");
	}

	@Test
	public void findRepeatableAnnotationsWithContainer() throws Exception {
		assertTagsFound(ContainerTaggedClass.class, "a", "b", "c", "d");
	}

	@Test
	public void findRepeatableAnnotationsWithComposedTagBeforeContainer() throws Exception {
		assertTagsFound(ContainerAfterComposedTaggedClass.class, "fast", "a", "b", "c");
	}

	private void assertTagsFound(Class<?> clazz, String... tags) throws Exception {
		assertEquals(asList(tags),
			findRepeatableAnnotations(clazz, Tag.class).stream().map(Tag::value).collect(toList()),
			() -> "Tags found for class " + clazz.getName());
	}

	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@interface Annotation1 {
	}

	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@interface Annotation2 {
	}

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@interface InheritedAnnotation {
	}

	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@Annotation1
	@interface ComposedAnnotation {
	}

	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@interface Tags {

		Tag[]value();
	}

	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@Repeatable(Tags.class)
	@interface Tag {

		String value();
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Tag("fast")
	@interface Fast {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Tag("smoke")
	@interface Smoke {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Fast
	@Smoke
	@interface FastAndSmoky {
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

	@Tag("a")
	static class SingleTaggedClass {
	}

	@Fast
	static class SingleComposedTaggedClass {
	}

	@Tag("a")
	@Tag("b")
	@Tag("c")
	static class MultiTaggedClass {
	}

	@Fast
	@Smoke
	static class MultiComposedTaggedClass {
	}

	@FastAndSmoky
	static class FastAndSmokyTaggedClass {
	}

	@Tags({ @Tag("a"), @Tag("b"), @Tag("c") })
	@Tag("d")
	static class ContainerTaggedClass {
	}

	@Fast
	@Tags({ @Tag("a"), @Tag("b"), @Tag("c") })
	static class ContainerAfterComposedTaggedClass {
	}

}
