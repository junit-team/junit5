/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import org.junit.gen5.engine.support.descriptor.AbstractTestDescriptor;

public class TestDescriptorStub extends AbstractTestDescriptor {

	private final String name;
	private final String displayName;

	public TestDescriptorStub(UniqueId uniqueId, String name) {
		this(uniqueId, name, name);
	}

	public TestDescriptorStub(UniqueId uniqueId, String name, String displayName) {
		super(uniqueId);
		this.name = name;
		this.displayName = displayName;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public boolean isTest() {
		return getChildren().isEmpty();
	}

	@Override
	public boolean isContainer() {
		return !isTest();
	}
}
