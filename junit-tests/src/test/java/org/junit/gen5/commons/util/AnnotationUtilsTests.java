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
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertFalse;
import static org.junit.gen5.api.Assertions.assertNotNull;
import static org.junit.gen5.api.Assertions.assertThrows;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.junit.gen5.commons.util.AnnotationUtils.findAnnotatedMethods;
import static org.junit.gen5.commons.util.AnnotationUtils.findAnnotation;
import static org.junit.gen5.commons.util.AnnotationUtils.findRepeatableAnnotations;
import static org.junit.gen5.commons.util.AnnotationUtils.isAnnotated;
import static org.junit.gen5.commons.util.ReflectionUtils.MethodSortOrder.HierarchyDown;
import static org.junit.gen5.commons.util.ReflectionUtils.MethodSortOrder.HierarchyUp;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;

import org.junit.gen5.api.Test;

/**
 * Unit tests for {@link AnnotationUtils}.
 *
 * @since 5.0
 */
public final class AnnotationUtilsTests {

	@Test
	public void findAnnotationForNullAnnotatedElement() {
		assertThat(findAnnotation(null, Annotation1.class)).isEmpty();
	}

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

	/**
	 * <b>Note:</b> there is no findAnnotationIndirectlyMetaPresentOnMethod counterpart because {@link Inherited}
	 * annotation has no effect if the annotated type is used to annotate anything other than a class.
	 *
	 * @see Inherited
	 */
	@Test
	public void findAnnotationIndirectlyMetaPresentOnClass() {
		assertThat(findAnnotation(InheritedComposedAnnotationSubClass.class, Annotation1.class)).isPresent();
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
	public void isAnnotatedWhenClassWithoutAnnotation() {
		assertFalse(isAnnotated(Annotation1Class.class, Annotation2.class));
	}

	@Test
	public void isAnnotatedWhenIndirectlyPresentOnClass() {
		assertTrue(isAnnotated(SubInheritedAnnotationClass.class, InheritedAnnotation.class));
	}

	@Test
	public void isAnnotatedWhenDirectlyPresentOnClass() {
		assertTrue(isAnnotated(Annotation1Class.class, Annotation1.class));
	}

	@Test
	public void isAnnotatedWhenMetaPresentOnClass() {
		assertTrue(isAnnotated(ComposedAnnotationClass.class, Annotation1.class));
	}

	@Test
	public void isAnnotatedWhenDirectlyPresentOnMethod() throws Exception {
		assertTrue(isAnnotated(Annotation2Class.class.getDeclaredMethod("method"), Annotation1.class));
	}

	@Test
	public void isAnnotatedWhenMetaPresentOnMethod() throws Exception {
		assertTrue(isAnnotated(ComposedAnnotationClass.class.getDeclaredMethod("method"), Annotation1.class));
	}

	@Test
	public void findRepeatableAnnotationsForNullAnnotatedElement() {
		assertThat(findRepeatableAnnotations(null, Tag.class)).isEmpty();
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
	public void findRepeatableAnnotationsWithSingleComposedTagOnImplementedInterface() throws Exception {
		assertTagsFound(TaggedInterfaceClass.class, "fast");
	}

	@Test
	public void findRepeatableAnnotationsWithLocalComposedTagAndComposedTagOnImplementedInterface() throws Exception {
		assertTagsFound(LocalTagOnTaggedInterfaceClass.class, "fast", "smoke");
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

	@Test
	public void findInheritedRepeatableAnnotationsWithSingleAnnotationOnSuperclass() throws Exception {
		assertExtensionsFound(SingleExtensionClass.class, "a");
		assertExtensionsFound(SubSingleExtensionClass.class, "a");
	}

	@Test
	public void findInheritedRepeatableAnnotationsWithMultipleAnnotationsOnSuperclass() throws Exception {
		assertExtensionsFound(MultiExtensionClass.class, "a", "b", "c");
		assertExtensionsFound(SubMultiExtensionClass.class, "a", "b", "c", "x", "y", "z");
	}

	@Test
	public void findInheritedRepeatableAnnotationsWithContainerAnnotationOnSuperclass() throws Exception {
		assertExtensionsFound(ContainerExtensionClass.class, "a", "b", "c");
		assertExtensionsFound(SubContainerExtensionClass.class, "a", "b", "c", "x");
	}

	@Test
	public void findInheritedRepeatableAnnotationsWithSingleComposedAnnotation() throws Exception {
		assertExtensionsFound(SingleComposedExtensionClass.class, "foo");
	}

	@Test
	public void findInheritedRepeatableAnnotationsWithSingleComposedAnnotationOnSuperclass() throws Exception {
		assertExtensionsFound(SubSingleComposedExtensionClass.class, "foo");
	}

	@Test
	public void findInheritedRepeatableAnnotationsWithMultipleComposedAnnotations() throws Exception {
		assertExtensionsFound(MultiComposedExtensionClass.class, "foo", "bar");
	}

	@Test
	public void findInheritedRepeatableAnnotationsWithMultipleComposedAnnotationsOnSuperclass() throws Exception {
		assertExtensionsFound(SubMultiComposedExtensionClass.class, "foo", "bar");
	}

	@Test
	public void findInheritedRepeatableAnnotationsWithMultipleComposedAnnotationsOnSuperclassAndLocalContainerAndComposed()
			throws Exception {
		assertExtensionsFound(ContainerPlusSubMultiComposedExtensionClass.class, "foo", "bar", "x", "y", "z");
	}

	private void assertExtensionsFound(Class<?> clazz, String... tags) throws Exception {
		assertEquals(asList(tags),
			findRepeatableAnnotations(clazz, ExtendWith.class).stream().map(ExtendWith::value).collect(toList()),
			() -> "Extensions found for class " + clazz.getName());
	}

	@Test
	public void findAnnotatedMethodsForNullClass() {
		assertThrows(PreconditionViolationException.class,
			() -> findAnnotatedMethods(null, Annotation1.class, HierarchyDown));
	}

	@Test
	public void findAnnotatedMethodsForNullAnnotationType() {
		assertThrows(PreconditionViolationException.class,
			() -> findAnnotatedMethods(ClassWithAnnotatedMethods.class, null, HierarchyDown));
	}

	@Test
	public void findAnnotatedMethodsForUnusedAnnotation() {
		assertThat(findAnnotatedMethods(ClassWithAnnotatedMethods.class, Fast.class, HierarchyDown)).isEmpty();
	}

	@Test
	public void findAnnotatedMethodsForAnnotationUsedInClassOnly() throws Exception {
		Method method2 = ClassWithAnnotatedMethods.class.getMethod("method2");
		Method method3 = ClassWithAnnotatedMethods.class.getMethod("method3");

		List<Method> methods = findAnnotatedMethods(ClassWithAnnotatedMethods.class, Annotation2.class, HierarchyDown);

		assertThat(methods).containsOnly(method2, method3);
	}

	@Test
	public void findAnnotatedMethodsForAnnotationUsedInClassAndSuperClassHierarchyUp() throws Exception {
		List<Method> methods = findAnnotatedMethods(ClassWithAnnotatedMethods.class, Annotation1.class, HierarchyUp);

		assertEquals(3, methods.size());
		assertThat(methods.subList(0, 2)).containsOnly(ClassWithAnnotatedMethods.class.getMethod("method1"),
			ClassWithAnnotatedMethods.class.getMethod("method3"));
		assertEquals(ClassWithAnnotatedMethods.class.getMethod("superMethod"), methods.get(2));
	}

	@Test
	public void findAnnotatedMethodsForAnnotationUsedInClassAndSuperClassHierarchyDown() throws Exception {
		List<Method> methods = findAnnotatedMethods(ClassWithAnnotatedMethods.class, Annotation1.class, HierarchyDown);

		assertEquals(3, methods.size());
		assertEquals(ClassWithAnnotatedMethods.class.getMethod("superMethod"), methods.get(0));
		assertThat(methods.subList(1, 3)).containsOnly(ClassWithAnnotatedMethods.class.getMethod("method1"),
			ClassWithAnnotatedMethods.class.getMethod("method3"));
	}

	@Test
	public void findAnnotatedMethodsForAnnotationUsedInInterface() throws Exception {
		List<Method> methods = findAnnotatedMethods(ClassWithAnnotatedMethods.class, Annotation3.class, HierarchyUp);

		assertThat(methods).containsExactly(ClassWithAnnotatedMethods.class.getMethod("interfaceMethod"));
	}

	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@interface Annotation1 {
	}

	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@interface Annotation2 {
	}

	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@interface Annotation3 {
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
	@Annotation1
	@Inherited
	@interface InheritedComposedAnnotation {
	}

	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@interface Tags {

		Tag[] value();
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

	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	public @interface Extensions {

		ExtendWith[] value();
	}

	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@Repeatable(Extensions.class)
	public @interface ExtendWith {

		String value();
	}

	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@ExtendWith("foo")
	public @interface ExtendWithFoo {
	}

	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@ExtendWith("bar")
	public @interface ExtendWithBar {
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

	@InheritedComposedAnnotation
	static class InheritedComposedAnnotationClass {

		@InheritedComposedAnnotation
		public void method() {
		}
	}

	static class InheritedComposedAnnotationSubClass extends InheritedComposedAnnotationClass {
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

	@Fast
	interface TaggedInterface {
	}

	static class TaggedInterfaceClass implements TaggedInterface {
	}

	@Smoke
	static class LocalTagOnTaggedInterfaceClass implements TaggedInterface {
	}

	@Tags({ @Tag("a"), @Tag("b"), @Tag("c") })
	@Tag("d")
	static class ContainerTaggedClass {
	}

	@Fast
	@Tags({ @Tag("a"), @Tag("b"), @Tag("c") })
	static class ContainerAfterComposedTaggedClass {
	}

	@ExtendWith("a")
	static class SingleExtensionClass {
	}

	static class SubSingleExtensionClass extends SingleExtensionClass {
	}

	@ExtendWith("a")
	@ExtendWith("b")
	@ExtendWith("c")
	static class MultiExtensionClass {
	}

	@ExtendWith("x")
	@ExtendWith("y")
	@ExtendWith("b") // duplicates parent
	@ExtendWith("z")
	@ExtendWith("a") // duplicates parent
	static class SubMultiExtensionClass extends MultiExtensionClass {
	}

	@Extensions({ @ExtendWith("a"), @ExtendWith("b"), @ExtendWith("c"), @ExtendWith("a") })
	static class ContainerExtensionClass {
	}

	@ExtendWith("x")
	static class SubContainerExtensionClass extends ContainerExtensionClass {
	}

	@ExtendWithFoo
	static class SingleComposedExtensionClass {
	}

	static class SubSingleComposedExtensionClass extends SingleComposedExtensionClass {
	}

	@ExtendWithFoo
	@ExtendWithBar
	static class MultiComposedExtensionClass {
	}

	static class SubMultiComposedExtensionClass extends MultiComposedExtensionClass {
	}

	@Extensions({ @ExtendWith("x"), @ExtendWith("y"), @ExtendWith("z") })
	@ExtendWithBar
	static class ContainerPlusSubMultiComposedExtensionClass extends MultiComposedExtensionClass {
	}

	interface InterfaceWithAnnotatedMethod {

		@Annotation3
		default void interfaceMethod() {
		}
	}

	static class SuperClassWithAnnotatedMethod {

		@Annotation1
		public void superMethod() {
		}
	}

	static class ClassWithAnnotatedMethods extends SuperClassWithAnnotatedMethod
			implements InterfaceWithAnnotatedMethod {

		@Annotation1
		public void method1() {
		}

		@Annotation2
		public void method2() {
		}

		@Annotation1
		@Annotation2
		public void method3() {
		}
	}
}
