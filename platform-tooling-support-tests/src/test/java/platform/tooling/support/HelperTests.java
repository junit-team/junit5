/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package platform.tooling.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

class HelperTests {

	@Test
	void loadModuleDirectoryNames() {
		assertLinesMatch(List.of( //
			"junit-jupiter-api", //
			"junit-jupiter-engine", //
			"junit-jupiter-migrationsupport", //
			"junit-jupiter-params", //
			"junit-platform-commons", //
			"junit-platform-console", //
			"junit-platform-engine", //
			"junit-platform-launcher", //
			"junit-platform-runner", //
			"junit-platform-suite-api", //
			"junit-platform-surefire-provider", //
			"junit-vintage-engine"//
		), Helper.loadModuleDirectoryNames());
	}

	@Test
	void version() {
		assertNotNull(Helper.version("junit-jupiter"));
		assertNotNull(Helper.version("junit-vintage"));
		assertNotNull(Helper.version("junit-platform"));

		Error error = assertThrows(AssertionError.class, () -> Helper.version("foo"));
		assertEquals("module name is unknown: foo", error.getMessage());
	}

}
