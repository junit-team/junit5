/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import java.util.HashSet;
import java.util.Set;

public abstract class AbstractTestDescriptor implements TestDescriptor {

	private final String uniqueId;
	private TestDescriptor parent;
	private final Set<TestDescriptor> children = new HashSet<>();

	protected AbstractTestDescriptor(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	@Override
	public boolean equals(Object other) {
		if (other == null)
			return false;
		if (this.getClass() != other.getClass())
			return false;
		TestDescriptor otherDescriptor = (TestDescriptor) other;
		return this.getUniqueId().equals(otherDescriptor.getUniqueId());
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

	public void addChild(AbstractTestDescriptor child) {
		child.setParent(this);
		children.add(child);
	}

	public Set<TestDescriptor> getChildren() {
		return children;
	}
}
