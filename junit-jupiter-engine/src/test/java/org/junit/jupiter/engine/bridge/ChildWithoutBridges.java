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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

// modifier "public" is *not* present for not creating bridge methods by the compiler
class ChildWithoutBridges extends PackagePrivateParent {

	@BeforeEach
	public void anotherBeforeEach() {
		bridgeMethodSequence.add("child.anotherBeforeEach()");
	}

	@Test
	public void test() {
		bridgeMethodSequence.add("child.test()");
	}

	@AfterEach
	public void anotherAfterEach() {
		bridgeMethodSequence.add("child.anotherAfterEach()");
	}
}
