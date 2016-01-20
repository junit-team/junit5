package org.junit.gen5.engine;

import org.junit.gen5.api.Assertions;
import org.junit.gen5.api.Test;

class UniqueIdTests {

	static final String ENGINE_ID = "junit5";

	@Test
	void uniqueIdMustBeCreatedWithEngineId() {
		UniqueId uniqueId = new UniqueId(ENGINE_ID);

		Assertions.assertEquals("[engine:junit5]", uniqueId.getUniqueString());

		UniqueId.Segment segment = uniqueId.getSegments().get(0);
		Assertions.assertEquals(UniqueId.TYPE_ENGINE, segment.getType());
		Assertions.assertEquals("junit5", segment.getValue());

	}
}
