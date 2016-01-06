/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.testdoubles;

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
	public Optional<TestDescriptor> getParent() {
		return null;
	}

	@Override
	public void setParent(TestDescriptor parent) {
	}

	@Override
	public boolean isTest() {
		return false;
	}

	@Override
	public boolean isContainer() {
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
	public void addChild(TestDescriptor descriptor) {

	}

	@Override
	public void removeChild(TestDescriptor descriptor) {

	}

	@Override
	public void accept(Visitor visitor) {

	}

	@Override
	public Optional<TestSource> getSource() {
		return null;
	}
}
