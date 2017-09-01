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
import java.util.List;
import java.util.Map;

import org.junit.platform.commons.meta.API.Usage;

/**
 * @since 1.0
 */
class AsciidocApiReport extends AbstractApiReport {

	private static final String ASCIIDOC_FORMAT = "| %-52s | %-42s | %-12s%n";

	public AsciidocApiReport(List<Class<?>> types, Map<Usage, List<Class<?>>> declarationsMap) {
		super(types, declarationsMap);
	}

	@Override
	protected String h1(String header) {
		return "= " + header;
	}

	@Override
	protected String h2(String header) {
		return "== " + header;
	}

	@Override
	protected void printDeclarationTableHeader(PrintStream out) {
		out.println("|===");
		out.printf(ASCIIDOC_FORMAT, "Package Name", "Class Name", "Type");
		out.println();
	}

	@Override
	protected void printDeclarationTableDetails(Class<?> type, PrintStream out) {
		out.printf(ASCIIDOC_FORMAT, //
			code(type.getPackage().getName()), //
			code(type.getSimpleName()), //
			code(getKind(type)) //
		);
	}

	@Override
	protected void printDeclarationTableFooter(PrintStream out) {
		out.println("|===");
	}

}
