/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.commons.util.SerializationUtils.serializeAndDeserialize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestTag;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.support.descriptor.JavaClassSource;
import org.junit.gen5.engine.test.TestDescriptorStub;
import org.junit.jupiter.api.Test;

/**
 * @since 5.0
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
			new TestIdentifier("uniqueId", "displayName", Optional.of(new JavaClassSource(TestIdentifierTests.class)),
				singleton(TestTag.of("aTag")), true, false, Optional.of("parentId")));

		assertEquals("uniqueId", identifier.getUniqueId());
		assertEquals("displayName", identifier.getDisplayName());
		assertThat(identifier.getSource()).contains(new JavaClassSource(TestIdentifierTests.class));
		assertEquals(singleton(TestTag.of("aTag")), identifier.getTags());
		assertTrue(identifier.isTest());
		assertFalse(identifier.isContainer());
		assertThat(identifier.getParentId()).contains("parentId");
	}

}
