/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.api.tools;

import java.io.PrintStream;
import java.lang.reflect.Modifier;

/**
 * @since 1.0
 */
class MarkdownApiReportWriter extends AbstractApiReportWriter {

	private static final String MARKDOWN_FORMAT = "%-52s | %-42s | %-12s | %-27s%n";

	public MarkdownApiReportWriter(ApiReport apiReport) {
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
	protected void printDeclarationTableHeader(PrintStream out) {
		out.printf(MARKDOWN_FORMAT, "Package Name", "Class Name", "Type", "Modifiers");
		out.printf(MARKDOWN_FORMAT, dashes(52), dashes(42), dashes(12), dashes(27));
	}

	@Override
	protected void printDeclarationTableDetails(Class<?> type, PrintStream out) {
		out.printf(MARKDOWN_FORMAT, //
			code(type.getPackage().getName()), //
			code(type.getSimpleName()), //
			code(getKind(type)), //
			code(Modifier.toString(type.getModifiers())) //
		);
	}

	@Override
	protected void printDeclarationTableFooter(PrintStream out) {
		/* no-op */
	}

}
