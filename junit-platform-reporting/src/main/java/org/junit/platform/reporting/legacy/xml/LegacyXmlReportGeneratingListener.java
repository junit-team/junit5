/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.reporting.legacy.xml;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Clock;
import java.util.Optional;
import java.util.function.Function;

import javax.xml.stream.XMLStreamException;

import org.apiguardian.api.API;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * {@code LegacyXmlReportGeneratingListener} is a {@link TestExecutionListener}
 * that generates a separate XML report for each {@linkplain TestPlan#getRoots()
 * root} in the {@link TestPlan}.
 *
 * <p>
 * Note that the generated XML format is compatible with the <em>legacy</em> de
 * facto standard for JUnit 4 based test reports that was made popular by the
 * Ant build system.
 *
 * @since 1.4
 * @see org.junit.platform.launcher.listeners.LoggingListener
 * @see org.junit.platform.launcher.listeners.SummaryGeneratingListener
 */
@API(status = EXPERIMENTAL, since = "1.4")
public class LegacyXmlReportGeneratingListener implements TestExecutionListener {

	private final Path reportsDir;
	private final PrintWriter out;
	private final Clock clock;
	private final Function<TestIdentifier, Optional<String>> rootFileName;

	private XmlReportData reportData;

	/**
	 * Creates a legacy xml report generating listener that outputs one file per test root.
	 *
	 * @param reportsDir the directory into which the xml report files will be written.
	 * @param out writer to output diagnostic information.
	 */
	public LegacyXmlReportGeneratingListener(Path reportsDir, PrintWriter out) {
		this(reportsDir, out, Clock.systemDefaultZone(), LegacyXmlReportGeneratingListener::defaultRootFileName);
	}

	/**
	 * Creates a legacy xml report generating listener with custom function for
	 * determining root nodes.
	 * <p>
	 *
	 * This constructor takes a function that can be used to change the behavior
	 * for when output files are written. The supplied {@code rootFileName} function
	 * is called every time a test is skipped or completed - if it returns a string,
	 * an xml report is generated for this node using the string as the basis of the
	 * filename; otherwise if it returns empty no file is written for this node.
	 *
	 * @param reportsDir the directory into which the xml report files will be written.
	 * @param out writer to output diagnostic information.
	 * @param rootFileName function that returns an optional string for the file name
	 *     to which the supplied test should be written. If the function returns a string,
	 *     the node (and its children) will be written to a file; otherwise if it returns empty no
	 *     file will be written for this node.
	 */
	public LegacyXmlReportGeneratingListener(Path reportsDir, PrintWriter out,
			Function<TestIdentifier, Optional<String>> rootFileName) {
		this(reportsDir, out, Clock.systemDefaultZone(), rootFileName);
	}

	// For tests only
	LegacyXmlReportGeneratingListener(String reportsDir, PrintWriter out, Clock clock) {
		this(Paths.get(reportsDir), out, clock, LegacyXmlReportGeneratingListener::defaultRootFileName);
	}

	private LegacyXmlReportGeneratingListener(Path reportsDir, PrintWriter out, Clock clock,
			Function<TestIdentifier, Optional<String>> rootFileName) {
		this.reportsDir = reportsDir;
		this.out = out;
		this.clock = clock;
		this.rootFileName = rootFileName;
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		this.reportData = new XmlReportData(testPlan, clock);
		try {
			Files.createDirectories(this.reportsDir);
		}
		catch (IOException e) {
			printException("Could not create reports directory: " + this.reportsDir, e);
		}
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		this.reportData = null;
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		this.reportData.markSkipped(testIdentifier, reason);
		writeXmlReportInCaseOfRoot(testIdentifier);
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		this.reportData.markStarted(testIdentifier);
	}

	@Override
	public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
		this.reportData.addReportEntry(testIdentifier, entry);
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult result) {
		this.reportData.markFinished(testIdentifier, result);
		writeXmlReportInCaseOfRoot(testIdentifier);
	}

	private void writeXmlReportInCaseOfRoot(TestIdentifier testIdentifier) {
		rootFileName.apply(testIdentifier).ifPresent(x -> writeXmlReportSafely(testIdentifier, x));
	}

	private void writeXmlReportSafely(TestIdentifier testIdentifier, String rootName) {
		Path xmlFile = this.reportsDir.resolve("TEST-" + rootName + ".xml");
		try (Writer fileWriter = Files.newBufferedWriter(xmlFile)) {
			new XmlReportWriter(this.reportData).writeXmlReport(testIdentifier, fileWriter);
		}
		catch (XMLStreamException | IOException e) {
			printException("Could not write XML report: " + xmlFile, e);
		}
	}

	/**
	 * Signify that the specified node should be written to a file. The optional
	 * returned string is used as the basis of the filename. If an empty optional is
	 * returned, then no file is written for this node.
	 * <p>
	 * This default implementation returns the uniqueId segment if it is a root test
	 * identifier (ie, it has no parent); otherwise it returns empty. Thus one file
	 * will be written for every root node in the test. Override this if you want to
	 * control when files are written (eg, one per test class or one per engine
	 * instead of one per test root) by supplying a custom function to the
	 * constructor.
	 *
	 * @param testIdentifier the identifier of the test being checked for writing to
	 *                       a file.
	 * @return An {@code Optional} containing the filename stub if the data is to be
	 *         written to a file, or empty if it is not to be written.
	 */
	private static Optional<String> defaultRootFileName(TestIdentifier testIdentifier) {
		if (!testIdentifier.getParentId().isPresent()) {
			return Optional.of(UniqueId.parse(testIdentifier.getUniqueId()).getSegments().get(0).getValue());
		}
		return Optional.empty();
	}

	private void printException(String message, Exception exception) {
		out.println(message);
		exception.printStackTrace(out);
	}

}
