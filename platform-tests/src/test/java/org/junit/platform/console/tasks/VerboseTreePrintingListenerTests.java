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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.TestExecutionResult.failed;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.test.TestDescriptorStub;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.0
 */
public class VerboseTreePrintingListenerTests {

	private static final String EOL = System.lineSeparator();

	private TestPlan testPlan = TestPlan.from(Collections.emptyList());

	private TreePrintingListener createTreePrinter(Writer writer) {
		PrintWriter printWriter = new PrintWriter(writer);
		return new VerboseTreePrintingListener(printWriter, true, 9, TreePrintingListener.Theme.UTF_8);
	}

	@Test
	public void executionSkipped() {
		StringWriter stringWriter = new StringWriter();
		listener(stringWriter).executionSkipped(newTestIdentifier(), "Test" + EOL + "disabled");
		String[] lines = lines(stringWriter);

		assertEquals(7, lines.length);
		assertAll("lines in the output", //
			() -> assertEquals("├─ demo-test", lines[0]), //
			() -> assertEquals("     tags: []", lines[1]), //
			() -> assertEquals(" uniqueId: [engine:demo-engine]", lines[2]), //
			() -> assertEquals("   parent: []", lines[3]), //
			() -> assertEquals("   reason: Test", lines[4]), //
			() -> assertEquals("             disabled", lines[5]), //
			() -> assertEquals("   status: ↷ SKIPPED", lines[6]) //
		);
	}

	@Test
	public void reportingEntryPublished() {
		StringWriter stringWriter = new StringWriter();
		listener(stringWriter).reportingEntryPublished(newTestIdentifier(), ReportEntry.from("foo", "bar"));
		String[] lines = lines(stringWriter);

		assertEquals(1, lines.length);
		assertAll("lines in the output", //
			() -> assertTrue(lines[0].startsWith("  reports: ReportEntry [timestamp =")), //
			() -> assertTrue(lines[0].endsWith(", foo = 'bar']")) //
		);
	}

	@Test
	public void executionFinishedWithFailure() {
		StringWriter stringWriter = new StringWriter();
		listener(stringWriter).executionFinished(newTestIdentifier(), failed(new AssertionError("Boom!")));
		String[] lines = lines(stringWriter);

		assertAll("lines in the output", //
			() -> assertEquals("   caught: java.lang.AssertionError: Boom!", lines[0]), //
			() -> assertEquals("   status: ✘ FAILED", lines[lines.length - 1]));
	}

	@Test
	public void emptyTree() throws Exception {
		StringWriter stringWriter = new StringWriter();
		TreePrintingListener listener = createTreePrinter(stringWriter);

		listener.testPlanExecutionStarted(testPlan);
		listener.testPlanExecutionFinished(testPlan);

		String output = stringWriter.toString();
		assertAll("empty tree", //
			() -> assertTrue(output.contains("Test plan execution started"), "plan start"), //
			() -> assertTrue(output.contains("Number of static tests: 0"), "0 static tests"), //
			() -> assertTrue(output.contains("Test plan execution finished"), "plan finish"), //
			() -> assertTrue(output.contains("Number of all tests: 0"), "still 0 tests") //
		);
	}

	@Test
	public void simpleTree() throws Exception {
		TestIdentifier engine1 = createContainerIdentifier("engine mercury");
		TestIdentifier engine2 = createContainerIdentifier("engine venus");
		TestIdentifier containerA = createContainerIdentifier("container alpha");
		TestIdentifier containerB = createContainerIdentifier("container beta");
		TestIdentifier test00 = createTestIdentifier("test 00");
		TestIdentifier test01 = createTestIdentifier("test 01");
		TestIdentifier test10 = createTestIdentifier("test 10");
		TestIdentifier test11 = createTestIdentifier("test 11");

		StringWriter stringWriter = new StringWriter();
		TreePrintingListener listener = createTreePrinter(stringWriter);

		listener.testPlanExecutionStarted(testPlan);
		listener.executionStarted(engine1);
		listener.executionStarted(containerA);
		listener.executionStarted(test00);
		listener.executionFinished(test00, TestExecutionResult.successful());
		listener.executionStarted(test01);
		listener.executionFinished(test01, TestExecutionResult.successful());
		listener.executionFinished(containerA, TestExecutionResult.successful());
		listener.executionFinished(engine1, TestExecutionResult.successful());

		listener.executionStarted(engine2);
		listener.executionStarted(containerB);
		listener.executionStarted(test10);
		listener.executionFinished(test10, TestExecutionResult.successful());
		listener.executionStarted(test11);
		listener.executionFinished(test11, TestExecutionResult.successful());
		listener.executionFinished(containerB, TestExecutionResult.successful());
		listener.executionFinished(engine2, TestExecutionResult.successful());
		listener.testPlanExecutionFinished(testPlan);

		String[] lines = stringWriter.toString().split("\\R");
		assertEquals(35, lines.length);
		assertAll("lines in the output", //
			() -> assertEquals("Test plan execution started. Number of static tests: 4", lines[0]), //
			() -> assertEquals(".", lines[1]), //
			() -> assertEquals("├─ engine mercury", lines[2]), //
			() -> assertEquals("│  ├─ container alpha", lines[3]), //
			() -> assertEquals("│  │  ├─ test 00", lines[4]), //
			() -> assertEquals("│  │  │       tags: []", lines[5]), //
			() -> assertEquals("│  │  │   uniqueId: [test:test 00]", lines[6]), //
			() -> assertEquals("│  │  │     parent: []", lines[7]), //
			() -> assertTrue(lines[8].startsWith("│  │  │   duration: ")), //
			() -> assertEquals("│  │  │     status: ✔ SUCCESSFUL", lines[9]), //
			() -> assertTrue(lines[16].startsWith("│  └─ container alpha finished after")), //
			() -> assertEquals("Test plan execution finished. Number of all tests: 4", lines[34]) //
		);
	}

	private TestIdentifier createTestIdentifier(String uniqueId) {
		TestIdentifier identifier = TestIdentifier.from(
			new TestDescriptorStub(UniqueId.root("test", uniqueId), uniqueId));
		testPlan.add(identifier);
		return identifier;
	}

	private TestIdentifier createContainerIdentifier(String uniqueId) {
		TestIdentifier identifier = TestIdentifier.from(
			new TestDescriptorStub(UniqueId.root("container", uniqueId), uniqueId) {

				@Override
				public boolean isContainer() {
					return true;
				}

				@Override
				public boolean isTest() {
					return false;
				}
			});
		testPlan.add(identifier);
		return identifier;
	}

	private VerboseTreePrintingListener listener(StringWriter stringWriter) {
		return new VerboseTreePrintingListener(new PrintWriter(stringWriter), true, 50,
			TreePrintingListener.Theme.UTF_8);
	}

	private static TestIdentifier newTestIdentifier() {
		TestDescriptorStub testDescriptor = new TestDescriptorStub(UniqueId.forEngine("demo-engine"), "demo-test");
		return TestIdentifier.from(testDescriptor);
	}

	private String[] lines(StringWriter stringWriter) {
		return stringWriter.toString().split(EOL);
	}

}
