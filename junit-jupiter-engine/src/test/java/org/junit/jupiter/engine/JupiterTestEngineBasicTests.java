/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * Basic assertions regarding {@link org.junit.platform.engine.TestEngine}
 * functionality in JUnit Jupiter.
 *
 * @since 5.0
 */
class JupiterTestEngineBasicTests {

	private final JupiterTestEngine jupiter = new JupiterTestEngine();

	@Test
	void id() {
		assertEquals("junit-jupiter", jupiter.getId());
	}

	@Test
	void groupId() {
		assertEquals("org.junit.jupiter", jupiter.getGroupId().get());
	}

	@Test
	void artifactId() {
		assertEquals("junit-jupiter-engine", jupiter.getArtifactId().get());
	}

	@Test
	void version() {
		assertEquals("DEVELOPMENT", jupiter.getVersion().get());
	}

}
