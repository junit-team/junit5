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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

/**
 * @since 1.0
 */
class FilePositionTests{

	@Test
	void equalsTest() {
		//test for a return of true when the equals method is passed itself
		FilePosition file = new FilePosition(0, 0);
		assertEquals(file, file);
	}

	@Test
	void equalsNullTest() {
		//test for a return of true when the equals method is passed null
		FilePosition file = new FilePosition(0, 0);
		assertNotNull(file);
	}

}
