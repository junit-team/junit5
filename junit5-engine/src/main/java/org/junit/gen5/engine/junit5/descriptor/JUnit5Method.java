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

class JUnit5Method extends JUnit5Testable {

	private final Method javaMethod;
	private final Class<?> containerClass;

	JUnit5Method(String uniqueId, Method javaMethod, Class<?> containerClass) {
		super(uniqueId);
		this.javaMethod = javaMethod;
		this.containerClass = containerClass;
	}

	public Method getJavaMethod() {
		return javaMethod;
	}

	public Class<?> getContainerClass() {
		return containerClass;
	}

	@Override
	void accept(Visitor visitor) {
		visitor.visitMethod(getUniqueId(), this.javaMethod, this.containerClass);
	}

}
