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

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.engine.MutableTestDescriptor;
import org.junit.gen5.engine.TestSource;
import org.junit.gen5.engine.TestTag;

public class MutableTestDescriptorStub implements MutableTestDescriptor {
	public static final String UNIQUE_ID = "MutableTestDescriptorStub.ID";
	public static final String DISPLAY_NAME = "MutableTestDescriptorStub.Name";

	private MutableTestDescriptor parent;

	public MutableTestDescriptorStub(MutableTestDescriptor parent) {
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
	public Optional<MutableTestDescriptor> getParent() {
		return Optional.ofNullable(parent);
	}

	@Override
	public boolean isTest() {
		return false;
	}

	@Override
	public Set<TestTag> getTags() {
		return Collections.emptySet();
	}

	@Override
	public void setParent(MutableTestDescriptor parent) {
	}

	@Override
	public Set<MutableTestDescriptor> getChildren() {
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
	public void addChild(MutableTestDescriptor descriptor) {
	}

	@Override
	public void removeChild(MutableTestDescriptor descriptor) {
	}
}
