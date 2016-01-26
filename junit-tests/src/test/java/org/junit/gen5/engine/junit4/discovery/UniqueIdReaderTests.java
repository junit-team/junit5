/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.discovery;

import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.runner.Description.createSuiteDescription;

import java.io.Serializable;

import org.junit.gen5.api.Test;
import org.junit.runner.Description;

class UniqueIdReaderTests {

	@Test
	void readsUniqueId() {
		Description description = createSuiteDescription("displayName", "uniqueId");

		Serializable uniqueId = new UniqueIdReader().apply(description);

		assertEquals("uniqueId", uniqueId);
	}

	@Test
	void returnsDisplayNameWhenUniqueIdCannotBeRead() {
		Description description = createSuiteDescription("displayName", "uniqueId");

		Serializable uniqueId = new UniqueIdReader("wrongFieldName").apply(description);

		assertEquals("displayName", uniqueId);
	}

}
