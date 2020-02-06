/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.engine.UniqueId.Segment;

/**
 * Unit tests for {@link UniqueId}.
 *
 * @since 1.0
 * @see org.junit.jupiter.engine.execution.UniqueIdParsingForArrayParameterIntegrationTests
 */
class UniqueIdTests {

	private static final String ENGINE_ID = "junit-jupiter";

	@Nested
	class Creation {

		@Test
		void uniqueIdCanBeCreatedFromEngineId() {
			UniqueId uniqueId = UniqueId.forEngine(ENGINE_ID);

			assertEquals("[engine:junit-jupiter]", uniqueId.toString());
			assertSegment(uniqueId.getSegments().get(0), "engine", "junit-jupiter");
		}

		@Test
		void retrievingOptionalEngineId() {
			UniqueId uniqueIdWithEngine = UniqueId.forEngine(ENGINE_ID);
			assertThat(uniqueIdWithEngine.getEngineId()).contains("junit-jupiter");

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

			assertThat(uniqueId.getRoot()).contains(new Segment("aType", "aValue"));
		}

		@Test
		void appendingOneSegment() {
			UniqueId engineId = UniqueId.root("engine", ENGINE_ID);
			UniqueId classId = engineId.append("class", "org.junit.MyClass");

			assertThat(classId.getSegments()).hasSize(2);
			assertSegment(classId.getSegments().get(0), "engine", ENGINE_ID);
			assertSegment(classId.getSegments().get(1), "class", "org.junit.MyClass");
		}

		@Test
		void appendingSegmentLeavesOriginalUnchanged() {
			UniqueId uniqueId = UniqueId.root("engine", ENGINE_ID);
			uniqueId.append("class", "org.junit.MyClass");

			assertThat(uniqueId.getSegments()).hasSize(1);
			assertSegment(uniqueId.getSegments().get(0), "engine", ENGINE_ID);
		}

		@Test
		void appendingSeveralSegments() {
			UniqueId engineId = UniqueId.root("engine", ENGINE_ID);
			UniqueId uniqueId = engineId.append("t1", "v1").append("t2", "v2").append("t3", "v3");

			assertThat(uniqueId.getSegments()).hasSize(4);
			assertSegment(uniqueId.getSegments().get(0), "engine", ENGINE_ID);
			assertSegment(uniqueId.getSegments().get(1), "t1", "v1");
			assertSegment(uniqueId.getSegments().get(2), "t2", "v2");
			assertSegment(uniqueId.getSegments().get(3), "t3", "v3");
		}

		@Test
		void appendingSegmentInstance() {
			UniqueId uniqueId = UniqueId.forEngine(ENGINE_ID).append("t1", "v1");

			uniqueId = uniqueId.append(new Segment("t2", "v2"));

			assertThat(uniqueId.getSegments()).hasSize(3);
			assertSegment(uniqueId.getSegments().get(0), "engine", ENGINE_ID);
			assertSegment(uniqueId.getSegments().get(1), "t1", "v1");
			assertSegment(uniqueId.getSegments().get(2), "t2", "v2");
		}

		@Test
		void appendingNullIsNotAllowed() {
			UniqueId uniqueId = UniqueId.forEngine(ENGINE_ID);

			assertThrows(PreconditionViolationException.class, () -> uniqueId.append(null));
			assertThrows(PreconditionViolationException.class, () -> uniqueId.append(null, "foo"));
			assertThrows(PreconditionViolationException.class, () -> uniqueId.append("foo", null));
		}

	}

	@Nested
	class ParsingAndFormatting {

		private final String uniqueIdString = "[engine:junit-jupiter]/[class:MyClass]/[method:myMethod]";

		@Test
		void ensureDefaultUniqueIdFormatIsUsedForParsing() {
			UniqueId parsedDirectly = UniqueId.parse(uniqueIdString);
			UniqueId parsedViaFormat = UniqueIdFormat.getDefault().parse(uniqueIdString);
			assertEquals(parsedViaFormat, parsedDirectly);
		}

		@Test
		void ensureDefaultUniqueIdFormatIsUsedForFormatting() {
			UniqueId parsedDirectly = UniqueId.parse("[engine:junit-jupiter]/[class:MyClass]/[method:myMethod]");
			assertEquals("[engine:junit-jupiter]/[class:MyClass]/[method:myMethod]", parsedDirectly.toString());
		}

		@Test
		void ensureDefaultUniqueIdFormatDecodingEncodesSegmentParts() {
			UniqueId.Segment segment = UniqueId.parse("[%5B+%25+%5D):(%3A+%2B+%2F]").getSegments().get(0);
			assertEquals("[ % ])", segment.getType());
			assertEquals("(: + /", segment.getValue());
		}

		@Test
		void ensureDefaultUniqueIdFormatCanHandleAllCharacters() {
			for (char c = 0; c < Character.MAX_VALUE; c++) {
				String value = "foo " + String.valueOf(c) + " bar";
				UniqueId uniqueId = UniqueId.parse(UniqueId.root("type", value).toString());
				Segment segment = uniqueId.getSegments().get(0);
				assertEquals(value, segment.getValue());
			}
		}

		@ParameterizedTest
		@ValueSource(strings = { "[a:b]", "[a:b]/[a:b]", "[a$b:b()]", "[a:b(%5BI)]", "[%5B%5D:%3A%2F]" })
		void ensureDefaultToStringAndParsingIsIdempotent(String expected) {
			assertEquals(expected, UniqueId.parse(expected).toString());
		}
	}

	@Nested
	class EqualsContract {

		@Test
		void sameEnginesAreEqual() {
			UniqueId id1 = UniqueId.root("engine", "junit-jupiter");
			UniqueId id2 = UniqueId.root("engine", "junit-jupiter");

			assertTrue(id1.equals(id2));
			assertTrue(id2.equals(id1));
			assertEquals(id1.hashCode(), id2.hashCode());
		}

		@Test
		void differentEnginesAreNotEqual() {
			UniqueId id1 = UniqueId.root("engine", "junit-vintage");
			UniqueId id2 = UniqueId.root("engine", "junit-jupiter");

			assertFalse(id1.equals(id2));
			assertFalse(id2.equals(id1));
		}

		@Test
		void uniqueIdWithSameSegmentsAreEqual() {
			UniqueId id1 = UniqueId.root("engine", "junit-jupiter").append("t1", "v1").append("t2", "v2");
			UniqueId id2 = UniqueId.root("engine", "junit-jupiter").append("t1", "v1").append("t2", "v2");

			assertTrue(id1.equals(id2));
			assertTrue(id2.equals(id1));
			assertEquals(id1.hashCode(), id2.hashCode());
		}

		@Test
		void differentOrderOfSegmentsAreNotEqual() {
			UniqueId id1 = UniqueId.root("engine", "junit-jupiter").append("t2", "v2").append("t1", "v1");
			UniqueId id2 = UniqueId.root("engine", "junit-jupiter").append("t1", "v1").append("t2", "v2");

			assertFalse(id1.equals(id2));
			assertFalse(id2.equals(id1));
		}

		@Test
		void additionalSegmentMakesItNotEqual() {
			UniqueId id1 = UniqueId.root("engine", "junit-jupiter").append("t1", "v1");
			UniqueId id2 = id1.append("t2", "v2");

			assertFalse(id1.equals(id2));
			assertFalse(id2.equals(id1));
		}
	}

	@Nested
	class Prefixing {

		@Test
		void nullIsNotAPrefix() {
			UniqueId id = UniqueId.forEngine(ENGINE_ID);

			assertThrows(PreconditionViolationException.class, () -> id.hasPrefix(null));
		}

		@Test
		void uniqueIdIsPrefixForItself() {
			UniqueId id = UniqueId.forEngine(ENGINE_ID).append("t1", "v1").append("t2", "v2");

			assertTrue(id.hasPrefix(id));
		}

		@Test
		void uniqueIdIsPrefixForUniqueIdWithAdditionalSegments() {
			UniqueId id1 = UniqueId.forEngine(ENGINE_ID);
			UniqueId id2 = id1.append("t1", "v1");
			UniqueId id3 = id2.append("t2", "v2");

			assertFalse(id1.hasPrefix(id2));
			assertFalse(id1.hasPrefix(id3));
			assertTrue(id2.hasPrefix(id1));
			assertFalse(id2.hasPrefix(id3));
			assertTrue(id3.hasPrefix(id1));
			assertTrue(id3.hasPrefix(id2));
		}

		@Test
		void completelyUnrelatedUniqueIdsAreNotPrefixesForEachOther() {
			UniqueId id1 = UniqueId.forEngine("foo");
			UniqueId id2 = UniqueId.forEngine("bar");

			assertFalse(id1.hasPrefix(id2));
			assertFalse(id2.hasPrefix(id1));
		}

	}

	@Nested
	class LastSegment {

		@Test
		void returnsLastSegment() {
			UniqueId uniqueId = UniqueId.forEngine("foo");
			assertSame(uniqueId.getSegments().get(0), uniqueId.getLastSegment());

			uniqueId = UniqueId.forEngine("foo").append("type", "bar");
			assertSame(uniqueId.getSegments().get(1), uniqueId.getLastSegment());
		}

		@Test
		void removesLastSegment() {
			UniqueId uniqueId = UniqueId.forEngine("foo");
			assertThrows(PreconditionViolationException.class, uniqueId::removeLastSegment);

			UniqueId newUniqueId = uniqueId.append("type", "bar").removeLastSegment();
			assertEquals(uniqueId, newUniqueId);
		}

	}

	private void assertSegment(Segment segment, String expectedType, String expectedValue) {
		assertEquals(expectedType, segment.getType(), "segment type");
		assertEquals(expectedValue, segment.getValue(), "segment value");
	}

}
