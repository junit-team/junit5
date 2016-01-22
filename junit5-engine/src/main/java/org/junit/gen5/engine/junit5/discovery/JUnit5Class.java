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

class JUnit5Class extends JUnit5Testable {

	private final Class<?> javaClass;

	JUnit5Class(String uniqueId, Class<?> javaClass) {
		super(uniqueId);
		this.javaClass = javaClass;
	}

	Class<?> getJavaClass() {
		return this.javaClass;
	}

	@Override
	void accept(Visitor visitor) {
		visitor.visitClass(getUniqueId(), this.javaClass);
	}

}
