/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.test.TestDescriptorStub;

/**
 * @since 1.0
 */
public class TestDescriptorTests {

	@Test
	public void isRootWithoutParent() {
		TestDescriptor root = new TestDescriptorStub(UniqueId.root("root", "id"), "id");

		assertTrue(root.isRoot());
	}

	@Test
	public void isRootWithParent() {
		TestDescriptor child = new TestDescriptorStub(UniqueId.root("child", "child"), "child");
		child.setParent(new TestDescriptorStub(UniqueId.root("root", "root"), "root"));

		assertFalse(child.isRoot());
	}
}
