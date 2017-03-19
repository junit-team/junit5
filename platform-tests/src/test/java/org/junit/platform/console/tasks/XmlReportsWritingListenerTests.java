/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console.tasks;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.platform.console.tasks.XmlReportAssertions.ensureValidAccordingToJenkinsSchema;
import static org.junit.platform.engine.TestExecutionResult.successful;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;
import static org.junit.platform.launcher.core.LauncherFactoryForTestingPurposesOnly.createLauncher;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.Year;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.extensions.TempDirectory;
import org.junit.jupiter.extensions.TempDirectory.Root;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.engine.support.hierarchical.DemoHierarchicalTestDescriptor;
import org.junit.platform.engine.support.hierarchical.DemoHierarchicalTestEngine;
import org.junit.platform.engine.test.TestDescriptorStub;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;
import org.opentest4j.AssertionFailedError;

/**
 * @since 1.0
 */
@ExtendWith(TempDirectory.class)
class XmlReportsWritingListenerTests {

	@Test
	void writesFileForSingleSucceedingTest(@Root Path tempDirectory) throws Exception {
		DemoHierarchicalTestEngine engine = new DemoHierarchicalTestEngine("dummy");
		engine.addTest("succeedingTest", "display<-->Name 😎", () -> {
		});

		executeTests(engine, tempDirectory);

		String content = readValidXmlFile(tempDirectory.resolve("TEST-dummy.xml"));
		//@formatter:off
		assertThat(content)
			.containsSequence(
				"<testsuite name=\"dummy\" tests=\"1\" skipped=\"0\" failures=\"0\" errors=\"0\"",

					"<testcase name=\"display&lt;--&gt;Name 😎\" classname=\"dummy\"",
						"<system-out>",
							"unique-id: [engine:dummy]/[test:succeedingTest]",
							"display-name: display<-->Name 😎",
						"</system-out>",
					"</testcase>",

					"<system-out>",
						"unique-id: [engine:dummy]",
						"display-name: dummy",
					"</system-out>",
				"</testsuite>")
			.doesNotContain("<skipped")
			.doesNotContain("<failure")
			.doesNotContain("<error");
		//@formatter:on
	}

	@Test
	void writesFileForSingleFailingTest(@Root Path tempDirectory) throws Exception {
		DemoHierarchicalTestEngine engine = new DemoHierarchicalTestEngine("dummy");
		engine.addTest("failingTest", () -> fail("expected to <b>fail</b>"));

		executeTests(engine, tempDirectory);

		String content = readValidXmlFile(tempDirectory.resolve("TEST-dummy.xml"));

		//@formatter:off
		assertThat(content)
			.containsSequence(
				"<testsuite name=\"dummy\" tests=\"1\" skipped=\"0\" failures=\"1\" errors=\"0\"",
				"<testcase name=\"failingTest\"",
				"<failure message=\"expected to &lt;b&gt;fail&lt;/b&gt;\" type=\"" + AssertionFailedError.class.getName() + "\">",
				"AssertionFailedError: expected to <b>fail</b>",
				"\tat",
				"</failure>",
				"</testcase>",
				"</testsuite>")
			.doesNotContain("<skipped")
			.doesNotContain("<error");
		//@formatter:on
	}

	@Test
	void writesFileForSingleErroneousTest(@Root Path tempDirectory) throws Exception {
		DemoHierarchicalTestEngine engine = new DemoHierarchicalTestEngine("dummy");
		engine.addTest("failingTest", () -> {
			throw new RuntimeException("error occurred");
		});

		executeTests(engine, tempDirectory);

		String content = readValidXmlFile(tempDirectory.resolve("TEST-dummy.xml"));

		//@formatter:off
		assertThat(content)
			.containsSequence(
				"<testsuite name=\"dummy\" tests=\"1\" skipped=\"0\" failures=\"0\" errors=\"1\"",
				"<testcase name=\"failingTest\"",
				"<error message=\"error occurred\" type=\"java.lang.RuntimeException\">",
				"RuntimeException: error occurred",
				"\tat ",
				"</error>",
				"</testcase>",
				"</testsuite>")
			.doesNotContain("<skipped")
			.doesNotContain("<failure");
		//@formatter:on
	}

	@Test
	void writesFileForSingleSkippedTest(@Root Path tempDirectory) throws Exception {
		DemoHierarchicalTestEngine engine = new DemoHierarchicalTestEngine("dummy");
		DemoHierarchicalTestDescriptor testDescriptor = engine.addTest("skippedTest", () -> fail("never called"));
		testDescriptor.markSkipped("should be skipped");

		executeTests(engine, tempDirectory);

		String content = readValidXmlFile(tempDirectory.resolve("TEST-dummy.xml"));

		//@formatter:off
		assertThat(content)
			.containsSequence(
				"<testsuite name=\"dummy\" tests=\"1\" skipped=\"1\" failures=\"0\" errors=\"0\"",
				"<testcase name=\"skippedTest\"",
				"<skipped>",
				"should be skipped",
				"</skipped>",
				"</testcase>",
				"</testsuite>")
			.doesNotContain("<failure")
			.doesNotContain("<error");
		//@formatter:on
	}

	@Test
	void writesFileForSingleAbortedTest(@Root Path tempDirectory) throws Exception {
		DemoHierarchicalTestEngine engine = new DemoHierarchicalTestEngine("dummy");
		engine.addTest("abortedTest", () -> assumeFalse(true, "deliberately aborted"));

		executeTests(engine, tempDirectory);

		String content = readValidXmlFile(tempDirectory.resolve("TEST-dummy.xml"));

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
		//@formatter:on
	}

	@Test
	void measuresTimesInSeconds(@Root Path tempDirectory) throws Exception {
		DemoHierarchicalTestEngine engine = new DemoHierarchicalTestEngine("dummy");
		engine.addTest("firstTest", () -> {
		});
		engine.addTest("secondTest", () -> {
		});

		executeTests(engine, tempDirectory, new IncrementingClock(0, Duration.ofMillis(333)));

		String content = readValidXmlFile(tempDirectory.resolve("TEST-dummy.xml"));

		//@formatter:off
		//               start        end
		// ----------- ---------- -----------
		// engine          0 (1)    1,665 (6)
		// firstTest     333 (2)      666 (3)
		// secondTest    999 (4)    1,332 (5)
		assertThat(content)
			.containsSequence(
				"<testsuite", "time=\"1.665\"",
				"<testcase name=\"firstTest\" classname=\"dummy\" time=\"0.333\"",
				"<testcase name=\"secondTest\" classname=\"dummy\" time=\"0.333\"");
		//@formatter:on
	}

	@Test
	void testWithImmeasurableTimeIsOutputCorrectly(@Root Path tempDirectory) throws Exception {
		DemoHierarchicalTestEngine engine = new DemoHierarchicalTestEngine("dummy");
		engine.addTest("test", () -> {
		});

		executeTests(engine, tempDirectory, Clock.fixed(Instant.EPOCH, ZoneId.systemDefault()));

		String content = readValidXmlFile(tempDirectory.resolve("TEST-dummy.xml"));

		//@formatter:off
		assertThat(content)
			.containsSequence(
				"<testsuite",
				"<testcase name=\"test\" classname=\"dummy\" time=\"0\"");
		//@formatter:on
	}

	@Test
	void writesFileForSkippedContainer(@Root Path tempDirectory) throws Exception {
		DemoHierarchicalTestEngine engine = new DemoHierarchicalTestEngine("dummy");
		engine.addTest("test", () -> fail("never called"));
		engine.getEngineDescriptor().markSkipped("should be skipped");

		executeTests(engine, tempDirectory);

		String content = readValidXmlFile(tempDirectory.resolve("TEST-dummy.xml"));

		//@formatter:off
		assertThat(content)
			.containsSequence(
				"<testsuite name=\"dummy\" tests=\"1\" skipped=\"1\" failures=\"0\" errors=\"0\"",
				"<testcase name=\"test\"",
				"<skipped>",
				"parent was skipped: should be skipped",
				"</skipped>",
				"</testcase>",
				"</testsuite>");
		//@formatter:on
	}

	@Test
	void writesFileForFailingContainer(@Root Path tempDirectory) throws Exception {
		DemoHierarchicalTestEngine engine = new DemoHierarchicalTestEngine("dummy");
		engine.addTest("test", () -> fail("never called"));
		engine.getEngineDescriptor().setBeforeAllBehavior(() -> fail("failure before all tests"));

		executeTests(engine, tempDirectory);

		String content = readValidXmlFile(tempDirectory.resolve("TEST-dummy.xml"));

		//@formatter:off
		assertThat(content)
			.containsSequence(
				"<testsuite name=\"dummy\" tests=\"1\" skipped=\"0\" failures=\"1\" errors=\"0\"",
				"<testcase name=\"test\"",
				"<failure message=\"failure before all tests\" type=\"" + AssertionFailedError.class.getName() + "\">",
				"AssertionFailedError: failure before all tests",
				"\tat",
				"</failure>",
				"</testcase>",
				"</testsuite>");
		//@formatter:on
	}

	@Test
	void writesSystemProperties(@Root Path tempDirectory) throws Exception {
		DemoHierarchicalTestEngine engine = new DemoHierarchicalTestEngine("dummy");
		engine.addTest("test", () -> {
		});

		executeTests(engine, tempDirectory);

		String content = readValidXmlFile(tempDirectory.resolve("TEST-dummy.xml"));

		//@formatter:off
		assertThat(content)
			.containsSequence(
				"<testsuite",
				"<properties>",
				"<property name=\"file.separator\" value=\"" + File.separator + "\"/>",
				"<property name=\"path.separator\" value=\"" + File.pathSeparator + "\"/>",
				"</properties>",
				"<testcase",
				"</testsuite>");
		//@formatter:on
	}

	@Test
	void writesHostNameAndTimestamp(@Root Path tempDirectory) throws Exception {
		DemoHierarchicalTestEngine engine = new DemoHierarchicalTestEngine("dummy");
		engine.addTest("test", () -> {
		});

		LocalDateTime now = LocalDateTime.parse("2016-01-28T14:02:59.123");
		ZoneId zone = ZoneId.systemDefault();

		executeTests(engine, tempDirectory, Clock.fixed(ZonedDateTime.of(now, zone).toInstant(), zone));

		String content = readValidXmlFile(tempDirectory.resolve("TEST-dummy.xml"));

		//@formatter:off
		assertThat(content)
			.containsSequence(
				"<testsuite",
				"hostname=\"" + InetAddress.getLocalHost().getHostName() + "\"",
				"timestamp=\"2016-01-28T14:02:59\"",
				"<testcase",
				"</testsuite>");
		//@formatter:on
	}

	@Test
	void printsExceptionWhenReportsDirCannotBeCreated(@Root Path tempDirectory) throws Exception {
		Path reportsDir = tempDirectory.resolve("dummy.txt");
		Files.write(reportsDir, singleton("content"));

		StringWriter out = new StringWriter();
		XmlReportsWritingListener listener = new XmlReportsWritingListener(reportsDir, new PrintWriter(out));

		listener.testPlanExecutionStarted(TestPlan.from(emptySet()));

		assertThat(out.toString()).containsSequence("Could not create reports directory", "FileAlreadyExistsException",
			"at ");
	}

	@Test
	void printsExceptionWhenReportCouldNotBeWritten(@Root Path tempDirectory) throws Exception {
		EngineDescriptor engineDescriptor = new EngineDescriptor(UniqueId.forEngine("engine"), "Engine");

		Path xmlFile = tempDirectory.resolve("TEST-engine.xml");
		Files.createDirectories(xmlFile);

		StringWriter out = new StringWriter();
		XmlReportsWritingListener listener = new XmlReportsWritingListener(tempDirectory, new PrintWriter(out));

		listener.testPlanExecutionStarted(TestPlan.from(singleton(engineDescriptor)));
		listener.executionFinished(TestIdentifier.from(engineDescriptor), successful());

		assertThat(out.toString()).containsSequence("Could not write XML report", "Exception", "at ");
	}

	@Test
	void writesReportEntriesToSystemOutElement(@Root Path tempDirectory, TestReporter testReporter) throws Exception {
		EngineDescriptor engineDescriptor = new EngineDescriptor(UniqueId.forEngine("engine"), "Engine");
		engineDescriptor.addChild(new TestDescriptorStub(UniqueId.root("child", "test"), "test"));
		TestPlan testPlan = TestPlan.from(singleton(engineDescriptor));

		StringWriter out = new StringWriter();
		XmlReportsWritingListener listener = new XmlReportsWritingListener(tempDirectory, new PrintWriter(out));

		listener.testPlanExecutionStarted(testPlan);
		TestIdentifier testIdentifier = testPlan.getTestIdentifier("[child:test]");
		listener.executionStarted(testIdentifier);
		listener.reportingEntryPublished(testIdentifier, ReportEntry.from("foo", "bar"));
		Map<String, String> map = new LinkedHashMap<>();
		map.put("bar", "baz");
		map.put("qux", "foo");
		listener.reportingEntryPublished(testIdentifier, ReportEntry.from(map));
		listener.executionFinished(testIdentifier, successful());
		listener.executionFinished(testPlan.getTestIdentifier("[engine:engine]"), successful());

		String content = readValidXmlFile(tempDirectory.resolve("TEST-engine.xml"));
		//testReporter.publishEntry("xml", content);

		//@formatter:off
		assertThat(content)
			.containsSequence(
				"<testsuite",
				"<testcase",
				"<system-out>",
				"Report Entry #1 (timestamp: " + Year.now(),
				"- foo: bar\n",
				"Report Entry #2 (timestamp: " + Year.now(),
				"- bar: baz\n",
				"- qux: foo\n",
				"</system-out>",
				"</testcase>",
				"</testsuite>");
		//@formatter:on
	}

	private void executeTests(TestEngine engine, Path tempDirectory) {
		executeTests(engine, tempDirectory, Clock.systemDefaultZone());
	}

	private void executeTests(TestEngine engine, Path tempDirectory, Clock clock) {
		PrintWriter out = new PrintWriter(new StringWriter());
		XmlReportsWritingListener reportListener = new XmlReportsWritingListener(tempDirectory.toString(), out, clock);
		Launcher launcher = createLauncher(engine);
		launcher.registerTestExecutionListeners(reportListener);
		launcher.execute(request().selectors(selectUniqueId(UniqueId.forEngine(engine.getId()))).build(),
			new TestExecutionListener[0]);
	}

	private String readValidXmlFile(Path xmlFile) throws Exception {
		assertTrue(Files.exists(xmlFile), () -> "File does not exist: " + xmlFile);
		String content = new String(Files.readAllBytes(xmlFile), UTF_8);
		return ensureValidAccordingToJenkinsSchema(content);
	}

}
