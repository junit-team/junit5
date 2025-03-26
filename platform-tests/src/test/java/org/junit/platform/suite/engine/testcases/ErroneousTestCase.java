/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.engine.testcases;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ErroneousTestCase {

	@SuppressWarnings({ "JUnitMalformedDeclaration", "unused" })
	@BeforeAll
	void nonStaticLifecycleMethod() {
		fail("should not be called");
	}

	@Test
	void name() {
		fail("should not be called");
	}
}
