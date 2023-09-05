/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package example.vintage;

import static java.util.Collections.singletonList;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.vintage.reporting.TestReporting;

public class VintageTestReportingDemo {

	@Rule
	public TemporaryFolder temporaryFolder = new TemporaryFolder();

	@Rule
	public TestReporting testReporting = new TestReporting();

	@Test
	public void reportFiles() throws Exception {
		Path existingFile = Files.write(temporaryFolder.getRoot().toPath().resolve("test.txt"), singletonList("Test"));
		testReporting.publishFile(existingFile);
	}
}
