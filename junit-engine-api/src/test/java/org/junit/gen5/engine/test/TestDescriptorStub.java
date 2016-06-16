/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.test;

import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.support.descriptor.AbstractTestDescriptor;

/**
 * @since 5.0
 */
public class TestDescriptorStub extends AbstractTestDescriptor {

	private final String displayName;

	public TestDescriptorStub(UniqueId uniqueId, String displayName) {
		super(uniqueId);
		this.displayName = displayName;
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
