/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.commons.util;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.commons.util.ReflectionUtils.MethodSortOrder.HierarchyDown;
import static org.junit.platform.commons.util.ReflectionUtils.MethodSortOrder.HierarchyUp;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.extensions.TempDirectory;
import org.junit.jupiter.extensions.TempDirectory.Root;
import org.junit.platform.commons.util.ReflectionUtilsTests.ClassWithNestedClasses.Nested1;
import org.junit.platform.commons.util.ReflectionUtilsTests.ClassWithNestedClasses.Nested2;
import org.junit.platform.commons.util.ReflectionUtilsTests.ClassWithNestedClasses.Nested3;

/**
 * Unit tests for {@link ReflectionUtils}.
 *
 * @since 1.0
 */
public class ReflectionUtilsTests {

	@Test
	void getDefaultClassLoaderWithExplicitContextClassLoader() {
		ClassLoader original = Thread.currentThread().getContextClassLoader();
		ClassLoader mock = mock(ClassLoader.class);
		Thread.currentThread().setContextClassLoader(mock);
		try {
			assertSame(mock, ReflectionUtils.getDefaultClassLoader());
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
			assertSame(ClassLoader.getSystemClassLoader(), ReflectionUtils.getDefaultClassLoader());
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
		assertThat(ReflectionUtils.newInstance(C.class, "one", "two")).isNotNull();
		assertThat(ReflectionUtils.newInstance(C.class)).isNotNull();
		assertThat(ReflectionUtils.newInstance(C.class, new Object[0])).isNotNull();

		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.newInstance(C.class, "one", null));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.newInstance(C.class, null, "two"));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.newInstance(C.class, null, null));
		assertThrows(PreconditionViolationException.class, () -> {
			ReflectionUtils.newInstance(C.class, ((Object[]) null));
		});

		RuntimeException exception = assertThrows(RuntimeException.class,
			() -> ReflectionUtils.newInstance(Exploder.class));
		assertThat(exception).hasMessage("boom");
	}

	@Test
	void readFieldValueOfExistingField() {
		Optional<Object> value = ReflectionUtils.readFieldValue(MyClass.class, "value", new MyClass(42));
		assertThat(value).contains(42);
	}

	@Test
	void readFieldValueOfMissingField() {
		Optional<Object> value = ReflectionUtils.readFieldValue(MyClass.class, "doesNotExist", new MyClass(42));
		assertThat(value).isEmpty();
	}

	@Test
	void isAssignableToForNullClass() {
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.isAssignableTo(new Object(), null));
	}

	@Test
	void isAssignableTo() {
		assertTrue(ReflectionUtils.isAssignableTo("string", String.class));
		assertTrue(ReflectionUtils.isAssignableTo("string", CharSequence.class));
		assertTrue(ReflectionUtils.isAssignableTo("string", Object.class));

		assertFalse(ReflectionUtils.isAssignableTo(new Object(), String.class));
		assertFalse(ReflectionUtils.isAssignableTo(Integer.valueOf("1"), StringBuilder.class));
		assertFalse(ReflectionUtils.isAssignableTo(new StringBuilder(), String.class));

		assertTrue(ReflectionUtils.isAssignableTo(new int[0], int[].class));
		assertTrue(ReflectionUtils.isAssignableTo(new double[0], Object.class));
		assertTrue(ReflectionUtils.isAssignableTo(new String[0], String[].class));
		assertTrue(ReflectionUtils.isAssignableTo(new String[0], Object.class));

		assertTrue(ReflectionUtils.isAssignableTo(1, int.class));
		assertTrue(ReflectionUtils.isAssignableTo(Long.valueOf("1"), long.class));
		assertTrue(ReflectionUtils.isAssignableTo(Boolean.TRUE, boolean.class));

		assertFalse(ReflectionUtils.isAssignableTo(1, char.class));
		assertFalse(ReflectionUtils.isAssignableTo(1L, byte.class));
		assertFalse(ReflectionUtils.isAssignableTo(1L, int.class));
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
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.invokeMethod(null, new Object()));

		assertThrows(PreconditionViolationException.class,
			() -> ReflectionUtils.invokeMethod(Object.class.getMethod("hashCode"), null));
	}

	@Test
	void invokePublicMethod() throws Exception {
		InvocationTracker tracker = new InvocationTracker();
		ReflectionUtils.invokeMethod(InvocationTracker.class.getDeclaredMethod("publicMethod"), tracker);
		assertTrue(tracker.publicMethodInvoked);
	}

	@Test
	void invokePrivateMethod() throws Exception {
		InvocationTracker tracker = new InvocationTracker();
		ReflectionUtils.invokeMethod(InvocationTracker.class.getDeclaredMethod("privateMethod"), tracker);
		assertTrue(tracker.privateMethodInvoked);
	}

	@Test
	void invokePublicStaticMethod() throws Exception {
		ReflectionUtils.invokeMethod(InvocationTracker.class.getDeclaredMethod("publicStaticMethod"), null);
		assertTrue(InvocationTracker.publicStaticMethodInvoked);
	}

	@Test
	void invokePrivateStaticMethod() throws Exception {
		ReflectionUtils.invokeMethod(InvocationTracker.class.getDeclaredMethod("privateStaticMethod"), null);
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
	void loadClassWhenClassNotFoundException() throws Exception {
		assertThat(ReflectionUtils.loadClass("foo.bar.EnigmaClassThatDoesNotExist")).isEmpty();
	}

	@Test
	void loadClass() throws Exception {
		Optional<Class<?>> optional = ReflectionUtils.loadClass(Integer.class.getName());
		assertThat(optional).isPresent();
		assertThat(optional).contains(Integer.class);
	}

	@Test
	void loadClassTrimsClassName() throws Exception {
		Optional<Class<?>> optional = ReflectionUtils.loadClass("  " + Integer.class.getName() + "\t");
		assertThat(optional).isPresent();
		assertThat(optional).contains(Integer.class);
	}

	@Test
	void loadClassForPrimitive() throws Exception {
		Optional<Class<?>> optional = ReflectionUtils.loadClass(int.class.getName());
		assertThat(optional).isPresent();
		assertThat(optional).contains(int.class);
	}

	@Test
	void loadClassForPrimitiveArray() throws Exception {
		Optional<Class<?>> optional = ReflectionUtils.loadClass(int[].class.getName());
		assertThat(optional).isPresent();
		assertThat(optional).contains(int[].class);
	}

	@Test
	void loadClassForObjectArray() throws Exception {
		Optional<Class<?>> optional = ReflectionUtils.loadClass(String[].class.getName());
		assertThat(optional).isPresent();
		assertThat(optional).contains(String[].class);
	}

	@Test
	void loadMethodPreconditions() {
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.loadMethod(null));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.loadMethod(""));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.loadMethod("    "));
	}

	@Test
	void loadMethodWithInvalidFormatForFullyQualifiedMethodName() {
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.loadMethod(null));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.loadMethod(""));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.loadMethod("   "));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.loadMethod("method"));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.loadMethod("#nonexistentMethod"));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.loadMethod("java.lang.String#"));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.loadMethod("java.lang.String#chars("));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.loadMethod("java.lang.String#chars)"));
	}

	@Test
	void loadMethod() {
		assertThat(ReflectionUtils.loadMethod(PublicClass.class.getName() + "#publicMethod")).isPresent();
		assertThat(ReflectionUtils.loadMethod(PrivateClass.class.getName() + "#privateMethod")).isPresent();
		assertThat(ReflectionUtils.loadMethod("  " + PrivateClass.class.getName() + "#privateMethod  ")).isPresent();

		assertThat(ReflectionUtils.loadMethod("org.example.NonexistentClass#nonexistentMethod")).isEmpty();
		assertThat(ReflectionUtils.loadMethod(PublicClass.class.getName() + "#nonexistentMethod")).isEmpty();

		// missing java.lang.String parameter type
		assertThat(ReflectionUtils.loadMethod("java.lang.String#equalsIgnoreCase")).isEmpty();
	}

	@Test
	void loadMethodWithArgumentList() {
		assertAll(//
			() -> assertFqmn(fqmn(PublicClass.class, "method", String.class, Integer.class)), //
			() -> assertFqmn(fqmn(PublicClass.class, "method", String[].class, Integer[].class)), //
			() -> assertFqmn(fqmn(PublicClass.class, "method", boolean.class, char.class)), //
			() -> assertFqmn(fqmn(PublicClass.class, "method", char[].class, int[].class))//
		);
	}

	@Test
	void loadMethodFromSuperclassOrInterface() throws Exception {
		assertAll(//
			() -> assertFqmn(fqmn(ChildClass.class, "method1")), //
			() -> assertFqmn(fqmn(ChildClass.class, "method2")), //
			() -> assertFqmn(fqmn(ChildClass.class, "method3")), //
			() -> assertFqmn(fqmn(ChildClass.class, "method4"))//
		);
	}

	private static String fqmn(Class<?> clazz, String methodName, Class<?>... params) {
		return String.format("%s#%s(%s)", clazz.getName(), methodName, StringUtils.nullSafeToString(params));
	}

	private static void assertFqmn(String fqmn) {
		assertThat(ReflectionUtils.loadMethod(fqmn)).as(fqmn).isPresent();
	}

	@Test
	void getOuterInstancePreconditions() {
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.getOuterInstance(null, null));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.getOuterInstance(null, Object.class));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.getOuterInstance(new Object(), null));
	}

	@Test
	void getOuterInstance() {
		FirstClass firstClass = new FirstClass();
		FirstClass.SecondClass secondClass = firstClass.new SecondClass();
		FirstClass.SecondClass.ThirdClass thirdClass = secondClass.new ThirdClass();

		assertThat(ReflectionUtils.getOuterInstance(thirdClass, FirstClass.SecondClass.ThirdClass.class)).contains(
			thirdClass);
		assertThat(ReflectionUtils.getOuterInstance(thirdClass, FirstClass.SecondClass.class)).contains(secondClass);
		assertThat(ReflectionUtils.getOuterInstance(thirdClass, FirstClass.class)).contains(firstClass);
		assertThat(ReflectionUtils.getOuterInstance(thirdClass, String.class)).isEmpty();
	}

	@Test
	void isPackage() {
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.isPackage(null));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.isPackage("     "));

		assertFalse(ReflectionUtils.isPackage("org.non.existing.package"));

		assertTrue(ReflectionUtils.isPackage("org.junit.platform.commons.util"));
		assertTrue(ReflectionUtils.isPackage("org.junit.platform.commons"));
		assertTrue(ReflectionUtils.isPackage("org.junit.platform"));
		assertTrue(ReflectionUtils.isPackage("")); // default package
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
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.findNestedClasses(null, null));
		assertThrows(PreconditionViolationException.class,
			() -> ReflectionUtils.findNestedClasses(null, clazz -> true));
		assertThrows(PreconditionViolationException.class,
			() -> ReflectionUtils.findNestedClasses(FirstClass.class, null));
	}

	@Test
	void findNestedClasses() {
		// @formatter:off
		assertThat(ReflectionUtils.findNestedClasses(Object.class, clazz -> true))
			.isEmpty();

		assertThat(ReflectionUtils.findNestedClasses(ClassWithNestedClasses.class, clazz -> true))
			.containsOnly(Nested1.class, Nested2.class, Nested3.class);

		assertThat(ReflectionUtils.findNestedClasses(ClassWithNestedClasses.class, clazz -> clazz.getName().contains("1")))
			.containsExactly(Nested1.class);

		assertThat(ReflectionUtils.findNestedClasses(ClassWithNestedClasses.class, ReflectionUtils::isStatic))
			.containsExactly(Nested3.class);
		// @formatter:on
	}

	@Test
	void getDeclaredConstructorPreconditions() {
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.getDeclaredConstructor(null));
		assertThrows(PreconditionViolationException.class,
			() -> ReflectionUtils.getDeclaredConstructor(ClassWithTwoConstructors.class));
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
		assertThat(ReflectionUtils.getMethod(String.class, "charAt", int.class)).contains(
			String.class.getMethod("charAt", int.class));

		assertThat(ReflectionUtils.getMethod(Path.class, "subpath", int.class, int.class)).contains(
			Path.class.getMethod("subpath", int.class, int.class));
		assertThat(ReflectionUtils.getMethod(String.class, "chars")).contains(String.class.getMethod("chars"));

		assertThrows(NoSuchMethodException.class, () -> ReflectionUtils.getMethod(String.class, "noSuchMethod"));
		assertThrows(NoSuchMethodException.class, () -> ReflectionUtils.getMethod(Object.class, "clone", int.class));
	}

	@Test
	void findMethodPreconditions() throws Exception {
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.findMethod(null, null));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.findMethod(null, "method"));
	}

	@Test
	void findMethod() throws Exception {
		assertThat(ReflectionUtils.findMethod(Object.class, "noSuchMethod")).isEmpty();
		assertThat(ReflectionUtils.findMethod(String.class, "noSuchMethod")).isEmpty();

		assertThat(ReflectionUtils.findMethod(String.class, "chars")).contains(String.class.getMethod("chars"));
		assertThat(ReflectionUtils.findMethod(Files.class, "copy", Path.class, OutputStream.class)).contains(
			Files.class.getMethod("copy", Path.class, OutputStream.class));

		RuntimeException exception = assertThrows(PreconditionViolationException.class,
			() -> ReflectionUtils.findMethod(String.class, null));
		assertThat(exception).hasMessage("method name must not be null or empty");

		exception = assertThrows(PreconditionViolationException.class,
			() -> ReflectionUtils.findMethod(String.class, "   "));
		assertThat(exception).hasMessage("method name must not be null or empty");
	}

	@Test
	void findMethodsPreconditions() {
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.findMethods(null, null));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.findMethods(null, clazz -> true));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.findMethods(String.class, null));

		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.findMethods(null, null, null));
		assertThrows(PreconditionViolationException.class,
			() -> ReflectionUtils.findMethods(null, clazz -> true, HierarchyUp));
		assertThrows(PreconditionViolationException.class,
			() -> ReflectionUtils.findMethods(String.class, null, HierarchyUp));
		assertThrows(PreconditionViolationException.class,
			() -> ReflectionUtils.findMethods(String.class, clazz -> true, null));
	}

	@Test
	void findMethodsInInterface() {
		List<Method> methods = ReflectionUtils.findMethods(InterfaceWithOneDeclaredMethod.class,
			method -> method.getName().contains("foo"));
		assertNotNull(methods);
		assertEquals(1, methods.size());

		methods = ReflectionUtils.findMethods(InterfaceWithDefaultMethod.class,
			method -> method.getName().contains("foo"));
		assertNotNull(methods);
		assertEquals(1, methods.size());

		methods = ReflectionUtils.findMethods(InterfaceWithDefaultMethodImpl.class,
			method -> method.getName().contains("foo"));
		assertNotNull(methods);
		assertEquals(1, methods.size());

		methods = ReflectionUtils.findMethods(InterfaceWithStaticMethod.class,
			method -> method.getName().contains("foo"));
		assertNotNull(methods);
		assertEquals(1, methods.size());

		methods = ReflectionUtils.findMethods(InterfaceWithStaticMethodImpl.class,
			method -> method.getName().contains("foo"));
		assertNotNull(methods);
		assertEquals(1, methods.size());
	}

	@Test
	void findMethodsInObject() {
		List<Method> methods = ReflectionUtils.findMethods(Object.class, method -> true);
		assertNotNull(methods);
		assertTrue(methods.size() > 10);
	}

	@Test
	void findMethodsInVoid() {
		List<Method> methods = ReflectionUtils.findMethods(void.class, method -> true);
		assertNotNull(methods);
		assertEquals(0, methods.size());

		methods = ReflectionUtils.findMethods(Void.class, method -> true);
		assertNotNull(methods);
		assertEquals(0, methods.size());
	}

	@Test
	void findMethodsInPrimitive() {
		List<Method> methods = ReflectionUtils.findMethods(int.class, method -> true);
		assertNotNull(methods);
		assertEquals(0, methods.size());
	}

	@Test
	void findMethodsInArrays() {
		List<Method> methods = ReflectionUtils.findMethods(int[].class, method -> true);
		assertNotNull(methods);
		assertEquals(0, methods.size());

		methods = ReflectionUtils.findMethods(Integer[].class, method -> true);
		assertNotNull(methods);
		assertEquals(0, methods.size());
	}

	@Test
	void findMethodsIgnoresSyntheticMethods() {
		List<Method> methods = ReflectionUtils.findMethods(ClassWithSyntheticMethod.class, method -> true);
		assertNotNull(methods);
		assertEquals(0, methods.size());
	}

	@Test
	void findMethodsUsingHierarchyUpMode() throws Exception {
		assertThat(ReflectionUtils.findMethods(ChildClass.class, method -> method.getName().contains("method"),
			HierarchyUp)).containsExactly(ChildClass.class.getMethod("method4"), ParentClass.class.getMethod("method3"),
				GrandparentInterface.class.getMethod("method2"), GrandparentClass.class.getMethod("method1"));

		assertThat(ReflectionUtils.findMethods(ChildClass.class, method -> method.getName().contains("other"),
			HierarchyUp)).containsExactly(ChildClass.class.getMethod("otherMethod3"),
				ParentClass.class.getMethod("otherMethod2"), GrandparentClass.class.getMethod("otherMethod1"));

		assertThat(ReflectionUtils.findMethods(ChildClass.class, method -> method.getName().equals("method2"),
			HierarchyUp)).containsExactly(ParentClass.class.getMethod("method2"));

		assertThat(ReflectionUtils.findMethods(ChildClass.class, method -> method.getName().equals("wrongName"),
			HierarchyUp)).isEmpty();

		assertThat(ReflectionUtils.findMethods(ParentClass.class, method -> method.getName().contains("method"),
			HierarchyUp)).containsExactly(ParentClass.class.getMethod("method3"),
				GrandparentInterface.class.getMethod("method2"), GrandparentClass.class.getMethod("method1"));
	}

	@Test
	void findMethodsUsingHierarchyDownMode() throws Exception {
		assertThat(ReflectionUtils.findMethods(ChildClass.class, method -> method.getName().contains("method"),
			HierarchyDown)).containsExactly(GrandparentClass.class.getMethod("method1"),
				GrandparentInterface.class.getMethod("method2"), ParentClass.class.getMethod("method3"),
				ChildClass.class.getMethod("method4"));

		assertThat(ReflectionUtils.findMethods(ChildClass.class, method -> method.getName().contains("other"),
			HierarchyDown)).containsExactly(GrandparentClass.class.getMethod("otherMethod1"),
				ParentClass.class.getMethod("otherMethod2"), ChildClass.class.getMethod("otherMethod3"));

		assertThat(ReflectionUtils.findMethods(ChildClass.class, method -> method.getName().equals("method2"),
			HierarchyDown)).containsExactly(ParentClass.class.getMethod("method2"));

		assertThat(ReflectionUtils.findMethods(ChildClass.class, method -> method.getName().equals("wrongName"),
			HierarchyDown)).isEmpty();

		assertThat(ReflectionUtils.findMethods(ParentClass.class, method -> method.getName().contains("method"),
			HierarchyDown)).containsExactly(GrandparentClass.class.getMethod("method1"),
				GrandparentInterface.class.getMethod("method2"), ParentClass.class.getMethod("method3"));
	}

	@Test
	void findMethodsWithShadowingUsingHierarchyUpMode() throws Exception {
		assertThat(ReflectionUtils.findMethods(MethodShadowingChild.class, method -> method.getName().contains("1"),
			HierarchyUp)).containsExactly(MethodShadowingChild.class.getMethod("method1", String.class));

		assertThat(ReflectionUtils.findMethods(MethodShadowingChild.class, method -> method.getName().contains("2"),
			HierarchyUp)).containsExactly(
				MethodShadowingParent.class.getMethod("method2", int.class, int.class, int.class),
				MethodShadowingInterface.class.getMethod("method2", int.class, int.class));

		assertThat(ReflectionUtils.findMethods(MethodShadowingChild.class, method -> method.getName().contains("4"),
			HierarchyUp)).containsExactly(MethodShadowingChild.class.getMethod("method4", boolean.class));

		assertThat(ReflectionUtils.findMethods(MethodShadowingChild.class, method -> method.getName().contains("5"),
			HierarchyUp)).containsExactly(MethodShadowingChild.class.getMethod("method5", Long.class),
				MethodShadowingParent.class.getMethod("method5", String.class));

		List<Method> methods = ReflectionUtils.findMethods(MethodShadowingChild.class, method -> true, HierarchyUp);
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
		assertThat(ReflectionUtils.findMethods(MethodShadowingChild.class, method -> method.getName().contains("1"),
			HierarchyDown)).containsExactly(MethodShadowingChild.class.getMethod("method1", String.class));

		assertThat(ReflectionUtils.findMethods(MethodShadowingChild.class, method -> method.getName().contains("2"),
			HierarchyDown)).containsExactly(MethodShadowingInterface.class.getMethod("method2", int.class, int.class),
				MethodShadowingParent.class.getMethod("method2", int.class, int.class, int.class));

		assertThat(ReflectionUtils.findMethods(MethodShadowingChild.class, method -> method.getName().contains("4"),
			HierarchyDown)).containsExactly(MethodShadowingChild.class.getMethod("method4", boolean.class));

		assertThat(ReflectionUtils.findMethods(MethodShadowingChild.class, method -> method.getName().contains("5"),
			HierarchyDown)).containsExactly(MethodShadowingParent.class.getMethod("method5", String.class),
				MethodShadowingChild.class.getMethod("method5", Long.class));

		List<Method> methods = ReflectionUtils.findMethods(MethodShadowingChild.class, method -> true, HierarchyDown);
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
	void findMethodsIgnoresBridgeMethods() throws Exception {
		assertFalse(Modifier.isPublic(PublicChildClass.class.getSuperclass().getModifiers()));
		assertTrue(Modifier.isPublic(PublicChildClass.class.getModifiers()));
		assertTrue(PublicChildClass.class.getDeclaredMethod("method1").isBridge());
		assertTrue(PublicChildClass.class.getDeclaredMethod("method3").isBridge());

		List<Method> methods = ReflectionUtils.findMethods(PublicChildClass.class, method -> true);
		List<String> names = methods.stream().map(Method::getName).collect(toList());
		assertThat(names).containsOnly("method1", "method2", "method3", "otherMethod1", "otherMethod2");
		assertTrue(methods.stream().filter(Method::isBridge).count() == 0);
	}

	private static void createDirectories(Path... paths) throws IOException {
		for (Path path : paths) {
			Files.createDirectory(path);
		}
	}

	class ClassWithSyntheticMethod {
		Runnable foo = InterfaceWithStaticMethod::foo;
		Runnable bar = StaticClass::staticMethod;
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

		final int value;

		MyClass(int value) {
			this.value = value;
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

	// "public" modifier is necessary here, the compiler creates a bridge method
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
