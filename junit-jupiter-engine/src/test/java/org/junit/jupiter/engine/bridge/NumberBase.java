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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(NumberResolver.class)
class NumberBase<N extends Number> {

	void test(N number) {
		BridgeTests.sequence.add("test(N)");
		Assertions.assertNotNull(number);
		Assertions.assertEquals(123, number.intValue());
	}
}
