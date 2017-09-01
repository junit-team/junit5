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
import java.nio.CharBuffer;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.junit.platform.commons.meta.API.Usage;

/**
 * @since 1.0
 */
abstract class AbstractApiReport implements ApiReport {

	private final List<Class<?>> types;

	private final Map<Usage, List<Class<?>>> declarationsMap;

	AbstractApiReport(List<Class<?>> types, Map<Usage, List<Class<?>>> declarationsMap) {
		this.types = types;
		this.declarationsMap = declarationsMap;
	}

	@Override
	public List<Class<?>> getTypes() {
		return this.types;
	}

	@Override
	public Map<Usage, List<Class<?>>> getDeclarationsMap() {
		return this.declarationsMap;
	}

	@Override
	public void printReportHeader(PrintStream out) {
		out.println(h1("`@API` Declarations"));
		out.println();
		out.printf("Discovered %d types with `@API` declarations.%n%n", getTypes().size());
	}

	@Override
	public void printDeclarationInfo(PrintStream out, EnumSet<Usage> usages) {
		// @formatter:off
		getDeclarationsMap().entrySet().stream()
				.filter(e -> usages.contains(e.getKey()))
				.forEach(e -> this.printDeclarationSection(e.getKey(), e.getValue(), out));
		// @formatter:on
	}

	protected void printDeclarationSection(Usage usage, List<Class<?>> types, PrintStream out) {
		printDeclarationHeader(usage, types, out);
		if (types.size() > 0) {
			printDeclarationTableHeader(out);
			types.forEach(type -> this.printDeclarationTableDetails(type, out));
			printDeclarationTableFooter(out);
			out.println();
		}
	}

	protected void printDeclarationHeader(Usage usage, List<Class<?>> types, PrintStream out) {
		out.println(h2(String.format("`@API(%s)`", usage)));
		out.println();
		out.printf("Discovered %d `@API(%s)` declarations.%n%n", types.size(), usage);
	}

	protected abstract String h1(String header);

	protected abstract String h2(String header);

	protected abstract void printDeclarationTableHeader(PrintStream out);

	protected abstract void printDeclarationTableDetails(Class<?> type, PrintStream out);

	protected abstract void printDeclarationTableFooter(PrintStream out);

	protected String getKind(Class<?> type) {
		if (type.isAnnotation()) {
			return "annotation";
		}
		if (type.isEnum()) {
			return "enum";
		}
		if (type.isInterface()) {
			return "interface";
		}
		return "class";
	}

	protected String code(String element) {
		return "`" + element + "`";
	}

	protected String dashes(int length) {
		return CharBuffer.allocate(length).toString().replace('\0', '-');
	}

}
