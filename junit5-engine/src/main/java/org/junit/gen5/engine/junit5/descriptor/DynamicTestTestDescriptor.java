/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import org.junit.gen5.api.DynamicTest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestSource;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.support.descriptor.AbstractTestDescriptor;

/**
 * {@link TestDescriptor} for a {@link DynamicTest}.
 *
 * @since 5.0
 */
class DynamicTestTestDescriptor extends AbstractTestDescriptor {

	private final DynamicTest dynamicTest;

	public DynamicTestTestDescriptor(UniqueId uniqueId, DynamicTest dynamicTest, TestSource source) {
		super(uniqueId);
		this.dynamicTest = dynamicTest;
		setSource(source);
	}

	@Override
	public String getDisplayName() {
		return this.dynamicTest.getDisplayName();
	}

	@Override
	public boolean isTest() {
		return true;
	}

	@Override
	public boolean isContainer() {
		return false;
	}

}
