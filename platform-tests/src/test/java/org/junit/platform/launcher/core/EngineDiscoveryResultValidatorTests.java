/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.fakes.TestDescriptorStub;

/**
 * @since 1.3
 */
class EngineDiscoveryResultValidatorTests {

	private final EngineDiscoveryResultValidator validator = new EngineDiscoveryResultValidator();

	@Test
	void detectCycleWithDoubleRoot() {
		TestDescriptorStub root = new TestDescriptorStub(UniqueId.forEngine("root"), "root");
		assertTrue(validator.isAcyclic(root));

		root.addChild(root);
		assertFalse(validator.isAcyclic(root));
	}

	@Test
	void detectCycleWithDoubleGroup() {
		UniqueId rootId = UniqueId.forEngine("root");
		TestDescriptorStub root = new TestDescriptorStub(rootId, "root");
		TestDescriptor group1 = new TestDescriptorStub(rootId.append("group", "1"), "1");
		TestDescriptor group2 = new TestDescriptorStub(rootId.append("group", "2"), "2");
		root.addChild(group1);
		root.addChild(group2);
		assertTrue(validator.isAcyclic(root));

		group2.addChild(group1);
		assertFalse(validator.isAcyclic(root));
	}

	@Test
	void detectCycleWithDoubleTest() {
		UniqueId rootId = UniqueId.forEngine("root");
		TestDescriptorStub root = new TestDescriptorStub(rootId, "root");
		TestDescriptor group1 = new TestDescriptorStub(rootId.append("group", "1"), "1");
		TestDescriptor group2 = new TestDescriptorStub(rootId.append("group", "2"), "2");
		root.addChild(group1);
		root.addChild(group2);
		TestDescriptor test1 = new TestDescriptorStub(rootId.append("test", "1"), "1-1");
		TestDescriptor test2 = new TestDescriptorStub(rootId.append("test", "2"), "2-2");
		group1.addChild(test1);
		group2.addChild(test2);
		assertTrue(validator.isAcyclic(root));

		group2.addChild(test1);
		assertFalse(validator.isAcyclic(root));
	}

}
