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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

class PackagePrivateParent {

	static List<String> bridgeMethodSequence = new ArrayList<>();

	@BeforeAll
	static void beforeAll() {
		bridgeMethodSequence.clear();
		bridgeMethodSequence.add("static parent.beforeAll()");
	}

	@AfterAll
	static void afterAll() {
		bridgeMethodSequence.add("static parent.afterAll()");
		assertAll("bridge method sequence test",
			() -> assertEquals("static parent.beforeAll()", bridgeMethodSequence.get(0)),
			() -> assertEquals("parent.beforeEach()", bridgeMethodSequence.get(1)),
			() -> assertEquals("child.anotherBeforeEach()", bridgeMethodSequence.get(2)),
			() -> assertEquals("child.test()", bridgeMethodSequence.get(3)),
			() -> assertEquals("child.anotherAfterEach()", bridgeMethodSequence.get(4)),
			() -> assertEquals("parent.afterEach()", bridgeMethodSequence.get(5)),
			() -> assertEquals("static parent.afterAll()", bridgeMethodSequence.get(6)));
	}

	@BeforeEach
	public void beforeEach() {
		bridgeMethodSequence.add("parent.beforeEach()");
	}

	@AfterEach
	public void afterEach() {
		bridgeMethodSequence.add("parent.afterEach()");
	}
}
