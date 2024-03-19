/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.api.tools;

import static java.lang.String.format;

import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import org.apiguardian.api.API.Status;

/**
 * @since 1.0
 */
abstract class AbstractApiReportWriter implements ApiReportWriter {

	protected static final int NAME_COLUMN_WIDTH = 128;

	private final ApiReport apiReport;

	AbstractApiReportWriter(ApiReport apiReport) {
		this.apiReport = apiReport;
	}

	@Override
	public void printReportHeader(PrintWriter out) {
		out.println(h1("@API Declarations"));
		out.println();
		out.println(paragraph(
			format("Discovered %d types with %s declarations.", this.apiReport.types().size(), code("@API"))));
		out.println();
	}

	@Override
	public void printDeclarationInfo(PrintWriter out, Set<Status> statuses) {
		statuses.forEach(
			status -> printDeclarationSection(statuses, status, this.apiReport.declarations().get(status), out));
	}

	protected void printDeclarationSection(Set<Status> statuses, Status status, List<Declaration> declarations,
			PrintWriter out) {
		printDeclarationSectionHeader(statuses, status, declarations, out);
		if (!declarations.isEmpty()) {
			printDeclarationTableHeader(out);
			declarations.forEach(it -> printDeclarationTableRow(it, out));
			printDeclarationTableFooter(out);
			out.println();
		}
	}

	protected void printDeclarationSectionHeader(Set<Status> statuses, Status status, List<Declaration> declarations,
			PrintWriter out) {
		if (statuses.size() < 2) {
			// omit section header when only a single status is printed
			return;
		}
		out.println(h2(format("@API(%s)", status)));
		out.println();
		out.println(
			paragraph(format("Discovered %d " + code("@API(%s)") + " declarations.", declarations.size(), status)));
		out.println();
	}

	protected abstract String h1(String header);

	protected abstract String h2(String header);

	protected abstract String code(String element);

	protected abstract String italic(String element);

	protected String paragraph(String element) {
		return element;
	}

	protected abstract void printDeclarationTableHeader(PrintWriter out);

	protected abstract void printDeclarationTableRow(Declaration declaration, PrintWriter out);

	protected abstract void printDeclarationTableFooter(PrintWriter out);

}
