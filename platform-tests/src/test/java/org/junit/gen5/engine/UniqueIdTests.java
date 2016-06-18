/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Optional;

import org.junit.gen5.engine.UniqueId.Segment;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Microtests for class {@link UniqueId}
 *
 * @since 5.0
 */
class UniqueIdTests {

	static final String ENGINE_ID = "junit5";

	@Nested
	class Creation {

		@Test
		void uniqueIdCanBeCreatedFromEngineId() {
			UniqueId uniqueId = UniqueId.forEngine(ENGINE_ID);

			assertEquals("[engine:junit5]", uniqueId.toString());
			assertSegment(uniqueId.getSegments().get(0), "engine", "junit5");
		}

		@Test
		void retrievingOptionalEngineId() {
			UniqueId uniqueIdWithEngine = UniqueId.forEngine(ENGINE_ID);
			assertEquals("junit5", uniqueIdWithEngine.getEngineId().get());

			UniqueId uniqueIdWithoutEngine = UniqueId.root("root", "avalue");
			assertEquals(Optional.empty(), uniqueIdWithoutEngine.getEngineId());
		}

		@Test
		void uniqueIdCanBeCreatedFromTypeAndValue() {
			UniqueId uniqueId = UniqueId.root("aType", "aValue");

			assertEquals("[aType:aValue]", uniqueId.toString());
			assertSegment(uniqueId.getSegments().get(0), "aType", "aValue");
		}

		@Test
		void rootSegmentCanBeRetrieved() {
			UniqueId uniqueId = UniqueId.root("aType", "aValue");

			assertEquals(new Segment("aType", "aValue"), uniqueId.getRoot().get());
		}

		@Test
		void appendingOneSegment() {
			UniqueId engineId = UniqueId.root("engine", ENGINE_ID);
			UniqueId classId = engineId.append("class", "org.junit.MyClass");

			assertEquals(2, classId.getSegments().size());
			assertSegment(classId.getSegments().get(0), "engine", ENGINE_ID);
			assertSegment(classId.getSegments().get(1), "class", "org.junit.MyClass");
		}

		@Test
		void appendingSegmentLeavesOriginalUnchanged() {
			UniqueId uniqueId = UniqueId.root("engine", ENGINE_ID);
			uniqueId.append("class", "org.junit.MyClass");

			assertEquals(1, uniqueId.getSegments().size());
			assertSegment(uniqueId.getSegments().get(0), "engine", ENGINE_ID);
		}

		@Test
		void appendingSeveralSegments() {
			UniqueId engineId = UniqueId.root("engine", ENGINE_ID);
			UniqueId uniqueId = engineId.append("t1", "v1").append("t2", "v2").append("t3", "v3");

			assertEquals(4, uniqueId.getSegments().size());
			assertSegment(uniqueId.getSegments().get(0), "engine", ENGINE_ID);
			assertSegment(uniqueId.getSegments().get(1), "t1", "v1");
			assertSegment(uniqueId.getSegments().get(2), "t2", "v2");
			assertSegment(uniqueId.getSegments().get(3), "t3", "v3");
		}

	}

	@Nested
	class ParsingAndFormatting {

		private final String uniqueIdString = "[engine:junit5]/[class:MyClass]/[method:myMethod]";

		@Test
		void ensureDefaultUniqueIdFormatIsUsedForParsing() {
			UniqueId parsedDirectly = UniqueId.parse(uniqueIdString);
			UniqueId parsedViaFormat = UniqueIdFormat.getDefault().parse(uniqueIdString);
			assertEquals(parsedViaFormat, parsedDirectly);
		}

		@Test
		void ensureDefaultUniqueIdFormatIsUsedForFormatting() {
			UniqueId parsedDirectly = UniqueId.parse("[engine:junit5]/[class:MyClass]/[method:myMethod]");
			assertEquals("[engine:junit5]/[class:MyClass]/[method:myMethod]", parsedDirectly.toString());
		}
	}

	@Nested
	class EqualsContract {

		@Test
		void sameEnginesAreEqual() {
			UniqueId id1 = UniqueId.root("engine", "junit5");
			UniqueId id2 = UniqueId.root("engine", "junit5");

			Assertions.assertTrue(id1.equals(id2));
			Assertions.assertTrue(id2.equals(id1));
			assertEquals(id1.hashCode(), id2.hashCode());
		}

		@Test
		void differentEnginesAreNotEqual() {
			UniqueId id1 = UniqueId.root("engine", "junit4");
			UniqueId id2 = UniqueId.root("engine", "junit5");

			Assertions.assertFalse(id1.equals(id2));
			Assertions.assertFalse(id2.equals(id1));
		}

		@Test
		void uniqueIdWithSameSegmentsAreEqual() {
			UniqueId id1 = UniqueId.root("engine", "junit5").append("t1", "v1").append("t2", "v2");
			UniqueId id2 = UniqueId.root("engine", "junit5").append("t1", "v1").append("t2", "v2");

			Assertions.assertTrue(id1.equals(id2));
			Assertions.assertTrue(id2.equals(id1));
			assertEquals(id1.hashCode(), id2.hashCode());
		}

		@Test
		void differentOrderOfSegmentsAreNotEqual() {
			UniqueId id1 = UniqueId.root("engine", "junit5").append("t2", "v2").append("t1", "v1");
			UniqueId id2 = UniqueId.root("engine", "junit5").append("t1", "v1").append("t2", "v2");

			Assertions.assertFalse(id1.equals(id2));
			Assertions.assertFalse(id2.equals(id1));
		}

		@Test
		void additionalSegmentMakesItNotEqual() {
			UniqueId id1 = UniqueId.root("engine", "junit5").append("t1", "v1");
			UniqueId id2 = id1.append("t2", "v2");

			Assertions.assertFalse(id1.equals(id2));
			Assertions.assertFalse(id2.equals(id1));
		}
	}

	private void assertSegment(Segment segment, String expectedType, String expectedValue) {
		assertEquals(expectedType, segment.getType(), "segment type");
		assertEquals(expectedValue, segment.getValue(), "segment value");
	}

}
