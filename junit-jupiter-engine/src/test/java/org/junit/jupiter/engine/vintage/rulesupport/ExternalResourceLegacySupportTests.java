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

import java.io.File;
import java.io.IOException;

import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.rules.TemporaryFolder;

@ExtendWith(ExternalResourceLegacySupport.class)
public class ExternalResourceLegacySupportTests {

	private File file;

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();

	@BeforeEach
	void setup() throws IOException {
		this.file = folder.newFile("temp.txt");
	}

	@Test
	void checkTemporaryFolder() {
		System.out.println("file of TemporaryFolder: " + this.file);
		assert file.canRead();
	}

}
