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

import lombok.Value;

@Value
public class JUnit5Method extends JUnit5Testable {

	private final Class<?> containerClass;
	private final Method javaMethod;

	public JUnit5Method(String uniqueId, Method javaElement, Class<?> containerClass) {
		super(uniqueId);
		this.javaMethod = javaElement;
		this.containerClass = containerClass;
	}

	public void accept(Visitor visitor) {
		visitor.visitMethod(getUniqueId(), javaMethod, containerClass);
	}

}
