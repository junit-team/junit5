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

import org.junit.gen5.api.Assertions;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.UniqueId.Segment;

class UniqueIdTests {

	static final String ENGINE_ID = "junit5";

	@Test
	void uniqueIdMustBeCreatedWithEngineId() {
		UniqueId uniqueId = new UniqueId(ENGINE_ID);

		Assertions.assertEquals("[engine:junit5]", uniqueId.getUniqueString());

		assertSegment(uniqueId.getSegments().get(0), UniqueId.TYPE_ENGINE, "junit5");
	}

	private void assertSegment(Segment segment, String expectedType, String expectedValue) {
		Assertions.assertEquals(expectedType, segment.getType(), "segment type");
		Assertions.assertEquals(expectedValue, segment.getValue(), "segment value");
	}

	@Test
	void appendingOneSegment() {
		UniqueId engineId = new UniqueId(ENGINE_ID);
		UniqueId classId = engineId.append("class", "org.junit.MyClass");

		Assertions.assertEquals("[engine:junit5]/[class:org.junit.MyClass]", classId.getUniqueString());

		Assertions.assertEquals(2, classId.getSegments().size());
		assertSegment(classId.getSegments().get(1), "class", "org.junit.MyClass");
	}

	@Test
	void appendingSeveralSegments() {
		UniqueId engineId = new UniqueId(ENGINE_ID);
		UniqueId uniqueId = engineId.append("t1", "v1").append("t2", "v2").append("t3", "v3");

		Assertions.assertEquals("[engine:junit5]/[t1:v1]/[t2:v2]/[t3:v3]", uniqueId.getUniqueString());
		Assertions.assertEquals(4, uniqueId.getSegments().size());
	}
}
