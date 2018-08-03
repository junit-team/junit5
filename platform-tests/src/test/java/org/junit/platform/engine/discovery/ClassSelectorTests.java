/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.discovery;

import org.junit.jupiter.api.Test;
import org.junit.platform.AbstractEqualsAndHashCodeTests;

/**
 * Unit tests for {@link ClassSelector}.
 *
 * @since 1.3
 * @see DiscoverySelectorsTests
 */
class ClassSelectorTests extends AbstractEqualsAndHashCodeTests {

	@Test
	void equalsAndHashCode() throws Exception {
		var selector1 = new ClassSelector("org.example.TestClass");
		var selector2 = new ClassSelector("org.example.TestClass");
		var selector3 = new ClassSelector("org.example.X");

		assertEqualsAndHashCode(selector1, selector2, selector3);
	}

}
