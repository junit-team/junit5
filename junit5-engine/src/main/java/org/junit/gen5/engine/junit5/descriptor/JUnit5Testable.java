/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import java.lang.reflect.Method;

abstract class JUnit5Testable {

	static JUnit5Testable fromUniqueId(String uniqueId, String engineId) {
		return new JUnit5TestableFactory().fromUniqueId(uniqueId, engineId);
	}

	static JUnit5Testable fromClassName(String className, String engineId) {
		return new JUnit5TestableFactory().fromClassName(className, engineId);
	}

	static JUnit5Testable fromClass(Class<?> clazz, String engineId) {
		return new JUnit5TestableFactory().fromClass(clazz, engineId);
	}

	static JUnit5Testable fromMethod(Method testMethod, Class<?> clazz, String engineId) {
		return new JUnit5TestableFactory().fromMethod(testMethod, clazz, engineId);
	}

	private final String uniqueId;

	JUnit5Testable(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	String getUniqueId() {
		return this.uniqueId;
	}

	abstract void accept(Visitor visitor);

	interface Visitor {

		void visitClass(String uniqueId, Class<?> testClass);

		void visitMethod(String uniqueId, Method method, Class<?> container);

		void visitContext(String uniqueId, Class<?> javaClass, Class<?> containerClass);
	}

}
