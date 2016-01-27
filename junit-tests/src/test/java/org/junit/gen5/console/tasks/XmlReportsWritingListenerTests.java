/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console.tasks;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.junit.gen5.api.Assertions.fail;
import static org.junit.gen5.api.Assumptions.assumeFalse;
import static org.junit.gen5.engine.discovery.UniqueIdSelector.forUniqueId;
import static org.junit.gen5.launcher.main.LauncherFactoryForTestingPurposesOnly.createLauncher;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeAll;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.TestInfo;
import org.junit.gen5.engine.support.hierarchical.DummyTestDescriptor;
import org.junit.gen5.engine.support.hierarchical.DummyTestEngine;
import org.junit.gen5.launcher.Launcher;
import org.opentest4j.AssertionFailedError;
import org.xml.sax.SAXException;

class XmlReportsWritingListenerTests {

	private static Validator schemaValidator;
	private Path tempDirectory;

	@BeforeAll
	static void initializeSchemaValidator() throws Exception {
		URL schemaFile = XmlReportsWritingListener.class.getResource("/jenkins-junit.xsd");
		SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		schemaValidator = schemaFactory.newSchema(schemaFile).newValidator();
	}

	@BeforeEach
	void createTempDirectory(TestInfo testInfo) throws Exception {
		tempDirectory = Files.createTempDirectory(testInfo.getName());
	}

	@AfterEach
	void deleteTempDirectory() throws Exception {
		Files.walkFileTree(tempDirectory, new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				return deleteAndContinue(file);
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				return deleteAndContinue(dir);
			}

			private FileVisitResult deleteAndContinue(Path path) throws IOException {
				Files.delete(path);
				return FileVisitResult.CONTINUE;
			}
		});
	}

	@Test
	void writesFileForSingleSucceedingTest() throws Exception {
		DummyTestEngine engine = new DummyTestEngine("dummy");
		engine.addTest("succeedingTest", () -> {
		}).setDisplayName("display<>Name");

		executeTests(engine);

		String content = readValidXmlFile("TEST-dummy.xml");
		//@formatter:off
		assertThat(content)
			.containsSequence(
				"<testsuite name=\"dummy\" tests=\"1\" skipped=\"0\" failures=\"0\" errors=\"0\"",
				"<!--Unique ID: dummy-->",
				"<testcase name=\"display&lt;&gt;Name\" classname=\"dummy\"",
				"<!--Unique ID: dummy:succeedingTest-->",
				"</testcase>",
				"</testsuite>")
			.doesNotContain("<skipped")
			.doesNotContain("<failure")
			.doesNotContain("<error");
		//@formatter:on
	}

	@Test
	void writesFileForSingleFailingTest() throws Exception {
		DummyTestEngine engine = new DummyTestEngine("dummy");
		engine.addTest("failingTest", () -> fail("expected to <b>fail</b>"));

		executeTests(engine);

		String content = readValidXmlFile("TEST-dummy.xml");
		//@formatter:off
		assertThat(content)
			.containsSequence(
				"<testsuite name=\"dummy\" tests=\"1\" skipped=\"0\" failures=\"1\" errors=\"0\"",
				"<testcase name=\"failingTest\"",
				"<failure message=\"expected to &lt;b&gt;fail&lt;/b&gt;\" type=\"" + AssertionFailedError.class.getName() + "\">",
				"AssertionFailedError: expected to &lt;b&gt;fail&lt;/b&gt;",
				"\tat",
				"</failure>",
				"</testcase>",
				"</testsuite>")
			.doesNotContain("<skipped")
			.doesNotContain("<error");
		//@formatter:on
	}

	@Test
	void writesFileForSingleSkippedTest() throws Exception {
		DummyTestEngine engine = new DummyTestEngine("dummy");
		DummyTestDescriptor testDescriptor = engine.addTest("skippedTest", () -> fail("never called"));
		testDescriptor.markSkipped("should be skipped");

		executeTests(engine);

		String content = readValidXmlFile("TEST-dummy.xml");
		//@formatter:off
		assertThat(content)
			.containsSequence(
				"<testsuite name=\"dummy\" tests=\"1\" skipped=\"1\" failures=\"0\" errors=\"0\"",
				"<testcase name=\"skippedTest\"",
				"<skipped>should be skipped</skipped>",
				"</testcase>",
				"</testsuite>")
			.doesNotContain("<failure")
			.doesNotContain("<error");
		//@formatter:off
	}

	@Test
	void writesFileForSingleAbortedTest() throws Exception {
		DummyTestEngine engine = new DummyTestEngine("dummy");
		engine.addTest("abortedTest", () -> assumeFalse(true, "deliberately aborted"));

		executeTests(engine);

		String content = readValidXmlFile("TEST-dummy.xml");
		//@formatter:off
		assertThat(content)
			.containsSequence(
				"<testsuite name=\"dummy\" tests=\"1\" skipped=\"1\" failures=\"0\" errors=\"0\"",
				"<testcase name=\"abortedTest\"",
				"<skipped>",
				"TestAbortedException: ",
				"deliberately aborted",
				"at ",
				"</skipped>",
				"</testcase>",
				"</testsuite>")
			.doesNotContain("<failure")
			.doesNotContain("<error");
		//@formatter:off
	}

	private void executeTests(DummyTestEngine engine) {
		PrintWriter out = new PrintWriter(new StringWriter());
		XmlReportsWritingListener reportListener = new XmlReportsWritingListener(tempDirectory.toString(), out);
		Launcher launcher = createLauncher(engine);
		launcher.registerTestExecutionListeners(reportListener);
		launcher.execute(request().select(forUniqueId(engine.getId())).build());
	}

	private String readValidXmlFile(String filename) throws Exception {
		Path xmlFile = tempDirectory.resolve(filename);
		assertTrue(Files.exists(xmlFile), () -> "File does not exist: " + xmlFile);
		String content = new String(Files.readAllBytes(xmlFile), UTF_8);
		assertValidAccordingToJenkinsSchema(content);
		return content;
	}

	private static void assertValidAccordingToJenkinsSchema(String content) throws Exception {
		try {
			schemaValidator.validate(new StreamSource(new StringReader(content)));
		}
		catch (SAXException e) {
			throw new AssertionFailedError("Invalid XML document: " + content, e);
		}
	}
}
