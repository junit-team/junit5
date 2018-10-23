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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * Unit tests for {@link ModifierSupport}.
 *
 * @since 1.4
 */
class ModifierSupportTests {

	@Test
	void isPublicPreconditions() throws Exception {
		assertThrows(PreconditionViolationException.class, () -> ModifierSupport.isPublic((Class<?>) null));
		assertThrows(PreconditionViolationException.class, () -> ModifierSupport.isPublic((Member) null));
	}

	@Classes
	void isPublicDelegates(Class<?> clazz) throws Exception {
		assertEquals(ReflectionUtils.isPublic(clazz), ModifierSupport.isPublic(clazz));
	}

	@Methods
	void isPublicDelegates(Method method) throws Exception {
		assertEquals(ReflectionUtils.isPublic(method), ModifierSupport.isPublic(method));
	}

	@Test
	void isPrivatePreconditions() throws Exception {
		assertThrows(PreconditionViolationException.class, () -> ModifierSupport.isPrivate((Class<?>) null));
		assertThrows(PreconditionViolationException.class, () -> ModifierSupport.isPrivate((Member) null));
	}

	@Classes
	void isPrivateDelegates(Class<?> clazz) throws Exception {
		assertEquals(ReflectionUtils.isPrivate(clazz), ModifierSupport.isPrivate(clazz));
	}

	@Methods
	void isPrivateDelegates(Method method) throws Exception {
		assertEquals(ReflectionUtils.isPrivate(method), ModifierSupport.isPrivate(method));
	}

	@Test
	void isNotPrivatePreconditions() throws Exception {
		assertThrows(PreconditionViolationException.class, () -> ModifierSupport.isNotPrivate((Class<?>) null));
		assertThrows(PreconditionViolationException.class, () -> ModifierSupport.isNotPrivate((Member) null));
	}

	@Classes
	void isNotPrivateDelegates(Class<?> clazz) throws Exception {
		assertEquals(ReflectionUtils.isNotPrivate(clazz), ModifierSupport.isNotPrivate(clazz));
	}

	@Methods
	void isNotPrivateDelegates(Method method) throws Exception {
		assertEquals(ReflectionUtils.isNotPrivate(method), ModifierSupport.isNotPrivate(method));
	}

	@Test
	void isAbstractPreconditions() throws Exception {
		assertThrows(PreconditionViolationException.class, () -> ModifierSupport.isAbstract((Class<?>) null));
		assertThrows(PreconditionViolationException.class, () -> ModifierSupport.isAbstract((Member) null));
	}

	@Classes
	void isAbstractDelegates(Class<?> clazz) throws Exception {
		assertEquals(ReflectionUtils.isAbstract(clazz), ModifierSupport.isAbstract(clazz));
	}

	@Methods
	void isAbstractDelegates(Method method) throws Exception {
		assertEquals(ReflectionUtils.isAbstract(method), ModifierSupport.isAbstract(method));
	}

	@Test
	void isStaticPreconditions() throws Exception {
		assertThrows(PreconditionViolationException.class, () -> ModifierSupport.isStatic((Class<?>) null));
		assertThrows(PreconditionViolationException.class, () -> ModifierSupport.isStatic((Member) null));
	}

	@Classes
	void isStaticDelegates(Class<?> clazz) throws Exception {
		assertEquals(ReflectionUtils.isStatic(clazz), ModifierSupport.isStatic(clazz));
	}

	@Methods
	void isStaticDelegates(Method method) throws Exception {
		assertEquals(ReflectionUtils.isStatic(method), ModifierSupport.isStatic(method));
	}

	@Test
	void isNotStaticPreconditions() throws Exception {
		assertThrows(PreconditionViolationException.class, () -> ModifierSupport.isNotStatic((Class<?>) null));
		assertThrows(PreconditionViolationException.class, () -> ModifierSupport.isNotStatic((Member) null));
	}

	@Classes
	void isNotStaticDelegates(Class<?> clazz) throws Exception {
		assertEquals(ReflectionUtils.isNotStatic(clazz), ModifierSupport.isNotStatic(clazz));
	}

	@Methods
	void isNotStaticDelegates(Method method) throws Exception {
		assertEquals(ReflectionUtils.isNotStatic(method), ModifierSupport.isNotStatic(method));
	}

	// -------------------------------------------------------------------------

	// Intentionally non-static
	public class PublicClass {

		public void publicMethod() {
		}
	}

	private class PrivateClass {

		@SuppressWarnings("unused")
		private void privateMethod() {
		}
	}

	protected class ProtectedClass {

		@SuppressWarnings("unused")
		protected void protectedMethod() {
		}
	}

	class PackageVisibleClass {

		@SuppressWarnings("unused")
		void packageVisibleMethod() {
		}
	}

	abstract static class AbstractClass {

		abstract void abstractMethod();
	}

	static class StaticClass {

		static void staticMethod() {
		}
	}

	// -------------------------------------------------------------------------

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@ParameterizedTest
	@ValueSource(classes = { PublicClass.class, PrivateClass.class, ProtectedClass.class, PackageVisibleClass.class,
			AbstractClass.class, StaticClass.class })
	@interface Classes {
	}

	@Target(ElementType.METHOD)
	@Retention(RetentionPolicy.RUNTIME)
	@ParameterizedTest
	@MethodSource("methods")
	@interface Methods {
	}

	static Stream<Method> methods() throws Exception {
		return Stream.of(//
			PublicClass.class.getMethod("publicMethod"), //
			PrivateClass.class.getDeclaredMethod("privateMethod"),
			ProtectedClass.class.getDeclaredMethod("protectedMethod"),
			PackageVisibleClass.class.getDeclaredMethod("packageVisibleMethod"),
			AbstractClass.class.getDeclaredMethod("abstractMethod"),
			StaticClass.class.getDeclaredMethod("staticMethod"));
	}

}
