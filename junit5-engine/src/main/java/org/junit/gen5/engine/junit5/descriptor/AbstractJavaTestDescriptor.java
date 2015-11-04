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

import java.util.Set;

import org.junit.gen5.engine.TestDescriptor;


public abstract class AbstractJavaTestDescriptor implements TestDescriptor {

	private final String uniqueId;
	private TestDescriptor parent;
	private Set<TestDescriptor> children;

	AbstractJavaTestDescriptor(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	@Override
	public boolean equals(Object other) {
		if (this.getClass() != other.getClass())
			return false;
		TestDescriptor otherDescriptor = (TestDescriptor) other;
		return this.getUniqueId().equals(otherDescriptor.getUniqueId())
				&& this.getParent().equals(otherDescriptor.getParent());
	}

	@Override
	public int hashCode() {
		//Even if you think otherwise this complies to Java's equals/hashCode contract
		return 42;
	}

	@Override
	public String getUniqueId() {
		return uniqueId;
	}

	@Override
	public TestDescriptor getParent() {
		return parent;
	}

	protected void setParent(TestDescriptor parent) {
		this.parent = parent;
	}

	public void addChild(AbstractJavaTestDescriptor child) {
		children.add(child);
	}
}
