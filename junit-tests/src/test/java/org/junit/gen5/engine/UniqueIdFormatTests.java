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

import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.junit.gen5.api.Assertions.expectThrows;

import org.junit.gen5.api.Nested;
import org.junit.gen5.api.Test;
import org.junit.gen5.commons.JUnitException;
import org.junit.gen5.engine.UniqueId.Segment;

/**
 * @since 5.0
 */
class UniqueIdFormatTests {

	static final String ENGINE_ID = "junit5";

	@Nested
	class Formatting {

		private final UniqueIdFormat format = UniqueIdFormat.getDefault();

		@Test
		void uniqueIdOnly() {
			UniqueId uniqueId = UniqueId.root("engine", ENGINE_ID);
			assertEquals("[engine:junit5]", uniqueId.getUniqueString());
			assertEquals(format.format(uniqueId), uniqueId.getUniqueString());
		}

		@Test
		void withTwoSegments() {
			UniqueId engineId = UniqueId.root("engine", ENGINE_ID);
			UniqueId classId = engineId.append("class", "org.junit.MyClass");
			assertEquals("[engine:junit5]/[class:org.junit.MyClass]", classId.getUniqueString());
			assertEquals(format.format(classId), classId.getUniqueString());
		}

		@Test
		void withManySegments() {
			UniqueId engineId = UniqueId.root("engine", ENGINE_ID);
			UniqueId uniqueId = engineId.append("t1", "v1").append("t2", "v2").append("t3", "v3");
			assertEquals("[engine:junit5]/[t1:v1]/[t2:v2]/[t3:v3]", uniqueId.getUniqueString());
			assertEquals(format.format(uniqueId), uniqueId.getUniqueString());
		}

	}

	@Nested
	class ParsingWithDefaultFormat implements ParsingTest {

		private final UniqueIdFormat format = UniqueIdFormat.getDefault();

		@Override
		public UniqueIdFormat getFormat() {
			return this.format;
		}

		@Override
		public String getEngineUid() {
			return "[engine:junit5]";
		}

		@Override
		public String getMethodUid() {
			return "[engine:junit5]/[class:MyClass]/[method:myMethod]";
		}

	}

	@Nested
	class ParsingWithCustomFormat implements ParsingTest {

		private final UniqueIdFormat format = new UniqueIdFormat('{', '=', '}', ',');

		@Override
		public UniqueIdFormat getFormat() {
			return this.format;
		}

		@Override
		public String getEngineUid() {
			return "{engine=junit5}";
		}

		@Override
		public String getMethodUid() {
			return "{engine=junit5},{class=MyClass},{method=myMethod}";
		}

	}

	// -------------------------------------------------------------------------

	private static void assertSegment(Segment segment, String expectedType, String expectedValue) {
		assertEquals(expectedType, segment.getType(), "segment type");
		assertEquals(expectedValue, segment.getValue(), "segment value");
	}

	interface ParsingTest {

		UniqueIdFormat getFormat();

		String getEngineUid();

		String getMethodUid();

		@Test
		default void parseMalformedUid() {
			Throwable throwable = expectThrows(JUnitException.class, () -> getFormat().parse("malformed UID"));
			assertTrue(throwable.getMessage().contains("malformed UID"));
		}

		@Test
		default void parseEngineUid() {
			UniqueId parsedId = getFormat().parse(getEngineUid());
			assertSegment(parsedId.getSegments().get(0), "engine", "junit5");
			assertEquals(getEngineUid(), getFormat().format(parsedId));
			assertEquals(getEngineUid(), parsedId.getUniqueString());
		}

		@Test
		default void parseMethodUid() {
			UniqueId parsedId = getFormat().parse(getMethodUid());
			assertSegment(parsedId.getSegments().get(0), "engine", "junit5");
			assertSegment(parsedId.getSegments().get(1), "class", "MyClass");
			assertSegment(parsedId.getSegments().get(2), "method", "myMethod");
			assertEquals(getMethodUid(), getFormat().format(parsedId));
			assertEquals(getMethodUid(), parsedId.getUniqueString());
		}

	}

}
