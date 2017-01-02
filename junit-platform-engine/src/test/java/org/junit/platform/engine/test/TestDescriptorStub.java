/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.test;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

/**
 * @since 1.0
 */
public class TestDescriptorStub extends AbstractTestDescriptor {

	public TestDescriptorStub(UniqueId uniqueId, String displayName) {
		super(uniqueId, displayName);
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
