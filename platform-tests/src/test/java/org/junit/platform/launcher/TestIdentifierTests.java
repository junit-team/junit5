/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.commons.util.SerializationUtils.deserialize;
import static org.junit.platform.commons.util.SerializationUtils.serialize;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.TestTag;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.fakes.TestDescriptorStub;

/**
 * @since 1.0
 */
class TestIdentifierTests {

	@Test
	void inheritsIdAndNamesFromDescriptor() {
		TestDescriptor testDescriptor = new TestDescriptorStub(UniqueId.root("aType", "uniqueId"), "displayName");
		var testIdentifier = TestIdentifier.from(testDescriptor);

		assertEquals("[aType:uniqueId]", testIdentifier.getUniqueId());
		assertEquals("displayName", testIdentifier.getDisplayName());
	}

	@Test
	void inheritsTypeFromDescriptor() {
		TestDescriptor descriptor = new TestDescriptorStub(UniqueId.root("aType", "uniqueId"), "displayName");
		var identifier = TestIdentifier.from(descriptor);
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
	void currentVersionCanBeSerializedAndDeserialized() throws Exception {
		var originalIdentifier = createOriginalTestIdentifier();
		var deserializedIdentifier = (TestIdentifier) deserialize(serialize(originalIdentifier));
		assertDeepEquals(originalIdentifier, deserializedIdentifier);
	}

	@Test
	void initialVersionCanBeDeserialized() throws Exception {
		try (var inputStream = getClass().getResourceAsStream("/serialized-test-identifier")) {
			var bytes = inputStream.readAllBytes();
			var deserializedIdentifier = (TestIdentifier) deserialize(bytes);
			assertDeepEquals(createOriginalTestIdentifier(), deserializedIdentifier);
		}
	}

	@Test
	void identifierWithNoParentCanBeSerializedAndDeserialized() throws Exception {
		TestIdentifier ti = TestIdentifier.from(new TestDescriptor() {
			@Override
			public UniqueId getUniqueId() {
				return UniqueId.root("example", "id");
			}

			@Override
			public String getDisplayName() {
				return "displayName";
			}

			@Override
			public Set<TestTag> getTags() {
				return Set.of();
			}

			@Override
			public Optional<TestSource> getSource() {
				return Optional.empty();
			}

			@Override
			public Optional<TestDescriptor> getParent() {
				return Optional.empty();
			}

			@Override
			public void setParent(TestDescriptor parent) {
				// ignore
			}

			@Override
			public Set<? extends TestDescriptor> getChildren() {
				return Set.of();
			}

			@Override
			public void addChild(TestDescriptor descriptor) {
				// ignore
			}

			@Override
			public void removeChild(TestDescriptor descriptor) {
				// ignore
			}

			@Override
			public void removeFromHierarchy() {
				// ignore
			}

			@Override
			public Type getType() {
				return Type.TEST;
			}

			@Override
			public Optional<? extends TestDescriptor> findByUniqueId(UniqueId uniqueId) {
				return Optional.empty();
			}
		});
		byte[] bytes = serialize(ti);
		TestIdentifier dti = (TestIdentifier) deserialize(bytes);
		assertEquals(ti, dti);
	}

	private static void assertDeepEquals(TestIdentifier first, TestIdentifier second) {
		assertEquals(first, second);
		assertEquals(first.getUniqueId(), second.getUniqueId());
		assertEquals(first.getUniqueIdObject(), second.getUniqueIdObject());
		assertEquals(first.getDisplayName(), second.getDisplayName());
		assertEquals(first.getLegacyReportingName(), second.getLegacyReportingName());
		assertEquals(first.getSource(), second.getSource());
		assertEquals(first.getTags(), second.getTags());
		assertEquals(first.getType(), second.getType());
		assertEquals(first.getParentId(), second.getParentId());
		assertEquals(first.getParentIdObject(), second.getParentIdObject());
	}

	private static TestIdentifier createOriginalTestIdentifier() {
		var engineDescriptor = new EngineDescriptor(UniqueId.forEngine("engine"), "Engine");
		var uniqueId = engineDescriptor.getUniqueId().append("child", "child");
		var testSource = ClassSource.from(TestIdentifierTests.class);
		var testDescriptor = new AbstractTestDescriptor(uniqueId, "displayName", testSource) {

			@Override
			public Type getType() {
				return Type.TEST;
			}

			@Override
			public String getLegacyReportingName() {
				return "reportingName";
			}

			@Override
			public Set<TestTag> getTags() {
				return Set.of(TestTag.create("aTag"));
			}
		};
		engineDescriptor.addChild(testDescriptor);
		return TestIdentifier.from(testDescriptor);
	}

}
