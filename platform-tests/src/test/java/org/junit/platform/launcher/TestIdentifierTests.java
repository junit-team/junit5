/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.launcher;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.commons.util.SerializationUtils.serializeAndDeserialize;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.DefaultLegacyReportingInfo;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.test.TestDescriptorStub;

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
	void serialization() throws Exception {
		TestIdentifier identifier = serializeAndDeserialize(//
			new TestIdentifier("uniqueId", "displayName", Optional.of(new ClassSource(TestIdentifierTests.class)),
				singleton(TestTag.create("aTag")), true, false, Optional.of("parentId"),
				new DefaultLegacyReportingInfo("method", "class")));

		assertEquals("uniqueId", identifier.getUniqueId());
		assertEquals("displayName", identifier.getDisplayName());
		assertThat(identifier.getLegacyReportingInfo().getMethodName()).contains("method");
		assertThat(identifier.getLegacyReportingInfo().getClassName()).contains("class");
		assertThat(identifier.getSource()).contains(new ClassSource(TestIdentifierTests.class));
		assertEquals(singleton(TestTag.create("aTag")), identifier.getTags());
		assertTrue(identifier.isTest());
		assertFalse(identifier.isContainer());
		assertThat(identifier.getParentId()).contains("parentId");
	}

}
