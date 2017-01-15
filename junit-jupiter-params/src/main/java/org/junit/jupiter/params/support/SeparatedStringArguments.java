/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.support;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;
import java.util.regex.Pattern;

import org.junit.jupiter.params.Arguments;

public class SeparatedStringArguments implements Arguments {

	private static final Pattern DEFAULT_SEPARATOR_PATTERN = Pattern.compile("[^\\\\]?[,|]");

	private final String line;
	private final Pattern separatorPattern;

	public static SeparatedStringArguments create(String line) {
		return new SeparatedStringArguments(line, DEFAULT_SEPARATOR_PATTERN);
	}

	public static SeparatedStringArguments create(String line, String... separators) {
		Pattern separatorPattern = Pattern.compile(Arrays.stream(separators).map(Pattern::quote).collect(joining("|")));
		return new SeparatedStringArguments(line, separatorPattern);
	}

	private SeparatedStringArguments(String line, Pattern separatorPattern) {
		this.line = line;
		this.separatorPattern = separatorPattern;
	}

	@Override
	public Object[] getArguments() {
		return separatorPattern.splitAsStream(line).map(String::trim).toArray(Object[]::new);
	}
}
