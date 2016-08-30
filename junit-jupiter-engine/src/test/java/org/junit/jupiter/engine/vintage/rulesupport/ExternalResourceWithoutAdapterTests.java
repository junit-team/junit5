/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.vintage.rulesupport;

import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.rules.TemporaryFolder;

public class ExternalResourceWithoutAdapterTests {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@BeforeEach
	void setup() {
		try {
			folder.newFile("temp.txt");
		}
		catch (Exception exception) {
			System.out.println("exception = " + exception.getMessage());
			assert exception.getMessage().equals("the temporary folder has not yet been created");
		}
	}

	@Test
	void checkTemporaryFolder() {
		// only needed to invoke testing at all
	}

}
