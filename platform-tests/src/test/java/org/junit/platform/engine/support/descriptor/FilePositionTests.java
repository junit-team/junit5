/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.support.descriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Created by Dustin on 4/28/2017.
 */
class FilePositionTests {

	@Test
	void Test_FilePosition_Start_getLine() {
		FilePosition fp = new FilePosition(0, 0);
		assertEquals(0, fp.getLine(), "check the getLine() method");
	}

	@Test
	void Test_FilePosition_Start_getColumn() {
		FilePosition fp = new FilePosition(0, 0);
		assertEquals(0, fp.getColumn(), "check the getCoumn() method");
	}

	@Test
	void Test_FilePosition_Equals_Itself() {
		FilePosition fp = new FilePosition(0, 0);
		assertTrue(fp.equals(fp), "Same FilePosition Object");
	}

	@Test
	void Test_FilePosition_Equals() {
		FilePosition fp = new FilePosition(1, 1);
		FilePosition fp2 = new FilePosition(1, 1);
		assertTrue(fp.equals(fp2), "Different FilePositions but same line and column numbers");
	}

	@Test
	void Test_FilePosition_Equals_FalseCase() {
		FilePosition fp = new FilePosition(1, 1);
		FilePosition fp2 = new FilePosition(0, 1);
		FilePosition fp3 = new FilePosition(1, 0);
		assertFalse(fp.equals(fp2));
		assertFalse(fp.equals(fp3));
		assertFalse(fp2.equals(fp3));
	}
}
