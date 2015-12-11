/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.testdoubles;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestSource;
import org.junit.gen5.engine.TestTag;

public class TestDescriptorWithParentStub implements TestDescriptor {
	public static final String UNIQUE_ID = "TestDescriptorStub.ID";
	public static final String DISPLAY_NAME = "TestDescriptorStub.Name";

	private TestDescriptor parent;

	public TestDescriptorWithParentStub(TestDescriptor parent) {
		this.parent = parent;
	}

	@Override
	public String getUniqueId() {
		return UNIQUE_ID;
	}

	@Override
	public String getDisplayName() {
		return DISPLAY_NAME;
	}

	@Override
	public Optional<TestDescriptor> getParent() {
		return Optional.ofNullable(parent);
	}

	@Override
	public boolean isTest() {
		return true;
	}

	@Override
	public boolean isContainer() {
		return false;
	}

	@Override
	public boolean isRoot() {
		return false;
	}

	@Override
	public Set<TestTag> getTags() {
		return Collections.emptySet();
	}

	@Override
	public void setParent(TestDescriptor parent) {
	}

	@Override
	public Set<TestDescriptor> getChildren() {
		return Collections.emptySet();
	}

	@Override
	public void accept(Visitor visitor) {
	}

	@Override
	public Optional<TestSource> getSource() {
		return Optional.empty();
	}

	@Override
	public void addChild(TestDescriptor descriptor) {
	}

	@Override
	public void removeChild(TestDescriptor descriptor) {
	}

	@Override
	public Set<? extends TestDescriptor> allDescendants() {
		return null;
	}

	@Override
	public long countStaticTests() {
		return 1;
	}

	@Override
	public boolean hasTests() {
		return false;
	}

	@Override
	public Optional<? extends TestDescriptor> findByUniqueId(String uniqueId) {
		return null;
	}
}
