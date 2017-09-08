/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedMethods;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;
import static org.junit.platform.commons.util.AnnotationUtils.findPublicAnnotatedFields;
import static org.junit.platform.commons.util.AnnotationUtils.findRepeatableAnnotations;
import static org.junit.platform.commons.util.AnnotationUtils.getDefaultValue;
import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;
import static org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode.BOTTOM_UP;
import static org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode.TOP_DOWN;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link AnnotationUtils}.
 *
 * @since 1.0
 */
class AnnotationUtilsTests {

	@Test
	void getDefaultValueForNullAnnotation() {
		assertThrows(PreconditionViolationException.class, () -> getDefaultValue(null, "foo", String.class));
	}

	@Test
	void getDefaultValueForNullAttributeName() {
		Annotation annotation = AnnotationWithDefaultValueClass.class.getAnnotations()[0];
		assertThrows(PreconditionViolationException.class, () -> getDefaultValue(annotation, null, String.class));
	}

	@Test
	void getDefaultValueForNullAttributeType() {
		Annotation annotation = AnnotationWithDefaultValueClass.class.getAnnotations()[0];
		assertThrows(PreconditionViolationException.class, () -> getDefaultValue(annotation, "foo", null));
	}

	@Test
	void getDefaultValueForNonMatchingAttributeType() {
		Annotation annotation = AnnotationWithDefaultValueClass.class.getAnnotations()[0];
		PreconditionViolationException exception = assertThrows(PreconditionViolationException.class,
			() -> getDefaultValue(annotation, "value", Integer.class));

		assertThat(exception.getMessage()).contains("Attribute 'value' in annotation",
			AnnotationWithDefaultValue.class.getName(), "is of type java.lang.String, not java.lang.Integer");
	}

	@Test
	void getDefaultValueForEmptyAttributeName() {
		Annotation annotation = AnnotationWithDefaultValueClass.class.getAnnotations()[0];
		assertThrows(PreconditionViolationException.class, () -> getDefaultValue(annotation, "  \t ", String.class));
	}

	@Test
	void getDefaultValueForAttributeWithDefault() {
		Annotation annotation = AnnotationWithDefaultValueClass.class.getAnnotations()[0];
		Optional<String> defaultValue = getDefaultValue(annotation, "value", String.class);
		assertThat(defaultValue).contains("default");
	}

	@Test
	void findAnnotationForNullOptional() {
		assertThat(findAnnotation((Optional<AnnotatedElement>) null, Annotation1.class)).isEmpty();
	}

	@Test
	void findAnnotationForEmptyOptional() {
		assertThat(findAnnotation(Optional.empty(), Annotation1.class)).isEmpty();
	}

	@Test
	void findAnnotationForNullAnnotatedElement() {
		assertThat(findAnnotation((AnnotatedElement) null, Annotation1.class)).isEmpty();
	}

	@Test
	void findAnnotationOnClassWithoutAnnotation() {
		assertThat(findAnnotation(Annotation1Class.class, Annotation2.class)).isNotPresent();
	}

	@Test
	void findAnnotationIndirectlyPresentOnOptionalClass() {
		Optional<Class<?>> optional = Optional.of(SubInheritedAnnotationClass.class);
		assertThat(findAnnotation(optional, InheritedAnnotation.class)).isPresent();
	}

	@Test
	void findAnnotationIndirectlyPresentOnClass() {
		assertThat(findAnnotation(SubInheritedAnnotationClass.class, InheritedAnnotation.class)).isPresent();
	}

	@Test
	void findAnnotationDirectlyPresentOnClass() {
		assertThat(findAnnotation(Annotation1Class.class, Annotation1.class)).isPresent();
	}

	@Test
	void findAnnotationMetaPresentOnClass() {
		assertThat(findAnnotation(ComposedAnnotationClass.class, Annotation1.class)).isPresent();
	}

	/**
	 * <b>Note:</b> there is no findAnnotationIndirectlyMetaPresentOnMethod counterpart because {@link Inherited}
	 * annotation has no effect if the annotated type is used to annotate anything other than a class.
	 *
	 * @see Inherited
	 */
	@Test
	void findAnnotationIndirectlyMetaPresentOnClass() {
		assertThat(findAnnotation(InheritedComposedAnnotationSubClass.class, Annotation1.class)).isPresent();
	}

	@Test
	void findAnnotationDirectlyPresentOnImplementedInterface() {
		assertThat(findAnnotation(TestingTraitClass.class, Annotation1.class)).isPresent();
	}

	@Test
	void findAnnotationMetaPresentOnImplementedInterface() {
		assertThat(findAnnotation(ComposedTestingTraitClass.class, Annotation1.class)).isPresent();
	}

	@Test
	void findAnnotationDirectlyPresentOnMethod() throws Exception {
		Method method = Annotation2Class.class.getDeclaredMethod("method");
		assertThat(findAnnotation(method, Annotation1.class)).isPresent();
	}

	@Test
	void findAnnotationMetaPresentOnMethod() throws Exception {
		Method method = ComposedAnnotationClass.class.getDeclaredMethod("method");
		assertThat(findAnnotation(method, Annotation1.class)).isPresent();
	}

	@Test
	void findAnnotationMetaPresentOnOptionalMethod() throws Exception {
		Method method = ComposedAnnotationClass.class.getDeclaredMethod("method");
		assertThat(findAnnotation(Optional.of(method), Annotation1.class)).isPresent();
	}

	@Test
	void isAnnotatedForClassWithoutAnnotation() {
		assertFalse(isAnnotated(Annotation1Class.class, Annotation2.class));
	}

	@Test
	void isAnnotatedWhenIndirectlyPresentOnClass() {
		assertTrue(isAnnotated(SubInheritedAnnotationClass.class, InheritedAnnotation.class));
	}

	@Test
	void isAnnotatedWhenDirectlyPresentOnClass() {
		assertTrue(isAnnotated(Annotation1Class.class, Annotation1.class));
	}

	@Test
	void isAnnotatedWhenMetaPresentOnClass() {
		assertTrue(isAnnotated(ComposedAnnotationClass.class, Annotation1.class));
	}

	@Test
	void isAnnotatedWhenDirectlyPresentOnMethod() throws Exception {
		assertTrue(isAnnotated(Annotation2Class.class.getDeclaredMethod("method"), Annotation1.class));
	}

	@Test
	void isAnnotatedWhenMetaPresentOnMethod() throws Exception {
		assertTrue(isAnnotated(ComposedAnnotationClass.class.getDeclaredMethod("method"), Annotation1.class));
	}

	@Test
	void findRepeatableAnnotationsForNullAnnotatedElement() {
		assertThat(findRepeatableAnnotations(null, Tag.class)).isEmpty();
	}

	@Test
	void findRepeatableAnnotationsWithSingleTag() throws Exception {
		assertTagsFound(SingleTaggedClass.class, "a");
	}

	@Test
	void findRepeatableAnnotationsWithSingleComposedTag() throws Exception {
		assertTagsFound(SingleComposedTaggedClass.class, "fast");
	}

	@Test
	void findRepeatableAnnotationsWithSingleComposedTagOnImplementedInterface() throws Exception {
		assertTagsFound(TaggedInterfaceClass.class, "fast");
	}

	@Test
	void findRepeatableAnnotationsWithLocalComposedTagAndComposedTagOnImplementedInterface() throws Exception {
		assertTagsFound(LocalTagOnTaggedInterfaceClass.class, "fast", "smoke");
	}

	@Test
	void findRepeatableAnnotationsWithMultipleTags() throws Exception {
		assertTagsFound(MultiTaggedClass.class, "a", "b", "c");
	}

	@Test
	void findRepeatableAnnotationsWithMultipleComposedTags() throws Exception {
		assertTagsFound(MultiComposedTaggedClass.class, "fast", "smoke");
		assertTagsFound(FastAndSmokyTaggedClass.class, "fast", "smoke");
	}

	@Test
	void findRepeatableAnnotationsWithContainer() throws Exception {
		assertTagsFound(ContainerTaggedClass.class, "a", "b", "c", "d");
	}

	@Test
	void findRepeatableAnnotationsWithComposedTagBeforeContainer() throws Exception {
		assertTagsFound(ContainerAfterComposedTaggedClass.class, "fast", "a", "b", "c");
	}

	private void assertTagsFound(Class<?> clazz, String... tags) throws Exception {
		assertEquals(asList(tags),
			findRepeatableAnnotations(clazz, Tag.class).stream().map(Tag::value).collect(toList()),
			() -> "Tags found for class " + clazz.getName());
	}

	@Test
	void findInheritedRepeatableAnnotationsWithSingleAnnotationOnSuperclass() throws Exception {
		assertExtensionsFound(SingleExtensionClass.class, "a");
		assertExtensionsFound(SubSingleExtensionClass.class, "a");
	}

	@Test
	void findInheritedRepeatableAnnotationsWithMultipleAnnotationsOnSuperclass() throws Exception {
		assertExtensionsFound(MultiExtensionClass.class, "a", "b", "c");
		assertExtensionsFound(SubMultiExtensionClass.class, "a", "b", "c", "x", "y", "z");
	}

	@Test
	void findInheritedRepeatableAnnotationsWithContainerAnnotationOnSuperclass() throws Exception {
		assertExtensionsFound(ContainerExtensionClass.class, "a", "b", "c");
		assertExtensionsFound(SubContainerExtensionClass.class, "a", "b", "c", "x");
	}

	@Test
	void findInheritedRepeatableAnnotationsWithSingleComposedAnnotation() throws Exception {
		assertExtensionsFound(SingleComposedExtensionClass.class, "foo");
	}

	@Test
	void findInheritedRepeatableAnnotationsWithSingleComposedAnnotationOnSuperclass() throws Exception {
		assertExtensionsFound(SubSingleComposedExtensionClass.class, "foo");
	}

	@Test
	void findInheritedRepeatableAnnotationsWithMultipleComposedAnnotations() throws Exception {
		assertExtensionsFound(MultiComposedExtensionClass.class, "foo", "bar");
	}

	@Test
	void findInheritedRepeatableAnnotationsWithMultipleComposedAnnotationsOnSuperclass() throws Exception {
		assertExtensionsFound(SubMultiComposedExtensionClass.class, "foo", "bar");
	}

	@Test
	void findInheritedRepeatableAnnotationsWithMultipleComposedAnnotationsOnSuperclassAndLocalContainerAndComposed()
			throws Exception {
		assertExtensionsFound(ContainerPlusSubMultiComposedExtensionClass.class, "foo", "bar", "x", "y", "z");
	}

	private void assertExtensionsFound(Class<?> clazz, String... tags) throws Exception {
		assertEquals(asList(tags),
			findRepeatableAnnotations(clazz, ExtendWith.class).stream().map(ExtendWith::value).collect(toList()),
			() -> "Extensions found for class " + clazz.getName());
	}

	@Test
	void findAnnotatedMethodsForNullClass() {
		assertThrows(PreconditionViolationException.class,
			() -> findAnnotatedMethods(null, Annotation1.class, TOP_DOWN));
	}

	@Test
	void findAnnotatedMethodsForNullAnnotationType() {
		assertThrows(PreconditionViolationException.class,
			() -> findAnnotatedMethods(ClassWithAnnotatedMethods.class, null, TOP_DOWN));
	}

	@Test
	void findAnnotatedMethodsForAnnotationThatIsNotPresent() {
		assertThat(findAnnotatedMethods(ClassWithAnnotatedMethods.class, Fast.class, TOP_DOWN)).isEmpty();
	}

	@Test
	void findAnnotatedMethodsForAnnotationOnMethodsInClassUsingHierarchyDownMode() throws Exception {
		Method method2 = ClassWithAnnotatedMethods.class.getDeclaredMethod("method2");
		Method method3 = ClassWithAnnotatedMethods.class.getDeclaredMethod("method3");

		List<Method> methods = findAnnotatedMethods(ClassWithAnnotatedMethods.class, Annotation2.class, TOP_DOWN);

		assertThat(methods).containsOnly(method2, method3);
	}

	@Test
	void findAnnotatedMethodsForAnnotationOnMethodsInClassHierarchyUsingHierarchyUpMode() throws Exception {
		Method method1 = ClassWithAnnotatedMethods.class.getDeclaredMethod("method1");
		Method method3 = ClassWithAnnotatedMethods.class.getDeclaredMethod("method3");
		Method superMethod = SuperClassWithAnnotatedMethod.class.getDeclaredMethod("superMethod");

		List<Method> methods = findAnnotatedMethods(ClassWithAnnotatedMethods.class, Annotation1.class, BOTTOM_UP);

		assertEquals(3, methods.size());
		assertThat(methods.subList(0, 2)).containsOnly(method1, method3);
		assertEquals(superMethod, methods.get(2));
	}

	@Test
	void findAnnotatedMethodsForAnnotationUsedInClassAndSuperClassHierarchyDown() throws Exception {
		Method method1 = ClassWithAnnotatedMethods.class.getDeclaredMethod("method1");
		Method method3 = ClassWithAnnotatedMethods.class.getDeclaredMethod("method3");
		Method superMethod = SuperClassWithAnnotatedMethod.class.getDeclaredMethod("superMethod");

		List<Method> methods = findAnnotatedMethods(ClassWithAnnotatedMethods.class, Annotation1.class, TOP_DOWN);

		assertEquals(3, methods.size());
		assertEquals(superMethod, methods.get(0));
		assertThat(methods.subList(1, 3)).containsOnly(method1, method3);
	}

	@Test
	void findAnnotatedMethodsForAnnotationUsedInInterface() throws Exception {
		Method interfaceMethod = InterfaceWithAnnotatedDefaultMethod.class.getDeclaredMethod("interfaceMethod");

		List<Method> methods = findAnnotatedMethods(ClassWithAnnotatedMethods.class, Annotation3.class, BOTTOM_UP);

		assertThat(methods).containsExactly(interfaceMethod);
	}

	@Test
	void findPublicAnnotatedFieldsForNullClass() {
		assertThrows(PreconditionViolationException.class,
			() -> findPublicAnnotatedFields(null, String.class, Annotation1.class));
	}

	@Test
	void findPublicAnnotatedFieldsForNullFieldType() {
		assertThrows(PreconditionViolationException.class,
			() -> findPublicAnnotatedFields(getClass(), null, Annotation1.class));
	}

	@Test
	void findPublicAnnotatedFieldsForNullAnnotationType() {
		assertThrows(PreconditionViolationException.class,
			() -> findPublicAnnotatedFields(getClass(), String.class, null));
	}

	@Test
	void findPublicAnnotatedFieldsForPrivateField() {
		List<Field> fields = findPublicAnnotatedFields(getClass(), Boolean.class, Annotation1.class);
		assertNotNull(fields);
		assertEquals(0, fields.size());
	}

	@Test
	void findPublicAnnotatedFieldsForDirectlyAnnotatedFieldOfWrongFieldType() {
		List<Field> fields = findPublicAnnotatedFields(getClass(), BigDecimal.class, Annotation1.class);
		assertNotNull(fields);
		assertEquals(0, fields.size());
	}

	@Test
	void findPublicAnnotatedFieldsForDirectlyAnnotatedField() {
		List<Field> fields = findPublicAnnotatedFields(getClass(), String.class, Annotation1.class);
		assertNotNull(fields);
		assertIterableEquals(asList("directlyAnnotatedField"), asNames(fields));
	}

	@Test
	void findPublicAnnotatedFieldsForMetaAnnotatedField() {
		List<Field> fields = findPublicAnnotatedFields(getClass(), Number.class, Annotation1.class);
		assertNotNull(fields);
		assertEquals(1, fields.size());
		assertIterableEquals(asList("metaAnnotatedField"), asNames(fields));
	}

	@Test
	void findPublicAnnotatedFieldsForDirectlyAnnotatedFieldInInterface() {
		List<Field> fields = findPublicAnnotatedFields(InterfaceWithAnnotatedFields.class, String.class,
			Annotation1.class);
		assertNotNull(fields);
		assertIterableEquals(asList("foo"), asNames(fields));
	}

	@Test
	void findPublicAnnotatedFieldsForDirectlyAnnotatedFieldsInClassAndInterface() {
		List<Field> fields = findPublicAnnotatedFields(ClassWithAnnotatedFieldsFromInterface.class, String.class,
			Annotation1.class);
		assertNotNull(fields);
		assertThat(asNames(fields)).containsExactlyInAnyOrder("foo", "bar");
	}

	private List<String> asNames(List<Field> fields) {
		return fields.stream().map(Field::getName).collect(toList());
	}

	// -------------------------------------------------------------------------

	@Target(ElementType.TYPE)
	@Retention(RetentionPolicy.RUNTIME)
	@interface AnnotationWithDefaultValue {

		String value() default "default";
	}

	@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
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

	@Target({ ElementType.TYPE, ElementType.METHOD, ElementType.FIELD })
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
	@interface Extensions {

		ExtendWith[] value();
	}

	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@Repeatable(Extensions.class)
	@interface ExtendWith {

		String value();
	}

	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@ExtendWith("foo")
	@interface ExtendWithFoo {
	}

	@Target({ ElementType.TYPE, ElementType.METHOD })
	@Retention(RetentionPolicy.RUNTIME)
	@Inherited
	@ExtendWith("bar")
	@interface ExtendWithBar {
	}

	@AnnotationWithDefaultValue
	static class AnnotationWithDefaultValueClass {
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
		void method() {
		}
	}

	static class InheritedComposedAnnotationSubClass extends InheritedComposedAnnotationClass {
	}

	@Annotation1
	interface TestingTrait {
	}

	static class TestingTraitClass implements TestingTrait {
	}

	@ComposedAnnotation
	interface ComposedTestingTrait {
	}

	static class ComposedTestingTraitClass implements ComposedTestingTrait {
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

	@ExtendWith("x")
	@Extensions({ @ExtendWith("y"), @ExtendWith("z") })
	@ExtendWithBar
	static class ContainerPlusSubMultiComposedExtensionClass extends MultiComposedExtensionClass {
	}

	interface InterfaceWithAnnotatedDefaultMethod {

		@Annotation3
		default void interfaceMethod() {
		}
	}

	static class SuperClassWithAnnotatedMethod {

		@Annotation1
		void superMethod() {
		}
	}

	static class ClassWithAnnotatedMethods extends SuperClassWithAnnotatedMethod
			implements InterfaceWithAnnotatedDefaultMethod {

		@Annotation1
		void method1() {
		}

		@Annotation2
		void method2() {
		}

		@Annotation1
		@Annotation2
		void method3() {
		}
	}

	@Annotation1
	private Boolean privateDirectlyAnnotatedField;

	@Annotation1
	public String directlyAnnotatedField;

	@ComposedAnnotation
	public Integer metaAnnotatedField;

	interface InterfaceWithAnnotatedFields {

		@Annotation1
		String foo = "bar";

		@Annotation1
		boolean wrongType = false;
	}

	class ClassWithAnnotatedFieldsFromInterface implements InterfaceWithAnnotatedFields {

		@Annotation1
		public String bar = "baz";

		@Annotation1
		public boolean notAString = true;
	}

}
