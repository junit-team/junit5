/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.api.tools;

import java.io.PrintWriter;
import java.util.EnumSet;

import org.apiguardian.api.API.Status;
import org.apiguardian.report.ApiReport;

/**
 * @since 1.0
 */
class ApiReportGenerator {

	public static void main(String... args) {
		PrintWriter writer = new PrintWriter(System.out, true);

		// scan all types below "org.junit" package
		ApiReport apiReport = ApiReport.generateReport("org.junit");

		// ApiReportWriter reportWriter = new MarkdownApiReportWriter(apiReport);
		ApiReportWriter reportWriter = new AsciidocApiReportWriter(apiReport);
		// ApiReportWriter reportWriter = new HtmlApiReportWriter(apiReport);

		// reportWriter.printReportHeader(writer);

		// Print report for all Usage enum constants
		// reportWriter.printDeclarationInfo(writer, EnumSet.allOf(Usage.class));

		// Print report only for Experimental Usage constant
		reportWriter.printDeclarationInfo(writer, EnumSet.of(Status.EXPERIMENTAL));
	}

}
