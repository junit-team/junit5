/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.tasks;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.console.tasks.FlatPrintingListener.INDENTATION;
import static org.junit.platform.engine.TestExecutionResult.failed;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.assertj.core.util.Maps;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.fakes.TestDescriptorStub;
import org.junit.platform.launcher.TestIdentifier;
import org.opentest4j.AssertionFailedError;

/**
 * @since 1.0
 */
class FlatPrintingListenerTests {

	private static final String EOL = System.lineSeparator();

	@Test
	void executionSkipped() {
		var stringWriter = new StringWriter();
		listener(stringWriter).executionSkipped(newTestIdentifier(), "Test" + EOL + "disabled");
		var lines = lines(stringWriter);

		assertEquals(3, lines.length);
		assertAll("lines in the output", //
			() -> assertEquals("Skipped:     demo-test ([engine:demo-engine])", lines[0]), //
			() -> assertEquals(INDENTATION + "=> Reason: Test", lines[1]), //
			() -> assertEquals(INDENTATION + "disabled", lines[2]));
	}

	@Test
	void reportingEntryPublished() {
		var stringWriter = new StringWriter();
		listener(stringWriter).reportingEntryPublished(newTestIdentifier(), ReportEntry.from("foo", "bar"));
		var lines = lines(stringWriter);

		assertEquals(2, lines.length);
		assertAll("lines in the output", //
			() -> assertEquals("Reported:    demo-test ([engine:demo-engine])", lines[0]), //
			() -> assertTrue(lines[1].startsWith(INDENTATION + "=> Reported values: ReportEntry [timestamp =")), //
			() -> assertTrue(lines[1].endsWith(", foo = 'bar']")));
	}

	@Test
	void executionFinishedWithFailure() {
		var stringWriter = new StringWriter();
		listener(stringWriter).executionFinished(newTestIdentifier(), failed(new AssertionError("Boom!")));
		var lines = lines(stringWriter);

		assertAll("lines in the output", //
			() -> assertEquals("Finished:    demo-test ([engine:demo-engine])", lines[0]), //
			() -> assertEquals(INDENTATION + "=> Exception: java.lang.AssertionError: Boom!", lines[1]));
	}

	@Nested
	class DiffOutputTests {
		@Test
		void printDiffForStringsInAssertionFailedErrors() {
			var stringWriter = new StringWriter();
			listener(stringWriter).executionFinished(newTestIdentifier(),
				failed(new AssertionFailedError("Detail Message", "Expected content", "Actual content")));
			var lines = lines(stringWriter);

			assertTrue(lines.length >= 5, "At least 5 lines are expected in failure report!");
			assertAll("lines in the output", //
				() -> assertEquals("Finished:    demo-test ([engine:demo-engine])", lines[0]), //
				() -> assertEquals(INDENTATION + "=> Expected : Expected content", lines[1]), //
				() -> assertEquals(INDENTATION + "=> Actual   : Actual content", lines[2]), //
				() -> assertEquals(INDENTATION + "=> Diff     : ~Expected~**Actual** content", lines[3]), //
				() -> assertEquals(INDENTATION + "=> Exception: org.opentest4j.AssertionFailedError: Detail Message",
					lines[4]));
		}

		@Test
		void ignoreDiffForNumbersInAssertionFailedErrors() {
			var stringWriter = new StringWriter();
			listener(stringWriter).executionFinished(newTestIdentifier(),
				failed(new AssertionFailedError("Detail Message", 10, 20)));
			var lines = lines(stringWriter);

			assertTrue(lines.length >= 2, "At least 3 lines are expected in failure report!");
			assertAll("lines in the output", //
				() -> assertEquals("Finished:    demo-test ([engine:demo-engine])", lines[0]), //
				() -> assertEquals(INDENTATION + "=> Exception: org.opentest4j.AssertionFailedError: Detail Message",
					lines[1]));
		}

		@Test
		void ignoreDiffForAnyAssertionErrors() {
			var stringWriter = new StringWriter();
			listener(stringWriter).executionFinished(newTestIdentifier(), failed(new AssertionError("Detail Message")));
			var lines = lines(stringWriter);

			assertTrue(lines.length >= 2, "At least 2 lines are expected in failure report!");
			assertAll("lines in the output", //
				() -> assertEquals("Finished:    demo-test ([engine:demo-engine])", lines[0]), //
				() -> assertEquals(INDENTATION + "=> Exception: java.lang.AssertionError: Detail Message", lines[1]));
		}
	}

	@Nested
	class ColorPaletteTests {

		@Nested
		class DefaultColorPaletteTests {

			@Test
			void executionSkipped() {
				var stringWriter = new StringWriter();
				new FlatPrintingListener(new PrintWriter(stringWriter), ColorPalette.DEFAULT).executionSkipped(
					newTestIdentifier(), "Test" + EOL + "disabled");
				var lines = lines(stringWriter);

				assertEquals(3, lines.length);
				assertAll("lines in the output", //
					() -> assertEquals("\u001B[35mSkipped:     demo-test ([engine:demo-engine])\u001B[0m", lines[0]), //
					() -> assertEquals("\u001B[35m" + INDENTATION + "=> Reason: Test", lines[1]), //
					() -> assertEquals(INDENTATION + "disabled\u001B[0m", lines[2]));
			}

			@Test
			void reportingEntryPublished() {
				var stringWriter = new StringWriter();
				new FlatPrintingListener(new PrintWriter(stringWriter), ColorPalette.DEFAULT).reportingEntryPublished(
					newTestIdentifier(), ReportEntry.from("foo", "bar"));
				var lines = lines(stringWriter);

				assertEquals(2, lines.length);
				assertAll("lines in the output", //
					() -> assertEquals("\u001B[37mReported:    demo-test ([engine:demo-engine])\u001B[0m", lines[0]), //
					() -> assertTrue(lines[1].startsWith(
						"\u001B[37m" + INDENTATION + "=> Reported values: ReportEntry [timestamp =")), //
					() -> assertTrue(lines[1].endsWith(", foo = 'bar']\u001B[0m")));
			}

			@Test
			void executionFinishedWithFailure() {
				var stringWriter = new StringWriter();
				new FlatPrintingListener(new PrintWriter(stringWriter), ColorPalette.DEFAULT).executionFinished(
					newTestIdentifier(), failed(new AssertionError("Boom!")));
				var lines = lines(stringWriter);

				assertAll("lines in the output", //
					() -> assertEquals("\u001B[31mFinished:    demo-test ([engine:demo-engine])\u001B[0m", lines[0]), //
					() -> assertEquals("\u001B[31m" + INDENTATION + "=> Exception: java.lang.AssertionError: Boom!",
						lines[1]),
					() -> assertTrue(lines[lines.length - 1].endsWith("\u001B[0m")));
			}

		}

		@Nested
		class ColorPaletteOverrideTests {

			@Test
			void overridingSkipped() {
				var stringWriter = new StringWriter();
				ColorPalette colorPalette = new ColorPalette(Maps.newHashMap(Style.SKIPPED, "36;7"));
				new FlatPrintingListener(new PrintWriter(stringWriter), colorPalette).executionSkipped(
					newTestIdentifier(), "Test" + EOL + "disabled");
				var lines = lines(stringWriter);

				assertEquals(3, lines.length);
				assertAll("lines in the output", //
					() -> assertEquals("\u001B[36;7mSkipped:     demo-test ([engine:demo-engine])\u001B[0m", lines[0]), //
					() -> assertEquals("\u001B[36;7m" + INDENTATION + "=> Reason: Test", lines[1]), //
					() -> assertEquals(INDENTATION + "disabled\u001B[0m", lines[2]));
			}

			@Test
			void overridingUnusedStyle() {
				var stringWriter = new StringWriter();
				ColorPalette colorPalette = new ColorPalette(Maps.newHashMap(Style.FAILED, "36;7"));
				new FlatPrintingListener(new PrintWriter(stringWriter), colorPalette).executionSkipped(
					newTestIdentifier(), "Test" + EOL + "disabled");
				var lines = lines(stringWriter);

				assertEquals(3, lines.length);
				assertAll("lines in the output", //
					() -> assertEquals("\u001B[35mSkipped:     demo-test ([engine:demo-engine])\u001B[0m", lines[0]), //
					() -> assertEquals("\u001B[35m" + INDENTATION + "=> Reason: Test", lines[1]), //
					() -> assertEquals(INDENTATION + "disabled\u001B[0m", lines[2]));
			}

		}

	}

	private FlatPrintingListener listener(StringWriter stringWriter) {
		return new FlatPrintingListener(new PrintWriter(stringWriter), ColorPalette.NONE);
	}

	private static TestIdentifier newTestIdentifier() {
		var testDescriptor = new TestDescriptorStub(UniqueId.forEngine("demo-engine"), "demo-test");
		return TestIdentifier.from(testDescriptor);
	}

	private String[] lines(StringWriter stringWriter) {
		return stringWriter.toString().split(EOL);
	}

}
