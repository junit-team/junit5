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

public abstract class JUnit5Testable {

	private final String uniqueId;

	JUnit5Testable(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	abstract void accept(Visitor visitor);

	public String getUniqueId() {
		return uniqueId;
	}

	public interface Visitor {

		void visitClass(String uniqueId, Class<?> testClass);

		void visitMethod(String uniqueId, Method method, Class<?> container);
	}
}
