/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.discovery;

import java.lang.reflect.Method;

import org.junit.gen5.engine.UniqueId;

abstract class JUnit5Testable {

	private static final JUnit5TestableFactory testableFactory = new JUnit5TestableFactory();

	private static final JUnit5Testable noOpTestable = new JUnit5Testable(null) {
		@Override
		void accept(Visitor visitor) {
			/* no-op */
		}
	};

	@Deprecated
	static JUnit5Testable fromUniqueId(String uniqueId, String engineId) {
		return fromUniqueId(UniqueId.parse(uniqueId), UniqueId.forEngine("engine", engineId));
	}

	static JUnit5Testable fromUniqueId(UniqueId uniqueId, UniqueId engineId) {
		return testableFactory.fromUniqueId(uniqueId, engineId);
	}

	static JUnit5Testable fromClass(Class<?> clazz, UniqueId engineId) {
		return testableFactory.fromClass(clazz, engineId);
	}

	static JUnit5Testable fromMethod(Method testMethod, Class<?> clazz, UniqueId engineId) {
		return testableFactory.fromMethod(testMethod, clazz, engineId);
	}

	static JUnit5Testable doNothing() {
		return noOpTestable;
	}

	private final UniqueId uniqueId;

	JUnit5Testable(UniqueId uniqueId) {
		this.uniqueId = uniqueId;
	}

	UniqueId getUniqueId() {
		return this.uniqueId;
	}

	abstract void accept(Visitor visitor);

	interface Visitor {

		void visitClass(UniqueId uniqueId, Class<?> testClass);

		void visitMethod(UniqueId uniqueId, Method method, Class<?> container);

		void visitNestedClass(UniqueId uniqueId, Class<?> javaClass, Class<?> containerClass);
	}

}
