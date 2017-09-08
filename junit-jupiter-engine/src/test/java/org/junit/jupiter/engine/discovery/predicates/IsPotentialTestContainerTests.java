/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery.predicates;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

/**
 * @since 5.0
 */
class IsPotentialTestContainerTests {

	private final Predicate<Class<?>> isPotentialTestContainer = new IsPotentialTestContainer();

	@Test
	void abstractClassEvaluatesToFalse() {
		assertFalse(isPotentialTestContainer.test(AbstractClass.class));
	}

	@Test
	void localClassEvaluatesToFalse() {

		class LocalClass {
		}

		assertFalse(isPotentialTestContainer.test(LocalClass.class));
	}

	@Test
	void anonymousClassEvaluatesToFalse() {

		Object object = new Object() {
			@Override
			public String toString() {
				return "";
			}
		};

		assertFalse(isPotentialTestContainer.test(object.getClass()));
	}

	@Test
	void staticClassEvaluatesToTrue() {
		assertTrue(isPotentialTestContainer.test(StaticClass.class));
	}

	static class StaticClass {
	}

}

abstract class AbstractClass {
}
