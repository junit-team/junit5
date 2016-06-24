/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.discovery.predicates;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

/**
 * @since 5.0
 */
public class IsInnerClassTests {

	private final Predicate<Class<?>> isInnerClass = new IsInnerClass();

	@Test
	void innerClassEvaluatesToTrue() {
		assertTrue(isInnerClass.test(ClassWithInnerClasses.InnerClass.class));
	}

	@Test
	void staticInnerClassEvaluatesToFalse() {
		assertFalse(isInnerClass.test(ClassWithInnerClasses.StaticInnerClass.class));
	}

	@Test
	void privateInnerClassEvaluatesToFalse() {
		// @formatter:off
        Class<?> privateInnerClass = Arrays.stream(ClassWithInnerClasses.class.getDeclaredClasses())
                .filter(aClass -> aClass.getSimpleName().equals("PrivateInnerClass"))
                .findFirst()
                .get();
		// @formatter:on

		assertFalse(isInnerClass.test(privateInnerClass));
	}

}

//class name must not end with 'Tests', otherwise it would be picked up by the suite
class ClassWithInnerClasses {

	class InnerClass {
	}

	static class StaticInnerClass {
	}

	@SuppressWarnings("unused")
	private class PrivateInnerClass {
	}

}
