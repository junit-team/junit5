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

/**
 * @since 5.0
 */
interface NumberTestGroup {

	class ByteTestCase extends AbstractNumberTests<Byte> {

		@Test
		@Override
		void test(Byte value) {
			BridgeMethodTests.sequence.add("test(Byte) BEGIN");
			super.test(value);
			BridgeMethodTests.sequence.add("test(Byte) END.");
		}
	}

	class ShortTestCase extends AbstractNumberTests<Short> {

		@Test
		@Override
		void test(Short value) {
			BridgeMethodTests.sequence.add("test(Short) BEGIN");
			super.test(value);
			BridgeMethodTests.sequence.add("test(Short) END.");
		}
	}

}
