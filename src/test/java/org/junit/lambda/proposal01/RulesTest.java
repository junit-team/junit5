package org.junit.lambda.proposal01;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.rules.TemporaryFolder;

/**
 * Experiments with rules
 */
public class RulesTest extends JUnitTest {{
	
	TemporaryFolder tempFolder = aroundEach(new TemporaryFolder());

	test("creates new file", () -> {
		File file = tempFolder.newFile("foo.txt");
		assertTrue(file.isFile());
	});

	test("creates new folder", () -> {
		File folder = tempFolder.newFolder("bar");
		assertTrue(folder.isDirectory());
	});

}}
