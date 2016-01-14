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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.api.Assertions.assertThrows;
import static org.junit.gen5.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.gen5.api.Test;

public class ReflectionUtilsTests {

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

		public C() {
		}

		public C(String a, String b) {
		}

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
		assertThat(ReflectionUtils.newInstance(C.class, new Object[] {})).isNotNull();

		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.newInstance(C.class, "one", null));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.newInstance(C.class, null, "two"));
		assertThrows(PreconditionViolationException.class, () -> ReflectionUtils.newInstance(C.class, null, null));
		assertThrows(PreconditionViolationException.class, () -> {
			ReflectionUtils.newInstance(C.class, ((Object[]) null));
		});
	}

}
