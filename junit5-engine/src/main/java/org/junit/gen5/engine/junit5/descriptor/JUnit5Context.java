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

import lombok.EqualsAndHashCode;
import lombok.Value;

import org.junit.gen5.engine.AbstractTestDescriptor;

@Value
@EqualsAndHashCode(callSuper = false)
class JUnit5Context extends JUnit5Testable {

	private final Class<?> javaClass;
	private final AbstractTestDescriptor parent;

	JUnit5Context(String uniqueId, Class<?> javaClass, AbstractTestDescriptor parent) {
		super(uniqueId);
		this.javaClass = javaClass;
		this.parent = parent;
	}

	@Override
	void accept(Visitor visitor) {
		visitor.visitContext(getUniqueId(), this.javaClass, this.parent);
	}

}
