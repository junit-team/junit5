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

import static java.util.Collections.singleton;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

import org.junit.gen5.launcher.TestExecutionListener;
import org.junit.gen5.launcher.TestPlan;

class XmlReportsWritingListener implements TestExecutionListener {

	private final File reportsDir;
	private final PrintWriter out;

	public XmlReportsWritingListener(String reportsDir, PrintWriter out) {
		this.reportsDir = new File(reportsDir);
		this.out = out;
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		try {
			Files.createDirectories(reportsDir.toPath());
		}
		catch (IOException e) {
			out.println("Could not create report directory: " + reportsDir);
			e.printStackTrace(out);
		}
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		// TODO #86 Write real files
		Path xmlFile = reportsDir.toPath().resolve("JUNIT5-Tests.xml");
		try {
			Files.write(xmlFile, singleton("JUnit 5 test run finished at " + LocalDateTime.now()));
		}
		catch (IOException e) {
			out.println("Could not write file: " + xmlFile);
			e.printStackTrace(out);
		}
	}

}
