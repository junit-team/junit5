/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.discovery;

import org.junit.jupiter.api.Test;
import org.junit.platform.AbstractEqualsAndHashCodeTests;

/**
 * Unit tests for {@link ModuleSelector}.
 *
 * @since 1.3
 * @see DiscoverySelectorsTests
 */
class ModuleSelectorTests extends AbstractEqualsAndHashCodeTests {

	@Test
	void equalsAndHashCode() {
		var selector1 = new ModuleSelector("foo-api");
		var selector2 = new ModuleSelector("foo-api");
		var selector3 = new ModuleSelector("bar-impl");

		assertEqualsAndHashCode(selector1, selector2, selector3);
	}

}
