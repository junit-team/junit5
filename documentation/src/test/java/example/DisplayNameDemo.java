/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package example;

// tag::user_guide[]
import org.junit.gen5.api.DisplayName;
import org.junit.gen5.api.Test;

@DisplayName("A special test case")
class DisplayNameDemo {

	@Test
	@DisplayName("A nice name, isn't it?")
	void testWithANiceName() {
	}
}
// end::user_guide[]
