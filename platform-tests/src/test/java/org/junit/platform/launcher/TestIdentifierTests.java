/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.commons.util.SerializationUtils.serializeAndDeserialize;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.fakes.TestDescriptorStub;

/**
 * @since 1.0
 */
class TestIdentifierTests {

	@Test
	void inheritsIdAndNamesFromDescriptor() {
		TestDescriptor testDescriptor = new TestDescriptorStub(UniqueId.root("aType", "uniqueId"), "displayName");
		TestIdentifier testIdentifier = TestIdentifier.from(testDescriptor);

		assertEquals("[aType:uniqueId]", testIdentifier.getUniqueId());
		assertEquals("displayName", testIdentifier.getDisplayName());
	}

	@Test
	void inheritsTypeFromDescriptor() {
		TestDescriptor descriptor = new TestDescriptorStub(UniqueId.root("aType", "uniqueId"), "displayName");
		TestIdentifier identifier = TestIdentifier.from(descriptor);
		assertEquals(TestDescriptor.Type.TEST, identifier.getType());
		assertTrue(identifier.isTest());
		assertFalse(identifier.isContainer());

		descriptor.addChild(new TestDescriptorStub(UniqueId.root("aChild", "uniqueId"), "displayName"));
		identifier = TestIdentifier.from(descriptor);
		assertEquals(TestDescriptor.Type.CONTAINER, identifier.getType());
		assertFalse(identifier.isTest());
		assertTrue(identifier.isContainer());
	}

	@Test
	void serialization() throws Exception {
		TestIdentifier identifier = serializeAndDeserialize(//
			new TestIdentifier("uniqueId", "displayName", ClassSource.from(TestIdentifierTests.class),
				singleton(TestTag.create("aTag")), TestDescriptor.Type.TEST, "parentId", "reportingName"));

		assertEquals("uniqueId", identifier.getUniqueId());
		assertEquals("displayName", identifier.getDisplayName());
		assertEquals("reportingName", identifier.getLegacyReportingName());
		assertThat(identifier.getSource()).contains(ClassSource.from(TestIdentifierTests.class));
		assertEquals(singleton(TestTag.create("aTag")), identifier.getTags());
		assertEquals(TestDescriptor.Type.TEST, identifier.getType());
		assertTrue(identifier.isTest());
		assertFalse(identifier.isContainer());
		assertThat(identifier.getParentId()).contains("parentId");
	}

}
