/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.subpackage;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @since 5.9
 */
public class SuperClassWithPackagePrivateLifecycleMethodInDifferentPackageTestCase {

	@BeforeEach
	void beforeEach() {
		fail();
	}

	@Test
	void test() {
	}

}
