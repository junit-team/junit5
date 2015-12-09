/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5ext.testdoubles;

import java.util.Optional;
import java.util.Set;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestSource;
import org.junit.gen5.engine.TestTag;

public class TestDescriptorStub implements TestDescriptor {
	@Override
	public String getUniqueId() {
		return null;
	}

	@Override
	public String getDisplayName() {
		return null;
	}

	@Override
	public Optional<? extends TestDescriptor> getParent() {
		return null;
	}

	@Override
	public boolean isTest() {
		return false;
	}

	@Override
	public Set<TestTag> getTags() {
		return null;
	}

	@Override
	public Set<? extends TestDescriptor> getChildren() {
		return null;
	}

	@Override
	public void accept(Visitor visitor) {

	}

	@Override
	public Optional<TestSource> getSource() {
		return null;
	}
}
