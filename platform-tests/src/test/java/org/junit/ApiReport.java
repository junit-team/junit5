/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit;

import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.nio.CharBuffer;
import java.util.List;
import java.util.Map;

import org.junit.platform.commons.meta.API.Usage;

/**
 * @since 1.0
 */
class ApiReport {

	private static final String MARKDOWN_FORMAT = "%-52s | %-42s | %-12s | %-27s%n";

	private final List<Class<?>> types;

	private final Map<Usage, List<Class<?>>> declarationsMap;

	public ApiReport(List<Class<?>> types, Map<Usage, List<Class<?>>> declarationsMap) {
		this.types = types;
		this.declarationsMap = declarationsMap;
	}

	public void printMarkdownReport(PrintStream out) {
		out.println("# `@API` Declarations");
		out.println();
		out.println("Discovered " + this.types.size() + " types with `@API` declarations.");
		this.declarationsMap.forEach((k, v) -> this.printMarkdown(k, v, out));
	}

	private void printMarkdown(Usage usage, List<Class<?>> types, PrintStream out) {
		out.printf("%n## `@API(%s)`%n", usage);
		out.printf("%nDiscovered %d `@API(%s)` declarations.%n", types.size(), usage);
		if (types.size() > 0) {
			out.printf("%n");
			out.printf(MARKDOWN_FORMAT, "PACKAGE NAME", "CLASS NAME", "TYPE", "MODIFIERS");
			out.printf(MARKDOWN_FORMAT, dashes(52), dashes(42), dashes(12), dashes(27));
			types.forEach(type -> this.printMarkdown(type, out));
		}
	}

	private void printMarkdown(Class<?> type, PrintStream out) {
		out.printf(MARKDOWN_FORMAT, //
			code(type.getPackage().getName()), //
			code(type.getSimpleName()), //
			code(getKind(type)), //
			code(Modifier.toString(type.getModifiers())) //
		);
	}

	private static String getKind(Class<?> type) {
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

	private static String code(String element) {
		return "`" + element + "`";
	}

	private static String dashes(int length) {
		return CharBuffer.allocate(length).toString().replace('\0', '-');
	}

}
