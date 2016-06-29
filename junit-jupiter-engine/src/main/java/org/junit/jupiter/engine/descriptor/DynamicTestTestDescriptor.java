/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.descriptor;

import org.junit.jupiter.api.DynamicTest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

/**
 * {@link TestDescriptor} for a {@link DynamicTest}.
 *
 * @since 5.0
 */
class DynamicTestTestDescriptor extends AbstractTestDescriptor {

	public DynamicTestTestDescriptor(UniqueId uniqueId, DynamicTest dynamicTest, TestSource source) {
		super(uniqueId, dynamicTest.getDisplayName());
		setSource(source);
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
