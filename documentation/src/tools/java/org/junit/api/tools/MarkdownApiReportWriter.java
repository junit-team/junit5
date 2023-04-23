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

import java.io.PrintWriter;
import java.nio.CharBuffer;

import org.apiguardian.api.API;

/**
 * @since 1.0
 */
class MarkdownApiReportWriter extends AbstractApiReportWriter {

	private static final String MARKDOWN_FORMAT = "%-52s | %-42s | %-12s | %-27s%n";

	MarkdownApiReportWriter(ApiReport apiReport) {
		super(apiReport);
	}

	@Override
	protected String h1(String header) {
		return "# " + header;
	}

	@Override
	protected String h2(String header) {
		return "## " + header;
	}

	@Override
	protected String code(String element) {
		return "`" + element + "`";
	}

	@Override
	protected String italic(String element) {
		return "_" + element + "_";
	}

	@Override
	protected void printDeclarationTableHeader(PrintWriter out) {
		out.printf(MARKDOWN_FORMAT, "Package Name", "Type Name", "Since");
		out.printf(MARKDOWN_FORMAT, dashes(52), dashes(42), dashes(12), dashes(27));
	}

	private String dashes(int length) {
		return CharBuffer.allocate(length).toString().replace('\0', '-');
	}

	@Override
	protected void printDeclarationTableRow(Class<?> type, PrintWriter out) {
		out.printf(MARKDOWN_FORMAT, //
			code(type.getPackage().getName()), //
			code(type.getSimpleName()) + " " + italic("(" + getKind(type) + ")"), //
			code(type.getAnnotation(API.class).since()) //
		);
	}

	@Override
	protected void printDeclarationTableFooter(PrintWriter out) {
		/* no-op */
	}

}
