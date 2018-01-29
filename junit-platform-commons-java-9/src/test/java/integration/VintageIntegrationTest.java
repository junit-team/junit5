/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package integration;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Integration tests for {@code ModuleUtils} using JUnit Vintage.
 *
 * <p>The integration tests are meant to be executed on the module-path by
 * running JUnit Platform {@link org.junit.platform.console.ConsoleLauncher}
 * with the {@code --scan-modules} option.
 *
 * @since 1.1
 */
public class VintageIntegrationTest {

	@Test
	public void successfulTest() {
		assertEquals(3, 1 + 2);
	}

}
