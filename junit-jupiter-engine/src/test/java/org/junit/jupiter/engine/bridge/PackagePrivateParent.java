/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.bridge;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

class PackagePrivateParent {

	@BeforeAll
	static void beforeAll() {
		BridgeTests.sequence.add("static parent.beforeAll()");
	}

	@AfterAll
	static void afterAll() {
		BridgeTests.sequence.add("static parent.afterAll()");
	}

	@BeforeEach
	public void beforeEach() {
		BridgeTests.sequence.add("parent.beforeEach()");
	}

	@AfterEach
	public void afterEach() {
		BridgeTests.sequence.add("parent.afterEach()");
	}
}
