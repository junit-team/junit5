/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.bridge;

import org.junit.jupiter.api.Test;

interface NumberTestGroup {

	class ByteTests extends NumberBase<Byte> {

		@Test
		@Override
		void test(Byte value) {
			BridgeTests.sequence.add("test(Byte) BEGIN");
			super.test(value);
			BridgeTests.sequence.add("test(Byte) END.");
		}
	}

	class ShortTests extends NumberBase<Short> {

		@Test
		@Override
		void test(Short value) {
			BridgeTests.sequence.add("test(Short) BEGIN");
			super.test(value);
			BridgeTests.sequence.add("test(Short) END.");
		}
	}
}
