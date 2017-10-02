/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.JigsawUtils;

/**
 * Integration tests for {@link JigsawUtils}.
 *
 * <p>The integration tests are meant to be executed on the module-path by
 * running JUnit Platform {@link org.junit.platform.console.ConsoleLauncher}
 * with the {@code --scan-module-path} option.
 *
 * @since 1.1
 */
class IntegrationTests {

	@Test
	void version() {
		assertEquals("9", JigsawUtils.VERSION);
	}

	@Test
	void packageName() {
		assertEquals("integration", getClass().getPackageName());
	}

	@Test
	void moduleIsNamed() {
		assertTrue(getClass().getModule().isNamed());
	}

}
