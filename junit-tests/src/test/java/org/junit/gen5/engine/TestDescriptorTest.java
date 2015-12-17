/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static org.junit.gen5.api.Assertions.*;

import org.junit.Test;

public class TestDescriptorTest {

	@Test
	public void isRootWithoutParent() {
		TestDescriptor root = new TestDescriptorStub("id");

		assertTrue(root.isRoot());
	}

	@Test
	public void isRootWithParent() {
		TestDescriptor child = new TestDescriptorStub("child");
		child.setParent(new TestDescriptorStub("root"));

		assertFalse(child.isRoot());
	}
}
