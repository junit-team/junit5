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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Member;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.PreconditionViolationException;

/**
 * Unit tests for {@link ModifierSupport}.
 *
 * @since 1.4
 */
class ModifierSupportTests {

	@Test
	void isPublic() throws Exception {
		assertTrue(ModifierSupport.isPublic(PublicClass.class));
		assertTrue(ModifierSupport.isPublic(PublicClass.class.getMethod("publicMethod")));

		assertFalse(ModifierSupport.isPublic(PrivateClass.class));
		assertFalse(ModifierSupport.isPublic(PrivateClass.class.getDeclaredMethod("privateMethod")));
		assertFalse(ModifierSupport.isPublic(ProtectedClass.class));
		assertFalse(ModifierSupport.isPublic(ProtectedClass.class.getDeclaredMethod("protectedMethod")));
		assertFalse(ModifierSupport.isPublic(PackageVisibleClass.class));
		assertFalse(ModifierSupport.isPublic(PackageVisibleClass.class.getDeclaredMethod("packageVisibleMethod")));

		assertThrows(PreconditionViolationException.class, () -> ModifierSupport.isPublic((Class<?>) null));
		assertThrows(PreconditionViolationException.class, () -> ModifierSupport.isPublic((Member) null));
	}

	@Test
	void isPrivate() throws Exception {
		assertTrue(ModifierSupport.isPrivate(PrivateClass.class));
		assertTrue(ModifierSupport.isPrivate(PrivateClass.class.getDeclaredMethod("privateMethod")));

		assertFalse(ModifierSupport.isPrivate(PublicClass.class));
		assertFalse(ModifierSupport.isPrivate(PublicClass.class.getMethod("publicMethod")));
		assertFalse(ModifierSupport.isPrivate(ProtectedClass.class));
		assertFalse(ModifierSupport.isPrivate(ProtectedClass.class.getDeclaredMethod("protectedMethod")));
		assertFalse(ModifierSupport.isPrivate(PackageVisibleClass.class));
		assertFalse(ModifierSupport.isPrivate(PackageVisibleClass.class.getDeclaredMethod("packageVisibleMethod")));

		assertThrows(PreconditionViolationException.class, () -> ModifierSupport.isPrivate((Class<?>) null));
		assertThrows(PreconditionViolationException.class, () -> ModifierSupport.isPrivate((Member) null));
	}

	@Test
	void isNotPrivate() throws Exception {
		assertTrue(ModifierSupport.isNotPrivate(PublicClass.class.getDeclaredMethod("publicMethod")));
		assertTrue(ModifierSupport.isNotPrivate(ProtectedClass.class.getDeclaredMethod("protectedMethod")));
		assertTrue(ModifierSupport.isNotPrivate(PackageVisibleClass.class.getDeclaredMethod("packageVisibleMethod")));

		assertFalse(ModifierSupport.isNotPrivate(PrivateClass.class.getDeclaredMethod("privateMethod")));

		assertThrows(PreconditionViolationException.class, () -> ModifierSupport.isNotPrivate(null));
	}

	@Test
	void isAbstract() throws Exception {
		assertTrue(ModifierSupport.isAbstract(AbstractClass.class));
		assertTrue(ModifierSupport.isAbstract(AbstractClass.class.getDeclaredMethod("abstractMethod")));

		assertFalse(ModifierSupport.isAbstract(PublicClass.class));
		assertFalse(ModifierSupport.isAbstract(PublicClass.class.getDeclaredMethod("publicMethod")));

		assertThrows(PreconditionViolationException.class, () -> ModifierSupport.isAbstract((Class<?>) null));
		assertThrows(PreconditionViolationException.class, () -> ModifierSupport.isAbstract((Member) null));
	}

	@Test
	void isStatic() throws Exception {
		assertTrue(ModifierSupport.isStatic(StaticClass.class));
		assertTrue(ModifierSupport.isStatic(StaticClass.class.getDeclaredMethod("staticMethod")));

		assertFalse(ModifierSupport.isStatic(PublicClass.class));
		assertFalse(ModifierSupport.isStatic(PublicClass.class.getDeclaredMethod("publicMethod")));

		assertThrows(PreconditionViolationException.class, () -> ModifierSupport.isStatic((Class<?>) null));
		assertThrows(PreconditionViolationException.class, () -> ModifierSupport.isStatic((Member) null));
	}

	@Test
	void isNotStatic() throws Exception {
		assertTrue(ModifierSupport.isNotStatic(PublicClass.class.getDeclaredMethod("publicMethod")));

		assertFalse(ModifierSupport.isNotStatic(StaticClass.class.getDeclaredMethod("staticMethod")));

		assertThrows(PreconditionViolationException.class, () -> ModifierSupport.isNotStatic(null));
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

}
