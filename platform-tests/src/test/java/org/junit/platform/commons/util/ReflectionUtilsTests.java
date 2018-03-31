/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode.BOTTOM_UP;
import static org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode.TOP_DOWN;
import static org.junit.platform.commons.util.ReflectionUtils.findMethod;
import static org.junit.platform.commons.util.ReflectionUtils.findMethods;
import static org.junit.platform.commons.util.ReflectionUtils.invokeMethod;
import static org.junit.platform.commons.util.ReflectionUtils.readFieldValue;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.extensions.TempDirectory;
import org.junit.jupiter.extensions.TempDirectory.Root;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.ReflectionUtilsTests.ClassWithNestedClasses.Nested1;
import org.junit.platform.commons.util.ReflectionUtilsTests.ClassWithNestedClasses.Nested2;
import org.junit.platform.commons.util.ReflectionUtilsTests.ClassWithNestedClasses.Nested3;
import org.junit.platform.commons.util.ReflectionUtilsTests.Interface45.Nested5;
import org.junit.platform.commons.util.ReflectionUtilsTests.InterfaceWithNestedClass.Nested4;

/**
 * Unit tests for {@link ReflectionUtils}.
 *
 * @since 1.0
 */
class ReflectionUtilsTests {

	private static final Predicate<Method> isFooMethod = method -> method.getName().equals("foo");
	private static final Predicate<Method> methodContains1 = method -> method.getName().contains("1");
	private static final Predicate<Method> methodContains2 = method -> method.getName().contains("2");
	private static final Predicate<Method> methodContains4 = method -> method.getName().contains("4");
	private static final Predicate<Method> methodContains5 = method -> method.getName().contains("5");

	@Test
	void getDefaultClassLoaderWithExplicitContextClassLoader() {
		ClassLoader original = Thread.currentThread().getContextClassLoader();
		ClassLoader mock = mock(ClassLoader.class);
		Thread.currentThread().setContextClassLoader(mock);
		try {
			assertSame(mock, ClassLoaderUtils.getDefaultClassLoader());
		}
		finally {
			Thread.currentThread().setContextClassLoader(original);
		}
	}

	@Test
	void getDefaultClassLoaderWithNullContextClassLoader() {
		ClassLoader original = Thread.currentThread().getContextClassLoader();
		Thread.currentThread().setContextClassLoader(null);
		try {
			assertSame(ClassLoader.getSystemClassLoader(), ClassLoaderUtils.getDefaultClassLoader());
		}
		finally {
			Thread.currentThread().setContextClassLoader(original);
		}
	}

	@Test
	void isPublic() throws Exception {
		assertTrue(ReflectionUtils.isPublic(PublicClass.class));
		assertTrue(ReflectionUtils.isPublic(PublicClass.class.getMethod("publicMethod")));

		assertFalse(ReflectionUtils.isPublic(PrivateClass.class));
		assertFalse(ReflectionUtils.isPublic(PrivateClass.class.getDeclaredMethod("privateMethod")));
	}

	@Test
	void isAbstract() throws Exception {
		assertTrue(ReflectionUtils.isAbstract(AbstractClass.class));
		assertTrue(ReflectionUtils.isAbstract(AbstractClass.class.getDeclaredMethod("abstractMethod")));

		assertFalse(ReflectionUtils.isAbstract(PublicClass.class));
		assertFalse(ReflectionUtils.isAbstract(PublicClass.class.getDeclaredMethod("publicMethod")));
	}

	@Test
	void isStatic() throws Exception {
		assertTrue(ReflectionUtils.isStatic(StaticClass.class));
		assertTrue(ReflectionUtils.isStatic(StaticClass.class.getDeclaredMethod("staticMethod")));

		assertFalse(ReflectionUtils.isStatic(PublicClass.class));
		assertFalse(ReflectionUtils.isStatic(PublicClass.class.getDeclaredMethod("publicMethod")));
	}

	@Test
	void returnsVoid() throws Exception {
		Class<?> clazz = ClassWithVoidAndNonVoidMethods.class;
		assertTrue(ReflectionUtils.returnsVoid(clazz.getDeclaredMethod("voidMethod")));

		assertFalse(ReflectionUtils.returnsVoid(clazz.getDeclaredMethod("methodReturningVoidReference")));
		assertFalse(ReflectionUtils.returnsVoid(clazz.getDeclaredMethod("methodReturningObject")));
		assertFalse(ReflectionUtils.returnsVoid(clazz.getDeclaredMethod("methodReturningPrimitive")));
	}

	@Test
	void getAllAssignmentCompatibleClassesWithNullClass() {
		assertThrows(PreconditionViolationException.class,
			() -> ReflectionUtils.getAllAssignmentCompatibleClasses(null));
	}

	@Test
	void getAllAssignmentCompatibleClasses() {
		Set<Class<?>> superclasses = ReflectionUtils.getAllAssignmentCompatibleClasses(B.class);
		assertThat(superclasses).containsExactly(B.class, InterfaceC.class, InterfaceA.class, InterfaceB.class, A.class,
			InterfaceD.class, Object.class);
		assertTrue(superclasses.stream().allMatch(clazz -> clazz.isAssignableFrom(B.class)));
	}

	@Test
	void newInstance() {
		// @formatter:off
		assertThat(ReflectionUtils.newInstance(C.class, "one", "two")).isNotNull();
		assertThat(ReflectionUtils.newInstance(C.class)).isNotNull();
		assertThat(ReflectionUtils.newInstance(C.class, new Object[0])).isNotNull();

		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.newInstance(C.class, "one", null));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.newInstance(C.class, null, "two"));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.newInstance(C.class, null, null));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.newInstance(C.class, ((Object[]) null)));

		RuntimeException exception = assertThrows(RuntimeException.class, () -> ReflectionUtils.newInstance(Exploder.class));
		assertThat(exception).hasMessage("boom");
		// @formatter:on
	}

	@Test
	void readFieldValueOfNonexistentStaticField() {
		assertThat(readFieldValue(MyClass.class, "doesNotExist", null)).isNotPresent();
		assertThat(readFieldValue(MySubClass.class, "staticField", null)).isNotPresent();
	}

	@Test
	void readFieldValueOfNonexistentInstanceField() {
		assertThat(readFieldValue(MyClass.class, "doesNotExist", new MyClass(42))).isNotPresent();
		assertThat(readFieldValue(MyClass.class, "doesNotExist", new MySubClass(42))).isNotPresent();
	}

	@Test
	void readFieldValueOfExistingStaticField() throws Exception {
		assertThat(readFieldValue(MyClass.class, "staticField", null)).contains(42);

		Field field = MyClass.class.getDeclaredField("staticField");
		assertThat(readFieldValue(field)).contains(42);
		assertThat(readFieldValue(field, null)).contains(42);
	}

	@Test
	void readFieldValueOfExistingInstanceField() throws Exception {
		MyClass instance = new MyClass(42);
		assertThat(readFieldValue(MyClass.class, "instanceField", instance)).contains(42);

		Field field = MyClass.class.getDeclaredField("instanceField");
		assertThat(readFieldValue(field, instance)).contains(42);
		assertThat(readFieldValue(field, null)).isNotPresent();
	}

	@Test
	void isAssignableToForNullClass() {
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.isAssignableTo(new Object(), null));
	}

	@Test
	void isAssignableTo() {
		// Reference Types
		assertTrue(ReflectionUtils.isAssignableTo("string", String.class));
		assertTrue(ReflectionUtils.isAssignableTo("string", CharSequence.class));
		assertTrue(ReflectionUtils.isAssignableTo("string", Object.class));

		assertFalse(ReflectionUtils.isAssignableTo(new Object(), String.class));
		assertFalse(ReflectionUtils.isAssignableTo(Integer.valueOf("1"), StringBuilder.class));
		assertFalse(ReflectionUtils.isAssignableTo(new StringBuilder(), String.class));

		// Arrays
		assertTrue(ReflectionUtils.isAssignableTo(new int[0], int[].class));
		assertTrue(ReflectionUtils.isAssignableTo(new double[0], Object.class));
		assertTrue(ReflectionUtils.isAssignableTo(new String[0], String[].class));
		assertTrue(ReflectionUtils.isAssignableTo(new String[0], Object.class));

		// Primitive Types
		assertTrue(ReflectionUtils.isAssignableTo(1, int.class));
		assertTrue(ReflectionUtils.isAssignableTo(Long.valueOf("1"), long.class));
		assertTrue(ReflectionUtils.isAssignableTo(Boolean.TRUE, boolean.class));

		// Widening Conversions to Primitives
		assertTrue(ReflectionUtils.isAssignableTo(1, long.class));
		assertTrue(ReflectionUtils.isAssignableTo(1f, double.class));
		assertTrue(ReflectionUtils.isAssignableTo((byte) 1, double.class));

		// Widening Conversions to Wrappers (not supported by Java)
		assertFalse(ReflectionUtils.isAssignableTo(1, Long.class));
		assertFalse(ReflectionUtils.isAssignableTo(1f, Double.class));
		assertFalse(ReflectionUtils.isAssignableTo((byte) 1, Double.class));

		// Narrowing Conversions
		assertFalse(ReflectionUtils.isAssignableTo(1, char.class));
		assertFalse(ReflectionUtils.isAssignableTo(1L, byte.class));
		assertFalse(ReflectionUtils.isAssignableTo(1L, int.class));
	}

	@Test
	void wideningConversion() {
		// byte
		assertTrue(ReflectionUtils.isWideningConversion(byte.class, short.class));
		assertTrue(ReflectionUtils.isWideningConversion(byte.class, int.class));
		assertTrue(ReflectionUtils.isWideningConversion(byte.class, long.class));
		assertTrue(ReflectionUtils.isWideningConversion(byte.class, float.class));
		assertTrue(ReflectionUtils.isWideningConversion(byte.class, double.class));
		// Byte
		assertTrue(ReflectionUtils.isWideningConversion(Byte.class, short.class));
		assertTrue(ReflectionUtils.isWideningConversion(Byte.class, int.class));
		assertTrue(ReflectionUtils.isWideningConversion(Byte.class, long.class));
		assertTrue(ReflectionUtils.isWideningConversion(Byte.class, float.class));
		assertTrue(ReflectionUtils.isWideningConversion(Byte.class, double.class));

		// short
		assertTrue(ReflectionUtils.isWideningConversion(short.class, int.class));
		assertTrue(ReflectionUtils.isWideningConversion(short.class, long.class));
		assertTrue(ReflectionUtils.isWideningConversion(short.class, float.class));
		assertTrue(ReflectionUtils.isWideningConversion(short.class, double.class));
		// Short
		assertTrue(ReflectionUtils.isWideningConversion(Short.class, int.class));
		assertTrue(ReflectionUtils.isWideningConversion(Short.class, long.class));
		assertTrue(ReflectionUtils.isWideningConversion(Short.class, float.class));
		assertTrue(ReflectionUtils.isWideningConversion(Short.class, double.class));

		// char
		assertTrue(ReflectionUtils.isWideningConversion(char.class, int.class));
		assertTrue(ReflectionUtils.isWideningConversion(char.class, long.class));
		assertTrue(ReflectionUtils.isWideningConversion(char.class, float.class));
		assertTrue(ReflectionUtils.isWideningConversion(char.class, double.class));
		// Character
		assertTrue(ReflectionUtils.isWideningConversion(Character.class, int.class));
		assertTrue(ReflectionUtils.isWideningConversion(Character.class, long.class));
		assertTrue(ReflectionUtils.isWideningConversion(Character.class, float.class));
		assertTrue(ReflectionUtils.isWideningConversion(Character.class, double.class));

		// int
		assertTrue(ReflectionUtils.isWideningConversion(int.class, long.class));
		assertTrue(ReflectionUtils.isWideningConversion(int.class, float.class));
		assertTrue(ReflectionUtils.isWideningConversion(int.class, double.class));
		// Integer
		assertTrue(ReflectionUtils.isWideningConversion(Integer.class, long.class));
		assertTrue(ReflectionUtils.isWideningConversion(Integer.class, float.class));
		assertTrue(ReflectionUtils.isWideningConversion(Integer.class, double.class));

		// long
		assertTrue(ReflectionUtils.isWideningConversion(long.class, float.class));
		assertTrue(ReflectionUtils.isWideningConversion(long.class, double.class));
		// Long
		assertTrue(ReflectionUtils.isWideningConversion(Long.class, float.class));
		assertTrue(ReflectionUtils.isWideningConversion(Long.class, double.class));

		// float
		assertTrue(ReflectionUtils.isWideningConversion(float.class, double.class));
		// Float
		assertTrue(ReflectionUtils.isWideningConversion(Float.class, double.class));

		// double and Double --> nothing to test

		// Unsupported
		assertFalse(ReflectionUtils.isWideningConversion(int.class, byte.class)); // narrowing
		assertFalse(ReflectionUtils.isWideningConversion(float.class, int.class)); // narrowing
		assertFalse(ReflectionUtils.isWideningConversion(int.class, int.class)); // direct match
		assertFalse(ReflectionUtils.isWideningConversion(String.class, int.class)); // neither a primitive nor a wrapper
	}

	@Test
	void isAssignableToForNullObject() {
		assertTrue(ReflectionUtils.isAssignableTo(null, Object.class));
		assertTrue(ReflectionUtils.isAssignableTo(null, String.class));
		assertTrue(ReflectionUtils.isAssignableTo(null, Long.class));
		assertTrue(ReflectionUtils.isAssignableTo(null, Character[].class));
	}

	@Test
	void isAssignableToForNullObjectAndPrimitive() {
		assertFalse(ReflectionUtils.isAssignableTo(null, byte.class));
		assertFalse(ReflectionUtils.isAssignableTo(null, int.class));
		assertFalse(ReflectionUtils.isAssignableTo(null, long.class));
		assertFalse(ReflectionUtils.isAssignableTo(null, boolean.class));
	}

	@Test
	void invokeMethodPreconditions() {
		// @formatter:off
		assertThrows(PreconditionViolationException.class, () -> invokeMethod(null, new Object()));
		assertThrows(PreconditionViolationException.class, () -> invokeMethod(Object.class.getMethod("hashCode"), null));
		// @formatter:on
	}

	@Test
	void invokePublicMethod() throws Exception {
		InvocationTracker tracker = new InvocationTracker();
		invokeMethod(InvocationTracker.class.getDeclaredMethod("publicMethod"), tracker);
		assertTrue(tracker.publicMethodInvoked);
	}

	@Test
	void invokePrivateMethod() throws Exception {
		InvocationTracker tracker = new InvocationTracker();
		invokeMethod(InvocationTracker.class.getDeclaredMethod("privateMethod"), tracker);
		assertTrue(tracker.privateMethodInvoked);
	}

	@Test
	void invokePublicStaticMethod() throws Exception {
		invokeMethod(InvocationTracker.class.getDeclaredMethod("publicStaticMethod"), null);
		assertTrue(InvocationTracker.publicStaticMethodInvoked);
	}

	@Test
	void invokePrivateStaticMethod() throws Exception {
		invokeMethod(InvocationTracker.class.getDeclaredMethod("privateStaticMethod"), null);
		assertTrue(InvocationTracker.privateStaticMethodInvoked);
	}

	@Test
	void loadClassPreconditions() {
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.loadClass(null));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.loadClass(""));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.loadClass("   "));

		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.loadClass(null, null));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.loadClass(getClass().getName(), null));
	}

	@Test
	void loadClassWhenClassNotFoundException() {
		assertThat(ReflectionUtils.loadClass("foo.bar.EnigmaClassThatDoesNotExist")).isEmpty();
	}

	@Test
	void loadClass() {
		Optional<Class<?>> optional = ReflectionUtils.loadClass(Integer.class.getName());
		assertThat(optional).contains(Integer.class);
	}

	@Test
	void loadClassTrimsClassName() {
		Optional<Class<?>> optional = ReflectionUtils.loadClass("  " + Integer.class.getName() + "\t");
		assertThat(optional).contains(Integer.class);
	}

	@Test
	void loadClassForPrimitive() {
		Optional<Class<?>> optional = ReflectionUtils.loadClass(int.class.getName());
		assertThat(optional).contains(int.class);
	}

	@Test
	void loadClassForPrimitiveArray() {
		Optional<Class<?>> optional = ReflectionUtils.loadClass(int[].class.getName());
		assertThat(optional).contains(int[].class);
	}

	@Test
	void loadClassForPrimitiveArrayUsingSourceCodeSyntax() {
		Optional<Class<?>> optional = ReflectionUtils.loadClass("int[]");
		assertThat(optional).contains(int[].class);
	}

	@Test
	void loadClassForObjectArray() {
		Optional<Class<?>> optional = ReflectionUtils.loadClass(String[].class.getName());
		assertThat(optional).contains(String[].class);
	}

	@Test
	void loadClassForObjectArrayUsingSourceCodeSyntax() {
		Optional<Class<?>> optional = ReflectionUtils.loadClass("java.lang.String[]");
		assertThat(optional).contains(String[].class);
	}

	@Test
	void loadClassForTwoDimensionalPrimitiveArray() {
		Optional<Class<?>> optional = ReflectionUtils.loadClass(int[][].class.getName());
		assertThat(optional).contains(int[][].class);
	}

	@Test
	void loadClassForTwoDimensionaldimensionalPrimitiveArrayUsingSourceCodeSyntax() {
		Optional<Class<?>> optional = ReflectionUtils.loadClass("int[][]");
		assertThat(optional).contains(int[][].class);
	}

	@Test
	void loadClassForMultidimensionalPrimitiveArray() {
		String className = int[][][][][].class.getName();
		Optional<Class<?>> optional = ReflectionUtils.loadClass(className);
		assertThat(optional).as(className).contains(int[][][][][].class);
	}

	@Test
	void loadClassForMultidimensionalPrimitiveArrayUsingSourceCodeSyntax() {
		String className = "int[][][][][]";
		Optional<Class<?>> optional = ReflectionUtils.loadClass(className);
		assertThat(optional).as(className).contains(int[][][][][].class);
	}

	@Test
	void loadClassForMultidimensionalObjectArray() {
		String className = String[][][][][].class.getName();
		Optional<Class<?>> optional = ReflectionUtils.loadClass(className);
		assertThat(optional).as(className).contains(String[][][][][].class);
	}

	@Test
	void loadClassForMultidimensionalObjectArrayUsingSourceCodeSyntax() {
		Optional<Class<?>> optional = ReflectionUtils.loadClass("java.lang.String[][][][][]");
		assertThat(optional).contains(String[][][][][].class);
	}

	@Test
	void getOutermostInstancePreconditions() {
		// @formatter:off
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.getOutermostInstance(null, null));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.getOutermostInstance(null, Object.class));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.getOutermostInstance(new Object(), null));
		// @formatter:on
	}

	@Test
	void getOutermostInstance() {
		FirstClass firstClass = new FirstClass();
		FirstClass.SecondClass secondClass = firstClass.new SecondClass();
		FirstClass.SecondClass.ThirdClass thirdClass = secondClass.new ThirdClass();

		assertThat(ReflectionUtils.getOutermostInstance(thirdClass, FirstClass.SecondClass.ThirdClass.class))//
				.contains(thirdClass);
		assertThat(ReflectionUtils.getOutermostInstance(thirdClass, FirstClass.SecondClass.class))//
				.contains(secondClass);
		assertThat(ReflectionUtils.getOutermostInstance(thirdClass, FirstClass.class)).contains(firstClass);
		assertThat(ReflectionUtils.getOutermostInstance(thirdClass, String.class)).isEmpty();
	}

	@Test
	@ExtendWith(TempDirectory.class)
	void getAllClasspathRootDirectories(@Root Path tempDirectory) throws Exception {
		Path root1 = tempDirectory.resolve("root1").toAbsolutePath();
		Path root2 = tempDirectory.resolve("root2").toAbsolutePath();
		String testClassPath = root1 + File.pathSeparator + root2;

		String originalClassPath = System.setProperty("java.class.path", testClassPath);
		try {
			createDirectories(root1, root2);

			assertThat(ReflectionUtils.getAllClasspathRootDirectories()).containsOnly(root1, root2);
		}
		finally {
			System.setProperty("java.class.path", originalClassPath);
		}
	}

	@Test
	void findNestedClassesPreconditions() {
		// @formatter:off
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.findNestedClasses(null, null));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.findNestedClasses(null, clazz -> true));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.findNestedClasses(FirstClass.class, null));
		// @formatter:on
	}

	@Test
	void findNestedClasses() {
		// @formatter:off
		assertThat(ReflectionUtils.findNestedClasses(Object.class, clazz -> true)).isEmpty();

		assertThat(ReflectionUtils.findNestedClasses(ClassWithNestedClasses.class, clazz -> true))
			.containsOnly(Nested1.class, Nested2.class, Nested3.class);

		assertThat(ReflectionUtils.findNestedClasses(ClassWithNestedClasses.class, clazz -> clazz.getName().contains("1")))
			.containsExactly(Nested1.class);

		assertThat(ReflectionUtils.findNestedClasses(ClassWithNestedClasses.class, ReflectionUtils::isStatic))
			.containsExactly(Nested3.class);

		assertThat(ReflectionUtils.findNestedClasses(ClassExtendingClassWithNestedClasses.class, clazz -> true))
			.containsOnly(Nested1.class, Nested2.class, Nested3.class, Nested4.class, Nested5.class);
		// @formatter:on
	}

	@Test
	void getDeclaredConstructorPreconditions() {
		// @formatter:off
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.getDeclaredConstructor(null));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.getDeclaredConstructor(ClassWithTwoConstructors.class));
		// @formatter:on
	}

	@Test
	void getDeclaredConstructor() {
		Constructor<?> constructor = ReflectionUtils.getDeclaredConstructor(getClass());
		assertNotNull(constructor);
		assertEquals(getClass(), constructor.getDeclaringClass());

		constructor = ReflectionUtils.getDeclaredConstructor(ClassWithOneCustomConstructor.class);
		assertNotNull(constructor);
		assertEquals(ClassWithOneCustomConstructor.class, constructor.getDeclaringClass());
		assertEquals(String.class, constructor.getParameterTypes()[0]);
	}

	@Test
	void getMethodPreconditions() {
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.getMethod(null, null));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.getMethod(String.class, null));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.getMethod(null, "hashCode"));
	}

	@Test
	void getMethod() throws Exception {
		assertThat(ReflectionUtils.getMethod(Object.class, "hashCode")).contains(Object.class.getMethod("hashCode"));
		assertThat(ReflectionUtils.getMethod(String.class, "charAt", int.class))//
				.contains(String.class.getMethod("charAt", int.class));

		assertThat(ReflectionUtils.getMethod(Path.class, "subpath", int.class, int.class))//
				.contains(Path.class.getMethod("subpath", int.class, int.class));
		assertThat(ReflectionUtils.getMethod(String.class, "chars")).contains(String.class.getMethod("chars"));

		assertThat(ReflectionUtils.getMethod(String.class, "noSuchMethod")).isEmpty();
		assertThat(ReflectionUtils.getMethod(Object.class, "clone", int.class)).isEmpty();
	}

	@Test
	void isMethodPresentPreconditions() {
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.isMethodPresent(null, m -> true));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.isMethodPresent(getClass(), null));
	}

	@Test
	void isMethodPresent() {
		Predicate<Method> isMethod1 = method -> (method.getName().equals("method1")
				&& method.getParameterTypes().length == 1 && method.getParameterTypes()[0] == String.class);

		assertThat(ReflectionUtils.isMethodPresent(MethodShadowingChild.class, isMethod1)).isTrue();

		assertThat(ReflectionUtils.isMethodPresent(getClass(), isMethod1)).isFalse();
	}

	@Test
	void findMethodByParameterTypesPreconditions() {
		// @formatter:off
		assertThrows(PreconditionViolationException.class, () -> findMethod(null, null));
		assertThrows(PreconditionViolationException.class, () -> findMethod(null, "method"));

		RuntimeException exception = assertThrows(PreconditionViolationException.class, () -> findMethod(String.class, null));
		assertThat(exception).hasMessage("Method name must not be null or blank");

		exception = assertThrows(PreconditionViolationException.class, () -> findMethod(String.class, "   "));
		assertThat(exception).hasMessage("Method name must not be null or blank");

		exception = assertThrows(PreconditionViolationException.class, () -> findMethod(Files.class, "copy", (Class<?>[]) null));
		assertThat(exception).hasMessage("Parameter types array must not be null");

		exception = assertThrows(PreconditionViolationException.class, () -> findMethod(Files.class, "copy", (Class<?>) null));
		assertThat(exception).hasMessage("Individual parameter types must not be null");

		exception = assertThrows(PreconditionViolationException.class, () -> findMethod(Files.class, "copy", new Class<?>[] { Path.class, null }));
		assertThat(exception).hasMessage("Individual parameter types must not be null");
		// @formatter:on
	}

	@Test
	void findMethodByParameterTypes() throws Exception {
		assertThat(findMethod(Object.class, "noSuchMethod")).isEmpty();
		assertThat(findMethod(String.class, "noSuchMethod")).isEmpty();

		assertThat(findMethod(String.class, "chars")).contains(String.class.getMethod("chars"));
		assertThat(findMethod(Files.class, "copy", Path.class, OutputStream.class))//
				.contains(Files.class.getMethod("copy", Path.class, OutputStream.class));

		assertThat(findMethod(MethodShadowingChild.class, "method1", String.class))//
				.contains(MethodShadowingChild.class.getMethod("method1", String.class));
	}

	@Test
	void findMethodByParameterTypesInGenericInterface() {
		Class<?> ifc = InterfaceWithGenericDefaultMethod.class;
		Optional<Method> method = findMethod(ifc, "foo", Number.class);
		assertThat(method).isNotEmpty();
		assertThat(method.get().getName()).isEqualTo("foo");
	}

	/**
	 * @see #findMethodByParameterTypesWithOverloadedMethodNextToGenericDefaultMethod()
	 */
	@Test
	void findMethodByParameterTypesInGenericInterfaceViaParameterizedSubclass() {
		Class<?> clazz = InterfaceWithGenericDefaultMethodImpl.class;
		Optional<Method> method = findMethod(clazz, "foo", Long.class);
		assertThat(method).isNotEmpty();
		assertThat(method.get().getName()).isEqualTo("foo");

		// One might expect or desire that the signature for the generic foo(N)
		// default method would be "foo(java.lang.Long)" when looked up via the
		// concrete parameterized class, but it apparently is only _visible_ as
		// "foo(java.lang.Number)" via reflection. Hence the following assertion
		// checks for java.lang.Number instead of java.lang.Long.
		assertThat(method.get().getParameterTypes()[0]).isEqualTo(Number.class);
	}

	/**
	 * This test is identical to
	 * {@link #findMethodByParameterTypesInGenericInterfaceViaParameterizedSubclass()},
	 * except that this test attempts to find the overloaded
	 * {@link InterfaceWithGenericDefaultMethodImpl#foo(Double)} method instead of
	 * the {@link InterfaceWithGenericDefaultMethod#foo(Number)} default method.
	 */
	@Test
	void findMethodByParameterTypesWithOverloadedMethodNextToGenericDefaultMethod() {
		Class<?> clazz = InterfaceWithGenericDefaultMethodImpl.class;
		Class<?> parameterType = Double.class;
		Optional<Method> method = findMethod(clazz, "foo", parameterType);
		assertThat(method).isNotEmpty();
		assertThat(method.get().getName()).isEqualTo("foo");
		assertThat(method.get().getParameterTypes()[0]).isEqualTo(parameterType);
	}

	@Test
	void findMethodByParameterNamesWithPrimitiveArrayParameter() throws Exception {
		assertFindMethodByParameterNames("methodWithPrimitiveArray", int[].class);
	}

	@Test
	void findMethodByParameterNamesWithTwoDimensionalPrimitiveArrayParameter() throws Exception {
		assertFindMethodByParameterNames("methodWithTwoDimensionalPrimitiveArray", int[][].class);
	}

	@Test
	void findMethodByParameterNamesWithMultidimensionalPrimitiveArrayParameter() throws Exception {
		assertFindMethodByParameterNames("methodWithMultidimensionalPrimitiveArray", int[][][][][].class);
	}

	@Test
	void findMethodByParameterNamesWithObjectArrayParameter() throws Exception {
		assertFindMethodByParameterNames("methodWithObjectArray", String[].class);
	}

	@Test
	void findMethodByParameterNamesWithMultidimensionalObjectArrayParameter() throws Exception {
		assertFindMethodByParameterNames("methodWithMultidimensionalObjectArray", Double[][][][][].class);
	}

	@Test
	void findMethodByParameterNamesWithParameterizedMapParameter() throws Exception {
		String methodName = "methodWithParameterizedMap";

		// standard, supported use case
		assertFindMethodByParameterNames(methodName, Map.class);

		// generic type info in parameter list
		Method method = getClass().getDeclaredMethod(methodName, Map.class);
		String genericParameterTypeName = method.getGenericParameterTypes()[0].getTypeName();
		JUnitException exception = assertThrows(JUnitException.class,
			() -> findMethod(getClass(), methodName, genericParameterTypeName));

		assertThat(exception).hasMessageStartingWith("Failed to load parameter type [java.util.Map<java.lang.String");
	}

	private void assertFindMethodByParameterNames(String methodName, Class<?> parameterType)
			throws NoSuchMethodException {

		Method method = getClass().getDeclaredMethod(methodName, parameterType);
		Optional<Method> optional = findMethod(getClass(), methodName, parameterType.getName());
		assertThat(optional).contains(method);
	}

	@Test
	void findMethodsPreconditions() {
		// @formatter:off
		assertThrows(PreconditionViolationException.class, () -> findMethods(null, null));
		assertThrows(PreconditionViolationException.class, () -> findMethods(null, clazz -> true));
		assertThrows(PreconditionViolationException.class, () -> findMethods(String.class, null));

		assertThrows(PreconditionViolationException.class, () -> findMethods(null, null, null));
		assertThrows(PreconditionViolationException.class, () -> findMethods(null, clazz -> true, BOTTOM_UP));
		assertThrows(PreconditionViolationException.class, () -> findMethods(String.class, null, BOTTOM_UP));
		assertThrows(PreconditionViolationException.class, () -> findMethods(String.class, clazz -> true, null));
		// @formatter:on
	}

	@Test
	void findMethodsInInterface() {
		assertOneFooMethodIn(InterfaceWithOneDeclaredMethod.class);
		assertOneFooMethodIn(InterfaceWithDefaultMethod.class);
		assertOneFooMethodIn(InterfaceWithDefaultMethodImpl.class);
		assertOneFooMethodIn(InterfaceWithStaticMethod.class);
		assertOneFooMethodIn(InterfaceWithStaticMethodImpl.class);
	}

	private static void assertOneFooMethodIn(Class<?> clazz) {
		assertThat(findMethods(clazz, isFooMethod)).hasSize(1);
	}

	@Test
	void findMethodsInObject() {
		List<Method> methods = findMethods(Object.class, method -> true);
		assertNotNull(methods);
		assertTrue(methods.size() > 10);
	}

	@Test
	void findMethodsInVoid() {
		assertThat(findMethods(void.class, method -> true)).isEmpty();
		assertThat(findMethods(Void.class, method -> true)).isEmpty();
	}

	@Test
	void findMethodsInPrimitive() {
		assertThat(findMethods(int.class, method -> true)).isEmpty();
	}

	@Test
	void findMethodsInArrays() {
		assertThat(findMethods(int[].class, method -> true)).isEmpty();
		assertThat(findMethods(Integer[].class, method -> true)).isEmpty();
	}

	@Test
	void findMethodsIgnoresSyntheticMethods() {
		assertTrue(stream(ClassWithSyntheticMethod.class.getDeclaredMethods()).anyMatch(Method::isSynthetic),
			"ClassWithSyntheticMethod must actually contain at least one synthetic method.");

		List<Method> methods = findMethods(ClassWithSyntheticMethod.class, method -> true);
		assertThat(methods).isEmpty();
	}

	@Test
	void findMethodsUsingHierarchyUpMode() throws Exception {
		assertThat(findMethods(ChildClass.class, method -> method.getName().contains("method"), BOTTOM_UP))//
				.containsExactly(ChildClass.class.getMethod("method4"), ParentClass.class.getMethod("method3"),
					GrandparentInterface.class.getMethod("method2"), GrandparentClass.class.getMethod("method1"));

		assertThat(findMethods(ChildClass.class, method -> method.getName().contains("other"), BOTTOM_UP))//
				.containsExactly(ChildClass.class.getMethod("otherMethod3"),
					ParentClass.class.getMethod("otherMethod2"), GrandparentClass.class.getMethod("otherMethod1"));

		assertThat(findMethods(ChildClass.class, method -> method.getName().equals("method2"), BOTTOM_UP))//
				.containsExactly(ParentClass.class.getMethod("method2"));

		assertThat(findMethods(ChildClass.class, method -> method.getName().equals("wrongName"), BOTTOM_UP)).isEmpty();

		assertThat(findMethods(ParentClass.class, method -> method.getName().contains("method"), BOTTOM_UP))//
				.containsExactly(ParentClass.class.getMethod("method3"),
					GrandparentInterface.class.getMethod("method2"), GrandparentClass.class.getMethod("method1"));
	}

	@Test
	void findMethodsUsingHierarchyDownMode() throws Exception {
		assertThat(findMethods(ChildClass.class, method -> method.getName().contains("method"), TOP_DOWN))//
				.containsExactly(GrandparentClass.class.getMethod("method1"),
					GrandparentInterface.class.getMethod("method2"), ParentClass.class.getMethod("method3"),
					ChildClass.class.getMethod("method4"));

		assertThat(findMethods(ChildClass.class, method -> method.getName().contains("other"), TOP_DOWN))//
				.containsExactly(GrandparentClass.class.getMethod("otherMethod1"),
					ParentClass.class.getMethod("otherMethod2"), ChildClass.class.getMethod("otherMethod3"));

		assertThat(findMethods(ChildClass.class, method -> method.getName().equals("method2"), TOP_DOWN))//
				.containsExactly(ParentClass.class.getMethod("method2"));

		assertThat(findMethods(ChildClass.class, method -> method.getName().equals("wrongName"), TOP_DOWN)).isEmpty();

		assertThat(findMethods(ParentClass.class, method -> method.getName().contains("method"), TOP_DOWN))//
				.containsExactly(GrandparentClass.class.getMethod("method1"),
					GrandparentInterface.class.getMethod("method2"), ParentClass.class.getMethod("method3"));
	}

	@Test
	void findMethodsWithShadowingUsingHierarchyUpMode() throws Exception {
		assertThat(findMethods(MethodShadowingChild.class, methodContains1, BOTTOM_UP))//
				.containsExactly(MethodShadowingChild.class.getMethod("method1", String.class));

		assertThat(findMethods(MethodShadowingChild.class, methodContains2, BOTTOM_UP))//
				.containsExactly(MethodShadowingParent.class.getMethod("method2", int.class, int.class, int.class),
					MethodShadowingInterface.class.getMethod("method2", int.class, int.class));

		assertThat(findMethods(MethodShadowingChild.class, methodContains4, BOTTOM_UP))//
				.containsExactly(MethodShadowingChild.class.getMethod("method4", boolean.class));

		assertThat(findMethods(MethodShadowingChild.class, methodContains5, BOTTOM_UP))//
				.containsExactly(MethodShadowingChild.class.getMethod("method5", Long.class),
					MethodShadowingParent.class.getMethod("method5", String.class));

		List<Method> methods = findMethods(MethodShadowingChild.class, method -> true, BOTTOM_UP);
		assertEquals(6, methods.size());
		assertThat(methods.subList(0, 3)).containsOnly(MethodShadowingChild.class.getMethod("method4", boolean.class),
			MethodShadowingChild.class.getMethod("method1", String.class),
			MethodShadowingChild.class.getMethod("method5", Long.class));
		assertThat(methods.subList(3, 5)).containsOnly(
			MethodShadowingParent.class.getMethod("method2", int.class, int.class, int.class),
			MethodShadowingParent.class.getMethod("method5", String.class));
		assertEquals(MethodShadowingInterface.class.getMethod("method2", int.class, int.class), methods.get(5));
	}

	@Test
	void findMethodsWithShadowingUsingHierarchyDownMode() throws Exception {
		assertThat(findMethods(MethodShadowingChild.class, methodContains1, TOP_DOWN))//
				.containsExactly(MethodShadowingChild.class.getMethod("method1", String.class));

		assertThat(findMethods(MethodShadowingChild.class, methodContains2, TOP_DOWN))//
				.containsExactly(MethodShadowingInterface.class.getMethod("method2", int.class, int.class),
					MethodShadowingParent.class.getMethod("method2", int.class, int.class, int.class));

		assertThat(findMethods(MethodShadowingChild.class, methodContains4, TOP_DOWN))//
				.containsExactly(MethodShadowingChild.class.getMethod("method4", boolean.class));

		assertThat(findMethods(MethodShadowingChild.class, methodContains5, TOP_DOWN))//
				.containsExactly(MethodShadowingParent.class.getMethod("method5", String.class),
					MethodShadowingChild.class.getMethod("method5", Long.class));

		List<Method> methods = findMethods(MethodShadowingChild.class, method -> true, TOP_DOWN);
		assertEquals(6, methods.size());
		assertEquals(MethodShadowingInterface.class.getMethod("method2", int.class, int.class), methods.get(0));
		assertThat(methods.subList(1, 3)).containsOnly(
			MethodShadowingParent.class.getMethod("method2", int.class, int.class, int.class),
			MethodShadowingParent.class.getMethod("method5", String.class));
		assertThat(methods.subList(3, 6)).containsOnly(MethodShadowingChild.class.getMethod("method4", boolean.class),
			MethodShadowingChild.class.getMethod("method1", String.class),
			MethodShadowingChild.class.getMethod("method5", Long.class));
	}

	@Test
	void findMethodsWithStaticHidingUsingHierarchyUpMode() throws Exception {
		Class<?> ifc = StaticMethodHidingInterface.class;
		Class<?> parent = StaticMethodHidingParent.class;
		Class<?> child = StaticMethodHidingChild.class;

		Method ifcMethod2 = ifc.getDeclaredMethod("method2", int.class, int.class);
		Method childMethod1 = child.getDeclaredMethod("method1", String.class);
		Method childMethod4 = child.getDeclaredMethod("method4", boolean.class);
		Method childMethod5 = child.getDeclaredMethod("method5", Long.class);
		Method parentMethod2 = parent.getDeclaredMethod("method2", int.class, int.class, int.class);
		Method parentMethod5 = parent.getDeclaredMethod("method5", String.class);

		assertThat(findMethods(child, methodContains1, BOTTOM_UP)).containsExactly(childMethod1);
		assertThat(findMethods(child, methodContains2, BOTTOM_UP)).containsExactly(parentMethod2, ifcMethod2);
		assertThat(findMethods(child, methodContains4, BOTTOM_UP)).containsExactly(childMethod4);
		assertThat(findMethods(child, methodContains5, BOTTOM_UP)).containsExactly(childMethod5, parentMethod5);

		List<Method> methods = findMethods(child, method -> true, BOTTOM_UP);
		assertEquals(6, methods.size());
		assertThat(methods.subList(0, 3)).containsOnly(childMethod1, childMethod4, childMethod5);
		assertThat(methods.subList(3, 5)).containsOnly(parentMethod2, parentMethod5);
		assertEquals(ifcMethod2, methods.get(5));
	}

	@Test
	void findMethodsWithStaticHidingUsingHierarchyDownMode() throws Exception {
		Class<?> ifc = StaticMethodHidingInterface.class;
		Class<?> parent = StaticMethodHidingParent.class;
		Class<?> child = StaticMethodHidingChild.class;

		Method ifcMethod2 = ifc.getDeclaredMethod("method2", int.class, int.class);
		Method childMethod1 = child.getDeclaredMethod("method1", String.class);
		Method childMethod4 = child.getDeclaredMethod("method4", boolean.class);
		Method childMethod5 = child.getDeclaredMethod("method5", Long.class);
		Method parentMethod2 = parent.getDeclaredMethod("method2", int.class, int.class, int.class);
		Method parentMethod5 = parent.getDeclaredMethod("method5", String.class);

		assertThat(findMethods(child, methodContains1, TOP_DOWN)).containsExactly(childMethod1);
		assertThat(findMethods(child, methodContains2, TOP_DOWN)).containsExactly(ifcMethod2, parentMethod2);
		assertThat(findMethods(child, methodContains4, TOP_DOWN)).containsExactly(childMethod4);
		assertThat(findMethods(child, methodContains5, TOP_DOWN)).containsExactly(parentMethod5, childMethod5);

		List<Method> methods = findMethods(child, method -> true, TOP_DOWN);
		assertEquals(6, methods.size());
		assertEquals(ifcMethod2, methods.get(0));
		assertThat(methods.subList(1, 3)).containsOnly(parentMethod2, parentMethod5);
		assertThat(methods.subList(3, 6)).containsOnly(childMethod1, childMethod4, childMethod5);
	}

	@Test
	void findMethodsReturnsAllOverloadedMethodsThatAreNotShadowed() {
		Class<?> clazz = InterfaceWithGenericDefaultMethodImpl.class;

		// Search for all foo(*) methods.
		List<Method> methods = findMethods(clazz, isFooMethod);

		// One might expect or desire that the signature for the generic foo(N)
		// default method would be "foo(java.lang.Long)" when looked up via the
		// concrete parameterized class, but it apparently is only _visible_ as
		// "foo(java.lang.Number)" via reflection.
		assertThat(signaturesOf(methods)).containsExactly("foo(java.lang.Number)", "foo(java.lang.Double)");
	}

	@Test
	void findMethodsDoesNotReturnOverriddenDefaultMethods() {
		Class<?> clazz = InterfaceWithOverriddenGenericDefaultMethodImpl.class;

		// Search for all foo(*) methods.
		List<Method> methods = findMethods(clazz, isFooMethod);
		List<String> signatures = signaturesOf(methods);

		// Although the subsequent assertion covers this case as well, this
		// assertion is in place to provide a more informative failure message.
		assertThat(signatures).as("overridden default method should not be in results").doesNotContain(
			"foo(java.lang.Number)");
		assertThat(signatures).containsExactly("foo(java.lang.Long)", "foo(java.lang.Double)");
	}

	private static List<String> signaturesOf(List<Method> methods) {
		// @formatter:off
		return methods.stream()
			.map(m -> String.format("%s(%s)", m.getName(), ClassUtils.nullSafeToString(m.getParameterTypes())))
			.collect(toList());
		// @formatter:on
	}

	@Test
	void findMethodsIgnoresBridgeMethods() throws Exception {
		assertFalse(Modifier.isPublic(PublicChildClass.class.getSuperclass().getModifiers()));
		assertTrue(Modifier.isPublic(PublicChildClass.class.getModifiers()));
		assertTrue(PublicChildClass.class.getDeclaredMethod("method1").isBridge());
		assertTrue(PublicChildClass.class.getDeclaredMethod("method3").isBridge());

		List<Method> methods = findMethods(PublicChildClass.class, method -> true);
		List<String> signatures = signaturesOf(methods);
		assertThat(signatures).containsOnly("method1()", "method2()", "method3()", "otherMethod1()", "otherMethod2()");
		assertEquals(0, methods.stream().filter(Method::isBridge).count());
	}

	@Test
	void isGeneric() throws Exception {
		for (Method method : Generic.class.getMethods()) {
			assertTrue(ReflectionUtils.isGeneric(method));
		}
		for (Method method : PublicClass.class.getMethods()) {
			assertFalse(ReflectionUtils.isGeneric(method));
		}
	}

	private static void createDirectories(Path... paths) throws IOException {
		for (Path path : paths) {
			Files.createDirectory(path);
		}
	}

	// -------------------------------------------------------------------------

	void methodWithPrimitiveArray(int[] nums) {
	}

	void methodWithTwoDimensionalPrimitiveArray(int[][] grid) {
	}

	void methodWithMultidimensionalPrimitiveArray(int[][][][][] grid) {
	}

	void methodWithObjectArray(String[] info) {
	}

	void methodWithTwoDimensionalObjectArray(String[][] info) {
	}

	void methodWithMultidimensionalObjectArray(Double[][][][][] data) {
	}

	void methodWithParameterizedMap(Map<String, String> map) {
	}

	interface Generic<X, Y, Z extends X> {

		X foo();

		Y foo(X x, Y y);

		Z foo(Z[][] zees);

		<T> int foo(T t);

		<T> T foo(int i);
	}

	class ClassWithSyntheticMethod {

		// The following lambda expression results in a synthetic method in the
		// compiled byte code.
		Comparable<Number> synthetic = number -> 0;
	}

	interface InterfaceWithOneDeclaredMethod {

		void foo();
	}

	interface InterfaceWithDefaultMethod {

		default void foo() {
		}
	}

	static class InterfaceWithDefaultMethodImpl implements InterfaceWithDefaultMethod {
	}

	interface InterfaceWithGenericDefaultMethod<N extends Number> {

		default void foo(N number) {
		}
	}

	static class InterfaceWithGenericDefaultMethodImpl implements InterfaceWithGenericDefaultMethod<Long> {

		void foo(Double number) {
		}
	}

	static class InterfaceWithOverriddenGenericDefaultMethodImpl implements InterfaceWithGenericDefaultMethod<Long> {

		@Override
		public void foo(Long number) {
		}

		void foo(Double number) {
		}
	}

	interface InterfaceWithStaticMethod {

		static void foo() {
		}
	}

	static class InterfaceWithStaticMethodImpl implements InterfaceWithStaticMethod {
	}

	interface InterfaceA {
	}

	interface InterfaceB {
	}

	interface InterfaceC extends InterfaceA, InterfaceB {
	}

	interface InterfaceD {
	}

	static class A implements InterfaceA, InterfaceD {
	}

	static class B extends A implements InterfaceC {
	}

	static class C {

		C() {
		}

		C(String a, String b) {
		}

	}

	static class Exploder {

		Exploder() {
			throw new RuntimeException("boom");
		}

	}

	static class MyClass {

		static final int staticField = 42;

		final int instanceField;

		MyClass(int value) {
			this.instanceField = value;
		}
	}

	static class MySubClass extends MyClass {

		MySubClass(int value) {
			super(value);
		}
	}

	// Intentionally non-static
	public class PublicClass {

		public void publicMethod() {
		}

		public void method(String str, Integer num) {
		}

		public void method(String[] strings, Integer[] nums) {
		}

		public void method(boolean b, char c) {
		}

		public void method(char[] characters, int[] nums) {
		}
	}

	private class PrivateClass {

		@SuppressWarnings("unused")
		private void privateMethod() {
		}
	}

	abstract static class AbstractClass {

		abstract void abstractMethod();
	}

	static class StaticClass {

		static void staticMethod() {
		}
	}

	static class ClassWithVoidAndNonVoidMethods {

		void voidMethod() {
		}

		Void methodReturningVoidReference() {
			return null;
		}

		String methodReturningObject() {
			return "";
		}

		int methodReturningPrimitive() {
			return 0;
		}

	}

	static class InvocationTracker {

		static boolean publicStaticMethodInvoked;
		static boolean privateStaticMethodInvoked;

		boolean publicMethodInvoked;
		boolean privateMethodInvoked;

		public static void publicStaticMethod() {
			publicStaticMethodInvoked = true;
		}

		@SuppressWarnings("unused")
		private static void privateStaticMethod() {
			privateStaticMethodInvoked = true;
		}

		public void publicMethod() {
			publicMethodInvoked = true;
		}

		@SuppressWarnings("unused")
		private void privateMethod() {
			privateMethodInvoked = true;
		}
	}

	static class FirstClass {

		class SecondClass {

			class ThirdClass {
			}
		}
	}

	static class ClassWithNestedClasses {

		class Nested1 {
		}

		class Nested2 {
		}

		static class Nested3 {
		}
	}

	interface InterfaceWithNestedClass {

		class Nested4 {
		}
	}

	interface Interface45 extends InterfaceWithNestedClass {

		class Nested5 {
		}
	}

	static class ClassExtendingClassWithNestedClasses extends ClassWithNestedClasses implements Interface45 {
	}

	static class GrandparentClass {

		public void method1() {
		}

		public void otherMethod1() {
		}
	}

	interface GrandparentInterface {

		default void method2() {
		}
	}

	static class ParentClass extends GrandparentClass implements GrandparentInterface {

		public void method3() {
		}

		public void otherMethod2() {
		}
	}

	static class ChildClass extends ParentClass {

		public void method4() {
		}

		public void otherMethod3() {
		}
	}

	interface MethodShadowingInterface {

		default void method1(String string) {
		}

		default void method2(int i, int j) {
		}
	}

	static class MethodShadowingParent implements MethodShadowingInterface {

		@Override
		public void method1(String string) {
		}

		public void method2(int i, int j, int k) {
		}

		public void method4(boolean flag) {
		}

		public void method5(String string) {
		}
	}

	static class MethodShadowingChild extends MethodShadowingParent {

		@Override
		public void method1(String string) {
		}

		@Override
		public void method4(boolean flag) {
		}

		public void method5(Long i) {
		}
	}

	interface StaticMethodHidingInterface {

		static void method1(String string) {
		}

		static void method2(int i, int j) {
		}
	}

	static class StaticMethodHidingParent implements StaticMethodHidingInterface {

		static void method1(String string) {
		}

		static void method2(int i, int j, int k) {
		}

		static void method4(boolean flag) {
		}

		static void method5(String string) {
		}
	}

	static class StaticMethodHidingChild extends StaticMethodHidingParent {

		static void method1(String string) {
		}

		static void method4(boolean flag) {
		}

		static void method5(Long i) {
		}
	}

	// "public" modifier is necessary here, so that the compiler creates a bridge method.
	public static class PublicChildClass extends ParentClass {

		@Override
		public void otherMethod1() {
		}

		@Override
		public void otherMethod2() {
		}
	}

	@SuppressWarnings("unused")
	private static class ClassWithOneCustomConstructor {

		ClassWithOneCustomConstructor(String str) {
		}
	}

	@SuppressWarnings("unused")
	private static class ClassWithTwoConstructors {

		ClassWithTwoConstructors() {
		}

		ClassWithTwoConstructors(String str) {
		}
	}

}
