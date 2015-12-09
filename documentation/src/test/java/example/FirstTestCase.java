/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package example;

//tag::complete[]
import static org.junit.gen5.api.Assertions.assertEquals;

import org.junit.gen5.api.Test;

class FirstTestCase {

	@Test
	void firstTest() {
		assertEquals(2, 1 + 1);
	}

}
// end::complete[]
